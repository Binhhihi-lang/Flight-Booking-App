package com.example.flight_booking_app.data.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FareClass implements Serializable {
    private String fareClassId; // Mã định danh thống nhất: ECO_SAVE, ECO_STANDARD, ECO_FLEX, BIZ_STANDARD, BIZ_FLEX
    private String airlineId;    // Thuộc hãng nào
    private String title;        // Tên hiển thị: Thương gia linh hoạt, Phổ thông Tiết kiệm
    private String seatType;     // Loại ghế vật lý: BUSINESS, ECONOMY, PREMIUM_ECONOMY

    private double basePrice;   // Giá vé gốc áp dụng riêng cho gói này trên chuyến bay này

    // ── KIẾN TRÚC HYBRID ──
    private String fareRuleId; // Lưu ID để làm việc với DB/Web Admin/Booking
    private List<String> baggageOptionIds = new ArrayList<>();

    @Exclude
    private FareRule fareRule; // Object được "nhồi" vào để UI hiển thị (Bỏ qua khi lưu lên DB)

    @Exclude
    private ArrayList<BaggageOption> baggageOptions = new ArrayList<>();


    public FareClass() {
    }

    public FareClass(String fareClassId, String airlineId, String title, String seatType, String fareRuleId) {
        this.fareClassId = fareClassId;
        this.airlineId = airlineId;
        this.title = title;
        this.seatType = seatType;
        this.fareRuleId = fareRuleId;
    }

    public String getFareClassId() {
        return fareClassId;
    }

    public void setFareClassId(String fareClassId) {
        this.fareClassId = fareClassId;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String airlineId) {
        this.airlineId = airlineId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public String getFareRuleId() {
        return fareRuleId;
    }
    public void setFareRuleId(String fareRuleId) {
        this.fareRuleId = fareRuleId;
    }

    public ArrayList<BaggageOption> getBaggageOptions() {
        return baggageOptions;
    }

    public void setBaggageOptions(ArrayList<BaggageOption> baggageOptions) {
        this.baggageOptions = baggageOptions;
    }

    public List<String> getBaggageOptionIds() {
        return baggageOptionIds;
    }

    public void setBaggageOptionIds(List<String> baggageOptionIds) {
        this.baggageOptionIds = baggageOptionIds;
    }

    public FareRule getFareRule() { return fareRule; }
    public void setFareRule(FareRule fareRule) { this.fareRule = fareRule; }
}
