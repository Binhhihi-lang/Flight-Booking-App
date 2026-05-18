package com.example.flight_booking_app.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Flight lưu foreign keys (fromCityId, toCityId, airlineId) thay vì dữ liệu thô.
 * Sau khi Repository JOIN với City và Airline:
 * → các transient field được điền để Adapter hiển thị.
 * <p>
 * Transient field KHÔNG lưu lên Firebase (Firebase bỏ qua transient).
 */
public class Flight {

    // ── Firebase fields (lưu trên DB) ────────────────────────────────────────
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
    private int checkedBaggage;

    private double taxFee;
    private double seatSelectionFee = 0;
    private double totalPrice;
    private int rewardPoints;

    private List<FlightFareOption> fareOptions = new ArrayList<>();
    private String fareRuleId;

    // ── Transient display fields (KHÔNG lưu Firebase, điền sau JOIN) ─────────
    // Repository sẽ set các field này sau khi JOIN City + Airline
    private transient String from;         // tên thành phố đi   "Hà Nội"
    private transient String fromIata;     // mã IATA điểm đi    "HAN"
    private transient String fromAirport;  // tên sân bay đi     "Nội Bài"
    private transient String to;           // tên thành phố đến
    private transient String toIata;       // mã IATA điểm đến
    private transient String toAirport;    // tên sân bay đến
    private transient String airlineName;  // tên hãng            "Vietnam Airlines"
    private transient String airlineLogo;  // URL logo hãng

    /**
     * Giá rẻ nhất hiển thị trên card — lấy từ fareOptions sau JOIN.
     * Nếu không có fareOptions thì = 0.
     */
    private transient double displayPrice;



    public Flight() {
    }


    // ── Business logic ────────────────────────────────────────────────────────

    /**
     * Tính giá rẻ nhất còn chỗ từ fareOptions.
     * Dùng trong Repository sau khi load xong để set displayPrice.
     */
    public double computeCheapestPrice() {
        double min = Double.MAX_VALUE;
        for (FlightFareOption opt : fareOptions) {
            if (opt.isAvailable() && opt.getBasePrice() < min) {
                min = opt.getBasePrice();
            }
        }
        return min == Double.MAX_VALUE ? 0.0 : min;
    }

    public double getCheapestPriceForSeatType(String targetSeatType,
                                              List<FareClass> globalFareClasses) {
        double minPrice = Double.MAX_VALUE;
        for (FlightFareOption option : fareOptions) {
            if (!option.isAvailable()) continue;
            for (FareClass fareClass : globalFareClasses) {
                if (fareClass.getFareRuleId().equals(option.getFareClassId())
                        && fareClass.getSeatType().equalsIgnoreCase(targetSeatType)) {
                    if (option.getBasePrice() < minPrice) {
                        minPrice = option.getBasePrice();
                    }
                }
            }
        }
        return minPrice == Double.MAX_VALUE ? 0.0 : minPrice;
    }

    public void calculateBookingTotal(FlightFareOption selectedOption) {
        if (selectedOption == null) return;
        double base = selectedOption.getBasePrice();
        double adultTotal = (base + taxFee) * adultCount;
        double childTotal = (base + taxFee) * 0.75 * childCount;
        double babyTotal = (base + taxFee) * 0.10 * babyCount;
        this.totalPrice = adultTotal + childTotal + babyTotal + seatSelectionFee;
        this.rewardPoints = (int) (this.totalPrice / 100000);
    }

    public void applyFareRule(FareRule rule) {
        this.selectedSeatClass = rule.getFareClassName();
        this.checkedBaggage = rule.getCheckedBaggage();
        updateFlightLogic();
    }

    public void updateFlightLogic() {
        calculateBaggage();
        calculatePoints();
    }

    private void calculateBaggage() {
        this.checkedBaggage = "Thương gia".equals(selectedSeatClass) ? 30 : 0;
    }

    private void calculatePoints() {
        this.rewardPoints = (int) (totalPrice / 100000);
    }

    // ── Getters / Setters Firebase fields ─────────────────────────────────────

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

    public String getSelectedSeatClass() {
        return selectedSeatClass;
    }

    public void setSelectedSeatClass(String v) {
        selectedSeatClass = v;
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

    public double getSeatSelectionFee() {
        return seatSelectionFee;
    }

    public void setSeatSelectionFee(double v) {
        seatSelectionFee = v;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double v) {
        totalPrice = v;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int v) {
        rewardPoints = v;
    }

    public List<FlightFareOption> getFareOptions() {
        return fareOptions;
    }

    public void setFareOptions(List<FlightFareOption> v) {
        fareOptions = v;
    }

    public String getFareRuleId() {
        return fareRuleId;
    }

    public void setFareRuleId(String v) {
        fareRuleId = v;
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

    public String getFromAirport() {
        return fromAirport;
    }

    public void setFromAirport(String v) {
        fromAirport = v;
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

    public String getToAirport() {
        return toAirport;
    }

    public void setToAirport(String v) {
        toAirport = v;
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
}