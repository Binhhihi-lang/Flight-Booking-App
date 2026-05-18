package com.example.flight_booking_app.data.model;

public class Aircraft {
    private String airCraftId;
    private String airlineId;
    private String modelName;
    private int totalSeats;
    private String seatMapId;  // mã ghế

    public Aircraft() {}

    public String getAirCraftId() {
        return airCraftId;
    }

    public void setAirCraftId(String airCraftId) {
        this.airCraftId = airCraftId;
    }

    public String getAirlineId() { return airlineId; }
    public void setAirlineId(String airlineId) { this.airlineId = airlineId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public String getSeatMapId() { return seatMapId; }
    public void setSeatMapId(String seatMapId) { this.seatMapId = seatMapId; }
}
