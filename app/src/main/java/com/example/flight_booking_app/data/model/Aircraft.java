package com.example.flight_booking_app.data.model;

public class Aircraft {
    private String airCraftId;
    private String airlineId;
    private String modelName;  // boeing 737, ...
    private int totalSeats;
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
}
