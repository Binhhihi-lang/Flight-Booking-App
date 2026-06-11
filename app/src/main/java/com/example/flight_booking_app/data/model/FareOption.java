package com.example.flight_booking_app.data.model;


import java.io.Serializable;

public class FareOption implements Serializable {
    private String fareClassId; // Tham chiếu đến FareClass.id (ECO_SAVE, ECO_STANDARD...)
    private int seatLimit;      // Số lượng vé tối đa được mở bán cho gói này (Ví dụ: Vé Eco_Save chỉ mở bán 10 vé giá rẻ)
    private int bookedCount;    // Số lượng vé của gói này đã được mua

    public FareOption() {}

    public FareOption(String fareClassId, int seatLimit, int bookedCount) {
        this.fareClassId = fareClassId;
        this.seatLimit = seatLimit;
        this.bookedCount = bookedCount;
    }

    // Kiểm tra xem gói vé này còn chỗ bán không
    public boolean isAvailable() {
        return bookedCount < seatLimit;
    }

    // Getters and Setters
    public String getFareClassId() { return fareClassId; }
    public void setFareClassId(String fareClassId) { this.fareClassId = fareClassId; }
    public int getSeatLimit() { return seatLimit; }
    public void setSeatLimit(int seatLimit) { this.seatLimit = seatLimit; }
    public int getBookedCount() { return bookedCount; }
    public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }
}