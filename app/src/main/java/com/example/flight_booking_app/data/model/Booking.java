package com.example.flight_booking_app.data.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

public class Booking implements Serializable {
    private String bookingId;
    private String userId;
    private String orderCode;
    private String bookingCode; // Mã đặt chỗ
    private String status; // RESERVATION_SUCCESS, RESERVATION_FAILED, PAYMENT_EXPIRED, PAYMENT_SUCCESS
    private double totalAmount;

    // Firestore sử dụng lớp com.google.firebase.Timestamp để quản lý thời gian cực chuẩn
    private Timestamp createdAt;
    private Timestamp paymentDeadline;

    private String outboundFlightId;
    private String returnFlightId; // Sẽ là null nếu đi một chiều
    private String outboundFareClassId; // Mã hạng vé lượt đi (Ví dụ: "ECO_STANDARD")
    private String returnFareClassId;   // Mã hạng vé lượt về (Ví dụ: "BUS_BUSINESS", null nếu đi 1 chiều)

    // ── BỔ SUNG CÁC TRƯỜNG TÓM TẮT ĐỂ HIỂN THỊ NHANH (DENORMALIZATION)
    private String departureCity;       // Tên thành phố đi (Ví dụ: "Hà Nội")
    private String arrivalCity;         // Tên thành phố đến (Ví dụ: "Hồ Chí Minh")
    private Timestamp departureTime; // Thời gian cất cánh lượt đi (Dùng kiểu Timestamp của Firebase)
    private Timestamp arrivalTime; // Thời gian đến Timestamp sắp xếp danh sách đơn hàng mới/cũ cho chuẩn.

    // Firestore sẽ tự động map mảng các Map thành List các Object Passenger
    private List<Passenger> passengers;

    // Constructor bắt buộc phải có cho Firestore (.toObject(Booking.class))
    public Booking() {
    }

    // Constructor đầy đủ để bạn tiện khởi tạo nhanh khi tạo đơn hàng


    // ── GETTERS VÀ SETTERS ──
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getPaymentDeadline() { return paymentDeadline; }
    public void setPaymentDeadline(Timestamp paymentDeadline) { this.paymentDeadline = paymentDeadline; }

    public String getOutboundFlightId() { return outboundFlightId; }
    public void setOutboundFlightId(String outboundFlightId) { this.outboundFlightId = outboundFlightId; }

    public String getReturnFlightId() { return returnFlightId; }
    public void setReturnFlightId(String returnFlightId) { this.returnFlightId = returnFlightId; }


    public String getOutboundFareClassId() {
        return outboundFareClassId;
    }

    public void setOutboundFareClassId(String outboundFareClassId) {
        this.outboundFareClassId = outboundFareClassId;
    }

    public String getReturnFareClassId() {
        return returnFareClassId;
    }

    public void setReturnFareClassId(String returnFareClassId) {
        this.returnFareClassId = returnFareClassId;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public Timestamp getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
    }

    public Timestamp getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Timestamp arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public List<Passenger> getPassengers() { return passengers; }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }
}