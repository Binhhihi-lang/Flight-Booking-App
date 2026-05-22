package com.example.flight_booking_app.data.repository;

import androidx.annotation.NonNull;

import com.example.flight_booking_app.data.model.Airline;
import com.example.flight_booking_app.data.model.City;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.FareOption;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlightRepository - Tìm kiếm chuyến bay với đầy đủ tiêu chí.
 *
 * LỌC THEO:
 *   1. fromCityId, toCityId (điểm đi/đến)
 *   2. departureDate (ngày bay)
 *   3. seatType (ECONOMY / BUSINESS) - lọc qua fareOptions
 *   4. totalPassengers (kiểm tra availableSeats)
 *
 * JOIN:
 *   - Cities: cityName, iataCode
 *   - Airlines: name, logo
 *   - FareClasses: để xác định seatType của fareOptions
 *
 * LOGIC GIÁ:
 *   - displayPrice = giá RẺ NHẤT trong fareOptions phù hợp với seatType
 *   - Ví dụ: User chọn "Phổ thông" → lấy giá rẻ nhất trong các gói ECONOMY
 */
public class FlightRepository {

    private final DatabaseReference dbFlights;
    private final DatabaseReference dbCities;
    private final DatabaseReference dbAirlines;
    private final DatabaseReference dbFareClasses;

    // Cache
    private Map<String, City> citiesMap = new HashMap<>();
    private Map<String, Airline> airlinesMap = new HashMap<>();
    private Map<String, FareClass> fareClassesMap = new HashMap<>();

    public FlightRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        dbFlights     = db.getReference("Flights");
        dbCities      = db.getReference("Cities");
        dbAirlines    = db.getReference("Airlines");
        dbFareClasses = db.getReference("FareClasses");
    }

    public interface OnFlightsLoaded {
        void onLoaded(List<Flight> flights);
        void onError(String error);
    }

    /**
     * Tìm kiếm chuyến bay với đầy đủ tiêu chí.
     *
     * @param fromCityId      ID thành phố đi (VD: "city_hn")
     * @param toCityId        ID thành phố đến (VD: "city_hcm")
     * @param departureDate   Ngày bay (VD: "15/05/2026")
     * @param seatClass       Hạng ghế ("Phổ thông" / "Thương gia")
     * @param totalPassengers Tổng số hành khách (adult + child + baby)
     * @param callback        Callback trả kết quả
     */
    public void searchFlights(String fromCityId, String toCityId, String departureDate,
                              String seatClass, int totalPassengers, OnFlightsLoaded callback) {

        // Load cache trước
        loadAllCaches(() -> {
            // Query Flights từ Firebase
            queryAndFilterFlights(fromCityId, toCityId, departureDate, seatClass, totalPassengers, callback);
        });
    }

    /**
     * Load tất cả cache cần thiết: Cities, Airlines, FareClasses.
     */
    private void loadAllCaches(Runnable onComplete) {
        // Load Cities
        if (citiesMap.isEmpty()) {
            loadCitiesCache(() -> {
                // Load Airlines
                if (airlinesMap.isEmpty()) {
                    loadAirlinesCache(() -> {
                        // Load FareClasses
                        if (fareClassesMap.isEmpty()) {
                            loadFareClassesCache(onComplete);
                        } else {
                            onComplete.run();
                        }
                    });
                } else if (fareClassesMap.isEmpty()) {
                    loadFareClassesCache(onComplete);
                } else {
                    onComplete.run();
                }
            });
        } else if (airlinesMap.isEmpty()) {
            loadAirlinesCache(() -> {
                if (fareClassesMap.isEmpty()) {
                    loadFareClassesCache(onComplete);
                } else {
                    onComplete.run();
                }
            });
        } else if (fareClassesMap.isEmpty()) {
            loadFareClassesCache(onComplete);
        } else {
            onComplete.run();
        }
    }

    private void loadCitiesCache(Runnable onComplete) {
        dbCities.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                citiesMap.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    City city = child.getValue(City.class);
                    if (city != null && city.getCityId() != null) {
                        citiesMap.put(city.getCityId(), city);
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

    private void loadAirlinesCache(Runnable onComplete) {
        dbAirlines.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                airlinesMap.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Airline airline = child.getValue(Airline.class);
                    if (airline != null && airline.getAirlineId() != null) {
                        airlinesMap.put(airline.getAirlineId(), airline);
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

    private void loadFareClassesCache(Runnable onComplete) {
        dbFareClasses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fareClassesMap.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FareClass fc = child.getValue(FareClass.class);
                    if (fc != null && fc.getFareClassId() != null) {
                        fareClassesMap.put(fc.getFareClassId(), fc);
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

    /**
     * Query Flights và lọc theo tất cả tiêu chí.
     */
    private void queryAndFilterFlights(String fromCityId, String toCityId, String departureDate,
                                       String seatClass, int totalPassengers, OnFlightsLoaded callback) {

        // Map seatClass → seatType
        // "Phổ thông" → "ECONOMY", "Thương gia" → "BUSINESS"
        String targetSeatType = mapSeatClassToType(seatClass);

        // Query server-side theo fromCityId
        Query query = dbFlights.orderByChild("fromCityId").equalTo(fromCityId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Flight> result = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Flight flight = child.getValue(Flight.class);
                    if (flight == null) continue;

                    // LỌC THEO TIÊU CHÍ CƠ BẢN

                    // 1.1. Lọc toCityId
                    if (!toCityId.equals(flight.getToCityId())) continue;

                    // 1.2. Lọc departureDate
                    if (departureDate != null && !departureDate.isEmpty()) {
                        if (!departureDate.equals(flight.getDepartureDate())) continue;
                    }

                    // 1.3. Kiểm tra availableSeats
                    if (flight.getAvailableSeats() < totalPassengers) continue;

                    // LỌC THEO HẠNG GHẾ (fareOptions) để tìm hạng vé

                    List<FareOption> fareOptions = flight.getFareOptions();
                    if (fareOptions == null || fareOptions.isEmpty()) continue;

                    //  Lọc theo status
                    if (!"ON_TIME".equalsIgnoreCase(flight.getStatus())) {
                        continue;
                    }

                    // Tìm giá fareOption với giá rẻ nhất và  trong fareOptions phù hợp với seatType
                    FareOption cheapestOption = findCheapestOptionForSeatType(fareOptions, targetSeatType);
                    // Nếu không tìm thấy gói nào phù hợp với hạng ghế  skip
                    if (cheapestOption == null) continue;

                    // JOIN THÔNG TIN CITY / AIRLINE

                    City fromCity = citiesMap.get(flight.getFromCityId());
                    City toCity   = citiesMap.get(flight.getToCityId());

                    if (fromCity != null) {
                        flight.setFrom(fromCity.getCityName());
                        flight.setFromIata(fromCity.getIataCode());
                    }
                    if (toCity != null) {
                        flight.setTo(toCity.getCityName());
                        flight.setToIata(toCity.getIataCode());
                    }

                    Airline airline = airlinesMap.get(flight.getAirlineId());
                    if (airline != null) {
                        flight.setAirlineName(airline.getName());
                        flight.setAirlineLogo(airline.getLogo());
                    }

                    // 2. GẮN GIÁ HIỂN THỊ CHÍNH XÁC
                    flight.setDisplayPrice(cheapestOption.getBasePrice());

                    // 3. GẮN TÊN HẠNG VÉ CHÍNH XÁC TỪ OPTION ĐÃ CHỌN
                    FareClass selectedFareClass = fareClassesMap.get(cheapestOption.getFareClassId());

                    if (selectedFareClass != null) {
                        // Gán tên hiển thị cụ thể (Ví dụ: "Phổ thông Tiết kiệm" hoặc "Eco Tiêu chuẩn")
                        flight.setFareClassName(selectedFareClass.getTitle());

                    }
                    result.add(flight);
                }

                callback.onLoaded(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Tìm giá rẻ nhất trong fareOptions phù hợp với seatType.
     *
     * @param fareOptions  Danh sách gói vé của chuyến bay
     * @param targetSeatType Loại ghế cần tìm ("ECONOMY" / "BUSINESS")
     * @return Giá rẻ nhất, hoặc 0.0 nếu không tìm thấy
     */
    private FareOption findCheapestOptionForSeatType(List<FareOption> fareOptions, String targetSeatType) {
        FareOption cheapestOption = null;
        double minPrice = Double.MAX_VALUE;

        for (FareOption option : fareOptions) {
            // Kiểm tra còn chỗ không
            if (!option.isAvailable()) continue;

            // Lấy FareClass để biết seatType
            FareClass fareClass = fareClassesMap.get(option.getFareClassId());
            if (fareClass == null) continue;

            // Kiểm tra seatType có khớp không
            if (!targetSeatType.equalsIgnoreCase(fareClass.getSeatType())) continue;

            // Lấy giá rẻ nhất
            if (option.getBasePrice() < minPrice) {
                minPrice = option.getBasePrice();
                cheapestOption = option;
            }
        }

        return cheapestOption;
    }

    /**
     * Map từ seatClass (user input) sang seatType (database value).
     *
     * @param seatClass "Phổ thông", "Thương gia", "Hạng nhất"
     * @return "ECONOMY", "BUSINESS", "FIRST_CLASS"
     */
    private String mapSeatClassToType(String seatClass) {
        if (seatClass == null) return "ECONOMY";

        switch (seatClass.toLowerCase().trim()) {
            case "phổ thông":
                return "ECONOMY";

            case "phổ thông đặc biệt":
                return "PREMIUM_ECONOMY";

            case "thương gia":
                return "BUSINESS";

            default:
                return "ECONOMY";
        }
    }

    /**
     * Clear cache khi cần refresh.
     */
    public void clearCache() {
        citiesMap.clear();
        airlinesMap.clear();
        fareClassesMap.clear();
    }
}