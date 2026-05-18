package com.example.flight_booking_app.data.model;

public class Airline {
    private String airlineId;
    private String name;
    private String logo;
    private String iataCode;

    public Airline() {}

    public Airline(String airlineId, String name, String logo, String iataCode) {
        this.airlineId = airlineId;
        this.name = name;
        this.logo = logo;
        this.iataCode = iataCode;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String airlineId) {
        this.airlineId = airlineId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getIataCode() { return iataCode; }
    public void setIataCode(String iataCode) { this.iataCode = iataCode; }
}
