package com.example.flight_booking_app.data.model;


public class City {
    private String cityId;
    private String cityName;
    private String airportName;
    private String iataCode;

    public City() {}

    public City(String id, String cityName, String airportName, String iataCode) {
        this.cityId = id;
        this.cityName = cityName;
        this.airportName = airportName;
        this.iataCode = iataCode;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName()    { return cityName; }
    public String getAirportName() { return airportName; }
    public String getIataCode()    { return iataCode; }

    public void setCityName(String cityName)       { this.cityName = cityName; }
    public void setAirportName(String airportName) { this.airportName = airportName; }
    public void setIataCode(String iataCode)       { this.iataCode = iataCode; }
}
