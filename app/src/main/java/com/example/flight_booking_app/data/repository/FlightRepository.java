package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.BaggageOption;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.FareOption;
import com.example.flight_booking_app.data.model.FareRule;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.FlightFilterState;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlightRepository — Firestore edition
 * ─────────────────────────────────────
 * Nhờ cấu trúc Firestore đã denormalize (snapshot airline/city nhúng thẳng vào Flight,
 * fareRule + baggageOptions nhúng thẳng vào FareClass), Repository này:
 * <p>
 * • Chỉ gọi 2 collection: "flights" + "fareClasses"
 * • KHÔNG còn loadAllCaches() / chain-of-callbacks 6 bước
 * • KHÔNG còn JOIN thủ công City / Airline / FareRule / BaggageOption
 * • Hỗ trợ pagination với DocumentSnapshot cursor
 */
public class FlightRepository {

    private static final int PAGE_SIZE = 5;

    private final FirebaseFirestore db;
    private final AppCacheManager cacheManager = AppCacheManager.getInstance();

    public FlightRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnFlightsLoaded {

        // DocumentSnapshot lastVisible (đây chính là "dấu sách" ghi nhớ bản ghi cuối cùng của trang trước)
        // để báo cho Firestore biết cần lấy tiếp từ vị trí đó.
        void onLoaded(List<Flight> flights, DocumentSnapshot lastVisible, Boolean isOffline);

        void onError(String error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tìm chuyến bay — trang đầu (không cursor).
     * Gọi từ HomeFragment khi người dùng bấm "Tìm".
     */
    public void searchFlights(String fromCityId, String toCityId, Long departureDate,
                              int totalPassengers, FlightFilterState filterState, OnFlightsLoaded callback) {
        searchFlightsInternal(fromCityId, toCityId, departureDate,
                totalPassengers, filterState, null, callback);
    }

    /**
     * Tải trang tiếp theo — dùng cursor (DocumentSnapshot trang cuối cùng).
     * Gọi từ ViewModel khi người dùng scroll xuống cuối danh sách.
     */
    public void searchFlightsNextPage(String fromCityId, String toCityId, Long departureDate,
                                      int totalPassengers, FlightFilterState filterState, DocumentSnapshot lastVisible,
                                      OnFlightsLoaded callback) {
        searchFlightsInternal(fromCityId, toCityId, departureDate,
                totalPassengers, filterState, lastVisible, callback);
    }

    private void searchFlightsInternal(String fromCityId, String toCityId,
                                       Long departureDate, int totalPassengers,
                                       FlightFilterState filterState,
                                       DocumentSnapshot cursor,
                                       OnFlightsLoaded callback) {
        // 1. Tính toán mốc bắt đầu ngày (00:00:00)
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(departureDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Timestamp startOfDay = new Timestamp(cal.getTime());

        // 2. Tính toán mốc kết thúc ngày (23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        Timestamp endOfDay = new Timestamp(cal.getTime());
        // Firestore query — server-side filter 3 điều kiện, sort theo minPrice
        Query query = db.collection("flights")
                .whereEqualTo("fromCityId", fromCityId)
                .whereEqualTo("toCityId", toCityId)
                .whereGreaterThanOrEqualTo("departureTime", startOfDay) // Lớn hơn hoặc bằng đầu ngày
                .whereLessThanOrEqualTo("departureTime", endOfDay)       // Nhỏ hơn hoặc bằng cuối ngày
                .whereEqualTo("status", "ON_TIME");

        // Áp dụng bộ lọc
        if (filterState != null) {
            // Lọc Hãng bay (Server gánh việc loại bỏ các document không cần thiết)
            // filterState.selectedAirlines.size() tối đa là 10 do giới hạn của Firestore
            if (filterState.selectedAirlines != null && !filterState.selectedAirlines.isEmpty()) {
                query = query.whereIn("airlineName", filterState.selectedAirlines);
            }

            // Sắp xếp cơ bản
            switch (filterState.sortMode) {
                case "PRICE_ASC":
                    query = query.orderBy("minPrice", Query.Direction.ASCENDING);
                    break;
                case "DEPART_EARLY":
                    query = query.orderBy("departureTime", Query.Direction.ASCENDING);
                    break;
                case "DURATION":
                    query = query.orderBy("duration", Query.Direction.ASCENDING); // Giả sử duration lưu dạng số phút trên db
                    break;
                default:
                    query = query.orderBy("minPrice", Query.Direction.ASCENDING);
                    break;
            }
        } else {
            // Sắp xếp mặc định
            query = query.orderBy("minPrice", Query.Direction.ASCENDING);
        }

        // Đặt giới hạn trang
        query = query.limit(PAGE_SIZE);

        if (cursor != null) {
            query = query.startAfter(cursor);
        }


        query.get().addOnSuccessListener(querySnapshot -> {
                    // Lấy con trỏ phân trang (Cursor) cho lần gọi tiếp theo
                    DocumentSnapshot nextCursor = null;
                    if (!querySnapshot.isEmpty()) {
                        nextCursor = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                    }

                    // Kiểm tra trạng thái mạng mạng (Đọc từ Cache Firestore hay từ Server)
                    boolean isOffline = querySnapshot.getMetadata().isFromCache();

                    //  Khởi tạo các kho chứa tạm thời
                    List<Flight> flightListRaw = new ArrayList<>();
                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    Map<Flight, String> cardToFareClassIdMap = new HashMap<>();

                    //  KIỂM TRA TRẠNG THÁI HẠN SỬ DỤNG CỦA RAM CACHE (Quan trọng!)
                    boolean isCacheReady = cacheManager.isCacheValid();

                    //  Duyệt qua danh sách chuyến bay thô từ Firestore
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Flight flight = parseFlightDocument(doc);
                        if (flight == null) continue;

                        // Lọc sơ bộ: Sức chứa phải đủ cho tổng số hành khách
                        if (flight.getAvailableSeats() < totalPassengers) continue;

                        List<FareOption> fareOptions = flight.getFareOptions();
                        if (fareOptions == null || fareOptions.isEmpty()) continue;

                        for (FareOption option : fareOptions) {
                            if (!option.isAvailable()) continue;

                            // Tạo một bản sao độc lập của Flight để gắn riêng từng gói vé (FareOption)
                            Flight card = parseFlightDocument(doc);
                            if (card == null) continue;

                            flightListRaw.add(card);
                            String fareClassId = option.getFareClassId();
                            cardToFareClassIdMap.put(card, fareClassId);

                            // CƠ CHẾ LAI (HYBRID):
                            // Nếu Cache tổng thể HỢP LỆ VÀ trong RAM đã có sẵn ID này -> Bỏ qua không gọi mạng.
                            // Nếu Cache HẾT HẠN HOẶC trong RAM chưa từng có ID này -> Bắt buộc tạo Task gọi Firebase.
                            if (!isCacheReady || cacheManager.getFareClass(fareClassId) == null) {
                                Task<DocumentSnapshot> fetchTask = db.collection("fareClasses").document(fareClassId).get();
                                tasks.add(fetchTask);
                            }
                        }
                    }

                    final DocumentSnapshot finalCursor = nextCursor;

                    // XỬ LÝ BẤT ĐỒNG BỘ: Chờ tất cả các Task gọi mạng chạy song song (nếu có)
                    if (!tasks.isEmpty()) {
                        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {

                            // Gom toàn bộ FareClass mới lấy từ server về vào danh sách tạm
                            List<FareClass> newlyFetchedFares = new ArrayList<>();
                            for (Task<?> task : tasks) {
                                if (task.isSuccessful() && task.getResult() instanceof DocumentSnapshot) {
                                    DocumentSnapshot fareDoc = (DocumentSnapshot) task.getResult();
                                    if (fareDoc.exists()) {
                                        FareClass fc = parseFareClass(fareDoc);
                                        if (fc != null) {
                                            newlyFetchedFares.add(fc);
                                        }
                                    }
                                }
                            }

                            // Nạp vào RAM
                            cacheManager.saveAllFareClasses(newlyFetchedFares);

                            // Ráp dữ liệu FareClass (lúc này chắc chắn đã đủ trong Cache) vào Flight
                            List<Flight> finalResult = matchFlightWithFareClass(flightListRaw, cardToFareClassIdMap);

                            // Trả kết quả về cho ViewModel
                            callback.onLoaded(finalResult, finalCursor, isOffline);
                        });
                    } else {
                        //  Nếu mọi thứ đã có sẵn trong RAM và Cache chưa hết hạn,
                        // ráp dữ liệu và trả về luôn TỨC THỜI, không tốn thời gian đợi Task mạng!
                        List<Flight> finalResult = matchFlightWithFareClass(flightListRaw, cardToFareClassIdMap);
                        callback.onLoaded(finalResult, finalCursor, isOffline);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));

    }
    private List<Flight> matchFlightWithFareClass(List<Flight> flightListRaw, Map<Flight, String> cardToFareClassIdMap) {
        List<Flight> finalResult = new ArrayList<>();

        for (Flight card : flightListRaw) {
            String neededId = cardToFareClassIdMap.get(card);
            FareClass fc = cacheManager.getFareClass(neededId);

            if (fc != null) {
                card.setSeatType(fc.getSeatType());
                card.setDisplayPrice(fc.getBasePrice());
                card.setSelectedFareClass(fc);
                finalResult.add(card);
            }
        }
        return finalResult;
    }

    // biến dữ liệu thô từ Firestore thành các Object Java.

    /**
     * Parse Flight từ Firestore document.
     */
    @SuppressWarnings("unchecked")
    private Flight parseFlightDocument(DocumentSnapshot doc) {
        if (!doc.exists()) return null;

        Flight f = new Flight();
        f.setFlightId(doc.getString("flightId"));
        f.setFlightNumber(doc.getString("flightNumber"));
        f.setAirlineId(doc.getString("airlineId"));
        f.setAircraftId(doc.getString("aircraftId"));
        f.setFromCityId(doc.getString("fromCityId"));
        f.setToCityId(doc.getString("toCityId"));
        f.setDepartureTime(doc.getTimestamp("departureTime"));
        f.setArrivalTime(doc.getTimestamp("arrivalTime"));
        f.setDuration(doc.getString("duration"));
        f.setStatus(doc.getString("status"));
        f.setSeatMapId(doc.getString("seatMapId"));
        f.setAirCraftName(doc.getString("aircraftModel"));

        Long seats = doc.getLong("availableSeats");
        if (seats != null) f.setAvailableSeats(seats.intValue());

        Double tax = doc.getDouble("taxFee");
        if (tax != null) f.setTaxFee(tax);

        // ── Snapshot airline/city đã denormalize — gán thẳng ──
        f.setAirlineName(doc.getString("airlineName"));
        f.setAirlineLogo(doc.getString("airlineLogo"));
        f.setFrom(doc.getString("fromCity"));
        f.setFromIata(doc.getString("fromIata"));
        f.setTo(doc.getString("toCity"));
        f.setToIata(doc.getString("toIata"));

        // ── fareOptions array ──
        List<Map<String, Object>> rawOptions =
                (List<Map<String, Object>>) doc.get("fareOptions");
        if (rawOptions != null) {
            List<FareOption> options = new ArrayList<>();
            for (Map<String, Object> raw : rawOptions) {
                FareOption fo = new FareOption();
                fo.setFareClassId((String) raw.get("fareClassId"));
                Long limit = (Long) raw.get("seatLimit");
                Long booked = (Long) raw.get("bookedCount");
                if (limit != null) fo.setSeatLimit(limit.intValue());
                if (booked != null) fo.setBookedCount(booked.intValue());
                options.add(fo);
            }
            f.setFareOptions(options);
        }

        return f;
    }

    /**
     * Parse FareClass từ Firestore document.
     * fareRule + baggageOptions đã nhúng sẵn trong document.
     */
    @SuppressWarnings("unchecked")
    private FareClass parseFareClass(DocumentSnapshot doc) {
        if (!doc.exists()) return null;

        FareClass fc = new FareClass();
        fc.setFareClassId(doc.getString("fareClassId"));
        fc.setAirlineId(doc.getString("airlineId"));
        fc.setTitle(doc.getString("title"));
        fc.setSeatType(doc.getString("seatType"));
        fc.setFareRuleId(doc.getString("fareRuleId"));

        Double basePrice = doc.getDouble("basePrice");
        if (basePrice != null) fc.setBasePrice(basePrice);

        // ── FareRule nhúng sẵn ──
        Map<String, Object> ruleMap = (Map<String, Object>) doc.get("fareRule");
        if (ruleMap != null) {
            FareRule rule = new FareRule();
            rule.setFareRuleId((String) ruleMap.get("fareRuleId"));
            rule.setAirlineId((String) ruleMap.get("airlineId"));
            rule.setFareClassName((String) ruleMap.get("fareClassName"));
            Long cabin = (Long) ruleMap.get("cabinBaggage");
            Long checked = (Long) ruleMap.get("checkedBaggage");
            if (cabin != null) rule.setCabinBaggage(cabin.intValue());
            if (checked != null) rule.setCheckedBaggage(checked.intValue());
            Boolean changeable = (Boolean) ruleMap.get("isChangeable");
            Boolean refundable = (Boolean) ruleMap.get("isRefundable");
            Boolean lounge = (Boolean) ruleMap.get("hasLoungeAccess");
            Boolean priority = (Boolean) ruleMap.get("hasPriority");
            Boolean meal = (Boolean) ruleMap.get("hasMeal");
            if (changeable != null) rule.setChangeable(changeable);
            if (refundable != null) rule.setRefundable(refundable);
            if (lounge != null) rule.setLoungeAccess(lounge);
            if (priority != null) rule.setHasPriority(priority);
            if (meal != null) rule.setHasMeal(meal);
            List<String> freeSeats = (List<String>) ruleMap.get("freeIncludedSeatTypes");
            if (freeSeats != null) rule.setFreeIncludedSeatTypes(freeSeats);
            fc.setFareRule(rule);
        }

        // ── BaggageOptions nhúng sẵn ──
        List<Map<String, Object>> rawBags =
                (List<Map<String, Object>>) doc.get("baggageOptions");
        if (rawBags != null) {
            ArrayList<BaggageOption> bags = new ArrayList<>();
            for (Map<String, Object> raw : rawBags) {
                BaggageOption bag = new BaggageOption();
                bag.setBaggageId((String) raw.get("baggageId"));
                Long weight = (Long) raw.get("weightKg");
                Double price = ((Number) raw.get("priceVnd")).doubleValue();
                if (weight != null) bag.setWeightKg(weight.intValue());
                if (price != null) bag.setPriceVnd(price);
                bags.add(bag);
            }
            fc.setBaggageOptions(bags);
        }

        return fc;
    }


}