package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class BookingRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface BookingCallback {
        void onSuccess(String bookingId);
        void onFailure(String errorMessage);
    }

    public void createBookingWithNotification(Booking booking, String userId, BookingCallback callback) {
        // 1. Tạo DocumentReference trống để lấy ID tự sinh từ Firestore
        DocumentReference bookingRef = db.collection("bookings").document();
        String generatedBookingId = bookingRef.getId();

        // 2. Gán cái ID tự sinh đó vào Object Booking
        booking.setBookingId(generatedBookingId);

        // 3. Tạo Object Thông báo (Notification)
        DocumentReference notifRef = db.collection("users").document(userId)
                .collection("notifications").document();

        Notification notification = new Notification();
        notification.setNotificationId(notifRef.getId());
        notification.setBookingId(generatedBookingId); // Liên kết khóa ngoại
        notification.setTitle("Đã giữ chỗ thành công \uD83D\uDC4C #" + booking.getOrderCode());
        notification.setBody("Chuyến bay đi " + booking.getArrivalCity() + " đã được giữ chỗ. Nhấn để xem chi tiết!");
        notification.setTimestamp(Timestamp.now());
        notification.setRead(false);
        notification.setType("RESERVATION_SUCCESS");

        // 4. Dùng WriteBatch để đẩy Đơn hàng và Thông báo lên cùng một lúc
        WriteBatch batch = db.batch();
        batch.set(bookingRef, booking);
        batch.set(notifRef, notification);

        // 5. Commit dữ liệu lên server
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Trả về bookingId để UI chuyển màn hình
                    callback.onSuccess(generatedBookingId);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}