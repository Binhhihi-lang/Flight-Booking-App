package com.example.flight_booking_app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Notification implements Serializable {
    private String notificationId; // ID của Document thông báo trên Firestore
    private String title;          // Tiêu đề thông báo (Ví dụ: "Đã giữ chỗ thành công 👌 #570507")
    private String body;           // Nội dung chi tiết thông báo

    private Timestamp createdAt; // Ngày tạo thông báo

    private String bookingId;      // Mã ID của đơn hàng liên kết (Dùng để mở OrderDetailActivity)
    private String userId;
    private boolean read;        // Trạng thái đã đọc hay chưa (true/false)
    private String type;           // Loại thông báo (Ví dụ: "BOOKING_SUCCESS", "EXPIRED", "PROMOTION")

    public Notification() {
    }

    public Notification(String notificationId, String title, String body, Timestamp createdAt, String bookingId, String userId, boolean isRead, String type) {
        this.notificationId = notificationId;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.bookingId = bookingId;
        this.userId = userId;
        this.read = isRead;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}