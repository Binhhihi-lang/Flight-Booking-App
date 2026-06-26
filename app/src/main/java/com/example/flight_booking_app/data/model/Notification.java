package com.example.flight_booking_app.data.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Notification implements Serializable {
    private String notificationId; // ID của Document thông báo trên Firestore
    private String title;          // Tiêu đề thông báo (Ví dụ: "Đã giữ chỗ thành công 👌 #570507")
    private String body;           // Nội dung chi tiết thông báo

    // Đổi từ long sang Timestamp để tối ưu hóa và đồng bộ với cấu trúc Firestore
    private Timestamp timestamp;

    private String bookingId;      // Mã ID của đơn hàng liên kết (Dùng để mở OrderDetailActivity)
    private boolean isRead;        // Trạng thái đã đọc hay chưa (true/false)
    private String type;           // Loại thông báo (Ví dụ: "BOOKING_SUCCESS", "EXPIRED", "PROMOTION")

    public Notification() {
    }

    public Notification(String notificationId, String title, String body, Timestamp timestamp,
                        String bookingId, boolean isRead, String type) {
        this.notificationId = notificationId;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.bookingId = bookingId;
        this.isRead = isRead;
        this.type = type;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}