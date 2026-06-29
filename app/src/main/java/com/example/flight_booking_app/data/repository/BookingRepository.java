package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

public class BookingRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration detailListenerReg;
    private ListenerRegistration listListenerReg;

    // Interface cho đơn hàng chi tiết
    public interface OnBookingDetailLoadedCallback {
        void onSuccess(Booking booking);
        void onFailure(String errorMessage);
    }

    // Interface cho danh sách đơn hàng
    public interface OnBookingListLoadedCallback {
        void onSuccess(List<Booking> bookings);
        void onFailure(String errorMessage);
    }

    public interface BookingIdCallback {
        void onSuccess(String bookingId);
        void onFailure(String errorMessage);
    }

    // interface để trả về cập nhật trạng thái đơn hàng
    public interface OnStatusResultCallback {
        void onSuccess();
        void onError(String error);
    }

    public void createBookingWithNotification(Booking booking, String userId, BookingIdCallback callback) {
        // 1. Tạo DocumentReference trống cho đơn hàng để lấy ID tự sinh
        DocumentReference bookingRef = db.collection("bookings").document();
        String generatedBookingId = bookingRef.getId();
        booking.setBookingId(generatedBookingId);

        // 2. Tạo DocumentReference trong bảng notifications độc lập ở ngoài Root
        DocumentReference notifRef = db.collection("notifications").document();

        // 3. Đóng gói Object Thông báo
        Notification notification = new Notification();
        notification.setNotificationId(notifRef.getId());
        notification.setBookingId(generatedBookingId); // Khóa ngoại liên kết sang Booking
        notification.setUserId(userId);                 // Khóa ngoại liên kết sang User chủ đơn

        notification.setTitle("Đã giữ chỗ thành công 👌 #" + booking.getOrderCode());

        // Tự động tối ưu chuỗi hiển thị theo hành trình Một chiều hay Khứ hồi
        String bodyMessage;
        if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
            bodyMessage = "Chuyến bay đi " + booking.getOutboundFlight().getToCityId()
                    + " và về " + booking.getReturnFlight().getToCityId()
                    + " đã được giữ chỗ thành công. Nhấn để xem chi tiết!";
        } else {
            bodyMessage = "Chuyến bay đi " + booking.getOutboundFlight().getToCityId()
                    + " đã được giữ chỗ thành công. Nhấn để xem chi tiết!";
        }
        notification.setBody(bodyMessage);

        notification.setCreatedAt(Timestamp.now());
        notification.setRead(false);
        notification.setType("RESERVATION_SUCCESS");

        // 4. Dùng WriteBatch đẩy lên 2 bảng độc lập cùng một lúc (Atomically)
        WriteBatch batch = db.batch();
        batch.set(bookingRef, booking);
        batch.set(notifRef, notification);

        // 5. Commit dữ liệu
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Thành công trả về bookingId để Activity chuyển sang màn OrderDetail
                    callback.onSuccess(generatedBookingId);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }


    // Lắng nghe realtime chi tiết đơn hàng
    public void observeBookingDetail(String bookingId, OnBookingDetailLoadedCallback callback) {
        if (detailListenerReg != null) detailListenerReg.remove();

        detailListenerReg = db.collection("bookings")
                .document(bookingId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) { callback.onFailure(error.getMessage()); return; }
                    if (snapshot != null && snapshot.exists()) {
                        callback.onSuccess(snapshot.toObject(Booking.class));
                    }
                });
    }

    // lắng nghe realtime danh sách đơn hàng BookingFragment
    public void observeBookingList(String userId, OnBookingListLoadedCallback callback) {
        if (listListenerReg != null) listListenerReg.remove();

        listListenerReg = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {
                        // Trả về cả list luôn
                        callback.onSuccess(querySnapshot.toObjects(Booking.class));
                    }
                });
    }

    // Giải phóng bộ nhớ
    public void removeObservers() {
        if (detailListenerReg != null) { detailListenerReg.remove(); detailListenerReg = null; }
        if (listListenerReg != null) { listListenerReg.remove(); listListenerReg = null; }
    }

    // Update status
    public void updateBookingStatus(String bookingId, String newStatus, OnStatusResultCallback callback) {
        if (bookingId == null || bookingId.isEmpty()) {
            callback.onError("Booking ID không hợp lệ");
            return;
        }

        db.collection("bookings")
                .document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


//    public void updateBookingCode(String bookingId, String bookingCode, OnStatusResultCallback callback) {
//        db.collection("bookings")
//                .document(bookingId)
//                .update("bookingCode", bookingCode)
//                .addOnSuccessListener(unused -> callback.onSuccess())
//                .addOnFailureListener(e -> callback.onError(e.getMessage()));
//    }


    // Dùng cho bất kỳ trường hợp nào cần update nhiều field cùng lúc
    public void updateBookingFields(String bookingId, Map<String, Object> fields,
                                    OnStatusResultCallback callback) {
        if (bookingId == null || bookingId.isEmpty()) {
            callback.onError("Booking ID không hợp lệ");
            return;
        }
        db.collection("bookings")
                .document(bookingId)
                .update(fields)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}