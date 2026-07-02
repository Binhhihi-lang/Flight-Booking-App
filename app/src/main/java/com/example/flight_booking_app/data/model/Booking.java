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

    // lồng Object
    private Flight outboundFlight;
    private FareClass outboundFare;
    private Flight returnFlight;
    private FareClass returnFare;

    private boolean isRoundTrip;

    // Firestore sẽ tự động map mảng các Map thành List các Object Passenger
    private List<Passenger> passengers;

    private String contactName;
    private String contactEmail;
    private String contactPhone;

    // Constructor bắt buộc phải có cho Firestore (.toObject(Booking.class))
    public Booking() {
    }

    // Constructor đầy đủ để bạn tiện khởi tạo nhanh khi tạo đơn hàng


    // ── GETTERS VÀ SETTERS ──
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

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(Timestamp paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public boolean isRoundTrip() {
        return isRoundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        isRoundTrip = roundTrip;
    }

    public Flight getOutboundFlight() {
        return outboundFlight;
    }

    public void setOutboundFlight(Flight outboundFlight) {
        this.outboundFlight = outboundFlight;
    }

    public FareClass getOutboundFare() {
        return outboundFare;
    }

    public void setOutboundFare(FareClass outboundFare) {
        this.outboundFare = outboundFare;
    }

    public Flight getReturnFlight() {
        return returnFlight;
    }

    public void setReturnFlight(Flight returnFlight) {
        this.returnFlight = returnFlight;
    }

    public FareClass getReturnFare() {
        return returnFare;
    }

    public void setReturnFare(FareClass returnFare) {
        this.returnFare = returnFare;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}