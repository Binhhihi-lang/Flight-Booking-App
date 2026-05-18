package com.example.flight_booking_app.data.model;

import java.io.Serializable;

public class FareClass implements Serializable {
    private String fareClassId; // Mã định danh thống nhất: ECO_SAVE, ECO_STANDARD, ECO_FLEX, BIZ_STANDARD, BIZ_FLEX
    private String airlineId;    // Thuộc hãng nào
    private String title;        // Tên hiển thị: Thương gia linh hoạt, Phổ thông Tiết kiệm
    private String seatType;     // Loại ghế vật lý: BUSINESS, ECONOMY, FIRST_CLASS

    // Liên kết với điều kiện (FareRule)
    private String fareRuleId;

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

    public String getFareRuleId() {
        return fareRuleId;
    }

    public void setFareRuleId(String fareRuleId) {
        this.fareRuleId = fareRuleId;
    }
}
