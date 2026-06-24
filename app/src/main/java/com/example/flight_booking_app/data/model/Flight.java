package com.example.flight_booking_app.data.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Flight implements Serializable {
    private String flightId;
    private String flightNumber;
    private String airlineId;
    private String aircraftId;
    private String fromCityId;
    private String toCityId;

    private String departureDate;
    private String departureTime;
    private String arrivalDate;
    private String arrivalTime;
    private String duration;

    private int availableSeats;

    private String selectedSeatClass;
    private int checkedBaggage;
    private double taxFee;
    private double totalPrice;

    // TRƯỜNG MỚI: Cần thiết vì Repository đang dùng .orderBy("minPrice")
    private long minPrice;

    private List<FareOption> fareOptions = new ArrayList<>();
    private String seatMapId;
    private String status;

    private String from;         // Tương ứng "fromCity" trên Firestore
    private String fromIata;
    private String to;           // Tương ứng "toCity" trên Firestore
    private String toIata;
    private String airlineName;
    private String airCraftName;
    private String airlineLogo;


    @Exclude
    private int adultCount = 1;

    @Exclude
    private int childCount = 0;

    @Exclude
    private int babyCount = 0;

    @Exclude
    private String seatType;

    @Exclude
    private FareClass selectedFareClass;

    @Exclude
    private double displayPrice;

    // Bắt buộc phải có Constructor rỗng cho Firestore
    public Flight() {
    }

    public long getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
    }

    public FareClass getSelectedFareClass() {
        return selectedFareClass;
    }

    public void setSelectedFareClass(FareClass selectedFareClass) {
        this.selectedFareClass = selectedFareClass;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String v) {
        flightId = v;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String v) {
        flightNumber = v;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String v) {
        airlineId = v;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(String v) {
        aircraftId = v;
    }

    public String getFromCityId() {
        return fromCityId;
    }

    public void setFromCityId(String v) {
        fromCityId = v;
    }

    public String getToCityId() {
        return toCityId;
    }

    public void setToCityId(String v) {
        toCityId = v;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String v) {
        departureDate = v;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String v) {
        departureTime = v;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String v) {
        arrivalDate = v;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String v) {
        arrivalTime = v;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String v) {
        duration = v;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int v) {
        availableSeats = v;
    }

    public int getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(int v) {
        adultCount = v;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int v) {
        childCount = v;
    }

    public int getBabyCount() {
        return babyCount;
    }

    public void setBabyCount(int v) {
        babyCount = v;
    }

    public int getCheckedBaggage() {
        return checkedBaggage;
    }

    public void setCheckedBaggage(int v) {
        checkedBaggage = v;
    }

    public double getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(double v) {
        taxFee = v;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double v) {
        totalPrice = v;
    }

    public List<FareOption> getFareOptions() {
        return fareOptions;
    }

    public void setFareOptions(List<FareOption> v) {
        fareOptions = v;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String v) {
        from = v;
    }

    public String getFromIata() {
        return fromIata;
    }

    public void setFromIata(String v) {
        fromIata = v;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String v) {
        to = v;
    }

    public String getToIata() {
        return toIata;
    }

    public void setToIata(String v) {
        toIata = v;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String v) {
        airlineName = v;
    }

    public String getAirlineLogo() {
        return airlineLogo;
    }

    public void setAirlineLogo(String v) {
        airlineLogo = v;
    }

    public double getDisplayPrice() {
        return displayPrice;
    }

    public void setDisplayPrice(double v) {
        displayPrice = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSelectedSeatClass() {
        return selectedSeatClass;
    }

    public void setSelectedSeatClass(String selectedSeatClass) {
        this.selectedSeatClass = selectedSeatClass;
    }

    public String getSeatMapId() {
        return seatMapId;
    }

    public void setSeatMapId(String seatMapId) {
        this.seatMapId = seatMapId;
    }

    public String getAirCraftName() {
        return airCraftName;
    }

    public void setAirCraftName(String airCraftName) {
        this.airCraftName = airCraftName;
    }
}