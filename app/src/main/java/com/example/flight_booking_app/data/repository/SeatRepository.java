package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.data.model.SeatMapMetadata;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SeatRepository {

    private final FirebaseFirestore db;

    public interface OnSeatsLoadedListener {
        void onLoaded(List<Seat> seats, SeatMapMetadata metadata);
        void onError(String error);
    }
    public interface OnUpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    public SeatRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Tải sơ đồ ghế cho một chuyến bay.
     *
     * Firestore structure:
     *   seatMapTemplates/{templateId}          → metadata, modelName
     *   seatMapTemplates/{templateId}/seats/*  → Seat (khung xương)
     *   flightSeats/{flightId}/seats/*         → { status, passengerId } (override)
     *
     * Logic giống cũ: fetch template + flightSeats song song, merge lại.
     */
    public void fetchSeatsForFlight(String templateId, String flightId,
                                    OnSeatsLoadedListener listener) {

        // Dùng AtomicInteger để biết khi nào cả 2 fetch song song đều xong
        final AtomicInteger latch = new AtomicInteger(2);
        final List<Seat>[] templateSeatsHolder = new List[]{null};
        final SeatMapMetadata[] metadataHolder  = new SeatMapMetadata[]{null};
        final Map<String, Seat>[] overrideHolder = new Map[]{null};
        final String[] errorHolder = new String[]{null};

        Runnable tryMerge = () -> {
            if (latch.decrementAndGet() != 0) return; // espera ao outro fetch

            if (errorHolder[0] != null) {
                listener.onError(errorHolder[0]);
                return;
            }

            List<Seat> templateSeats = templateSeatsHolder[0];
            Map<String, Seat> overrideMap = overrideHolder[0];

            if (templateSeats == null) {
                listener.onError("Không tìm thấy template ghế: " + templateId);
                return;
            }

            // Merge: ghi đè status/passengerId từ flightSeats lên template
            if (overrideMap != null && !overrideMap.isEmpty()) {
                for (Seat seat : templateSeats) {
                    Seat override = overrideMap.get(seat.getSeatNumber());
                    if (override != null) {
                        seat.setStatus(override.getStatus());
                        seat.setPassengerId(override.getPassengerId());
                    }
                }
            }

            listener.onLoaded(templateSeats, metadataHolder[0]);
        };

        // ── FETCH 1: Template metadata + seats subcollection ─────────────────

        // Lấy metadata từ document chính
        db.collection("seatMapTemplates")
                .document(templateId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        SeatMapMetadata meta = new SeatMapMetadata();
                        Map<String, Object> metaMap =
                                (Map<String, Object>) doc.get("metadata");
                        if (metaMap != null) {
                            Long span = (Long) metaMap.get("spanCount");
                            if (span != null) meta.spanCount = span.intValue();
                            meta.columns = (List<String>) metaMap.get("columns");
                            List<Long> aislesRaw = (List<Long>) metaMap.get("aisles");
                            if (aislesRaw != null) {
                                List<Integer> aisles = new ArrayList<>();
                                for (Long a : aislesRaw) aisles.add(a.intValue());
                                meta.aisles = aisles;
                            }
                        }
                        metadataHolder[0] = meta;
                    }

                    // Lấy subcollection seats
                    db.collection("seatMapTemplates")
                            .document(templateId)
                            .collection("seats")
                            .get()
                            .addOnSuccessListener(seatsSnap -> {
                                List<Seat> seats = new ArrayList<>();
                                for (DocumentSnapshot seatDoc : seatsSnap.getDocuments()) {
                                    Seat seat = parseSeat(seatDoc);
                                    if (seat != null) {
                                        seat.setStatus("AVAILABLE"); // reset về AVAILABLE trước khi merge
                                        seats.add(seat);
                                    }
                                }
                                templateSeatsHolder[0] = seats;
                                tryMerge.run();
                            })
                            .addOnFailureListener(e -> {
                                errorHolder[0] = "Lỗi tải ghế template: " + e.getMessage();
                                tryMerge.run();
                            });
                })
                .addOnFailureListener(e -> {
                    errorHolder[0] = "Lỗi tải template: " + e.getMessage();
                    // Vẫn gọi tryMerge 2 lần để latch về 0
                    latch.decrementAndGet(); // bù cho fetch seats bên trong không chạy
                    tryMerge.run();
                });

        // ── FETCH 2: FlightSeats override (song song với fetch 1) ────────────

        db.collection("flightSeats")
                .document(flightId)
                .collection("seats")
                .get()
                .addOnSuccessListener(overrideSnap -> {
                    Map<String, Seat> overrideMap = new HashMap<>();
                    for (DocumentSnapshot doc : overrideSnap.getDocuments()) {
                        // doc.getId() = seatNumber (VD: "12C")
                        String seatNumber = doc.getId();
                        String status      = doc.getString("status");
                        String passengerId = doc.getString("passengerId");
                        Seat s = new Seat();
                        s.setSeatNumber(seatNumber);
                        s.setStatus(status != null ? status : "AVAILABLE");
                        s.setPassengerId(passengerId != null ? passengerId : "");
                        overrideMap.put(seatNumber, s);
                    }
                    overrideHolder[0] = overrideMap;
                    tryMerge.run();
                })
                .addOnFailureListener(e -> {
                    // FlightSeats không có → chuyến bay chưa có ghế nào bị đặt, vẫn ổn
                    overrideHolder[0] = new HashMap<>();
                    tryMerge.run();
                });
    }

    /**
     * Khóa nhiều ghế cùng lúc. Nếu 1 ghế lỗi, hủy toàn bộ.
     */
    public void reserveMultipleSeatsWithTransaction(String flightId, List<String> seatNumbers, String currentUserId, OnUpdateCallback callback) {
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    List<DocumentReference> seatRefs = new ArrayList<>();
                    List<DocumentSnapshot> snapshots = new ArrayList<>();

                    // đọc dữ liệu của bảng flightSeat
                    for (String seatNumber : seatNumbers) {
                        DocumentReference ref = db.collection("flightSeats")
                                .document(flightId)
                                .collection("seats")
                                .document(seatNumber);
                        seatRefs.add(ref);
                        snapshots.add(transaction.get(ref)); // Đọc dữ liệu từ Server
                    }

                    long currentTime = System.currentTimeMillis();

                    // Kiểm tra điều kiện của từng ghế
                    for (int i = 0; i < snapshots.size(); i++) {
                        DocumentSnapshot snapshot = snapshots.get(i);
                        String seatNumber = seatNumbers.get(i);

                        if (snapshot.exists()) {
                            String currentStatus = snapshot.getString("status");
                            Timestamp holdUntil = snapshot.getTimestamp("holdUntil");

                            if ("SOLD".equals(currentStatus) || "BOOKED".equals(currentStatus)) {
                                throw new FirebaseFirestoreException("Ghế " + seatNumber + " đã được bán mất rồi!",
                                        FirebaseFirestoreException.Code.ABORTED);
                            }

                            if ("HOLD".equals(currentStatus) && holdUntil != null && holdUntil.toDate().getTime() > currentTime) {
                                String holdingUser = snapshot.getString("passengerId");
                                if (!currentUserId.equals(holdingUser)) {
                                    throw new FirebaseFirestoreException("Ghế " + seatNumber + " đang có người khác giữ chỗ.",
                                            FirebaseFirestoreException.Code.ABORTED);
                                }
                            }
                        }
                    }

                    // Nếu tất cả các ghế trống thì tiến hành giữ chỗ
                    long holdDuration = 15 * 60 * 1000; // 15 phút
                    Timestamp holdUntilTime = new Timestamp(new Date(currentTime + holdDuration));

                    for (DocumentReference ref : seatRefs) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("status", "HOLD");
                        data.put("passengerId", currentUserId); // Lưu ID người giữ để họ có thể thanh toán tiếp
                        data.put("holdUntil", holdUntilTime);

                        transaction.set(ref, data);
                    }

                    return null; // Thành công!

                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    //  xóa hẳn document, flightSeats chỉ còn ghế HOLD/BOOKED
    public void releaseSeats(String flightId, List<String> seatNumbers, OnUpdateCallback callback) {
        WriteBatch batch = db.batch();
        for (String seatNumber : seatNumbers) {
            DocumentReference ref = db.collection("flightSeats")
                    .document(flightId)
                    .collection("seats")
                    .document(seatNumber);
            batch.delete(ref);
        }
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    /**
     * Cập nhật trạng thái một ghế khi hành khách đặt/huỷ.
     * Ghi vào flightSeats/{flightId}/seats/{seatNumber}
     */

    /**
     * Xác nhận thanh toán thành công: đổi ghế từ HOLD → BOOKED
     * Dùng WriteBatch để ghi tất cả cùng lúc
     */
    public void confirmSeats(String flightId, List<String> seatNumbers,
                             String passengerId, OnUpdateCallback callback) {
        WriteBatch batch = db.batch();

        for (String seatNumber : seatNumbers) {
            DocumentReference ref = db.collection("flightSeats")
                    .document(flightId)
                    .collection("seats")
                    .document(seatNumber);

            Map<String, Object> data = new HashMap<>();
            data.put("status", "BOOKED");
            data.put("passengerId", passengerId);
            data.put("holdUntil", null); // Xóa thời hạn giữ chỗ
            batch.set(ref, data);
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private Seat parseSeat(DocumentSnapshot doc) {
        if (!doc.exists()) return null;
        Seat seat = new Seat();
        seat.setSeatId("seat_" + doc.getId());
        seat.setSeatNumber(doc.getString("seatNumber") != null
                ? doc.getString("seatNumber") : doc.getId());
        Long row = doc.getLong("row");
        if (row != null) seat.setRow(row.intValue());
        seat.setColumn(doc.getString("column"));
        seat.setType(doc.getString("type"));
        Double price = doc.getDouble("price");
        if (price != null) seat.setPrice(price);
        seat.setStatus(doc.getString("status") != null ? doc.getString("status") : "AVAILABLE");
        return seat;
    }


}