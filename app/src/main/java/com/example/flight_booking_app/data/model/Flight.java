package com.example.flight_booking_app.data.model;


import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Flight lưu foreign keys (fromCityId, toCityId, airlineId) thay vì dữ liệu thô.
 * Sau khi Repository JOIN với City và Airline:
 * → các transient field được điền để Adapter hiển thị.
 * <p>
 * Transient field KHÔNG lưu lên Firebase (Firebase bỏ qua transient).
 */
public class Flight implements Serializable {
    private String flightId;
    private String flightNumber;
    private String airlineId;    // FK → Airlines
    private String aircraftId;
    private String fromCityId;   // FK → Cities
    private String toCityId;     // FK → Cities

    private String departureDate;
    private String departureTime;
    private String arrivalDate;
    private String arrivalTime;
    private String duration;

    private int availableSeats;
    private int adultCount = 1;
    private int childCount = 0;
    private int babyCount = 0;

    private String selectedSeatClass;
    private int checkedBaggage; // (Số kg hành lý ký gửi
    private double taxFee;
    private double totalPrice;

    private List<FareOption> fareOptions = new ArrayList<>();
    private String seatMapId;

    private String status;

    // Transient display fields (KHÔNG lưu Firebase, điền sau JOIN)
    // Repository sẽ set các field này sau khi JOIN City + Airline
    // Đổi 'transient' thành '@Exclude' của Firebase để Intent không bị mất dữ liệu khi truyền Intent
    // Không lưu trong csdl

    @Exclude
    private String from;         // tên thành phố đi   "Hà Nội"
    @Exclude
    private String fromIata;     // mã IATA điểm đi    "HAN"
    @Exclude
    private String to;           // tên thành phố đến
    @Exclude
    private String toIata;       // mã IATA điểm đến
    @Exclude
    private String airlineName;  // tên hãng            "Vietnam Airlines"
    @Exclude
    private String airCraftName;
    @Exclude
    private String airlineLogo;  // URL logo hãng
    @Exclude
    private String seatType;     // loại hạng ghế để lọc

    private transient FareClass selectedFareClass; // Dùng để lưu cấu hình hạng vé phục vụ UI

    public FareClass getSelectedFareClass() { return selectedFareClass; }
    public void setSelectedFareClass(FareClass selectedFareClass) { this.selectedFareClass = selectedFareClass; }
    private transient double displayPrice;
    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    /**
     * Giá rẻ nhất hiển thị trên card — lấy từ fareOptions sau JOIN.
     * Nếu không có fareOptions thì = 0.
     */

    public Flight() {
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


    // Transient nơi chứa tạm thời các dữ liệu đầy đủ đó sau khi thực hiện thuật toán JOIN (Kết hợp) ở tầng Repository.

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