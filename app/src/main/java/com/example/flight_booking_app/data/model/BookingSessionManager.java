package com.example.flight_booking_app.data.model;

public class BookingSessionManager {
    private static BookingSessionManager instance;

    // Các biến lưu trữ tạm thời phiên đặt vé hiện tại
    private Flight selectedOutboundFlight;
    private FareClass selectedOutboundFare;
    private Flight selectedReturnFlight;
    private FareClass selectedReturnFare;
    private boolean isRoundTrip;
    private int adultCount, childCount, babyCount;

    private BookingSessionManager() {}

    public static synchronized BookingSessionManager getInstance() {
        if (instance == null) {
            instance = new BookingSessionManager();
        }
        return instance;
    }
    // Hàm xóa sạch dữ liệu sau khi khách đặt vé thành công
    public void clearSession() {
        selectedOutboundFlight = null;
        selectedOutboundFare = null;
        selectedReturnFlight = null;
        selectedReturnFare = null;
    }

    public void setSelectedOutboundFlight(Flight f) { this.selectedOutboundFlight = f; }
    public Flight getSelectedOutboundFlight() { return selectedOutboundFlight; }

    public static void setInstance(BookingSessionManager instance) {
        BookingSessionManager.instance = instance;
    }

    public FareClass getSelectedOutboundFare() {
        return selectedOutboundFare;
    }

    public void setSelectedOutboundFare(FareClass selectedOutboundFare) {
        this.selectedOutboundFare = selectedOutboundFare;
    }

    public Flight getSelectedReturnFlight() {
        return selectedReturnFlight;
    }

    public void setSelectedReturnFlight(Flight selectedReturnFlight) {
        this.selectedReturnFlight = selectedReturnFlight;
    }

    public FareClass getSelectedReturnFare() {
        return selectedReturnFare;
    }

    public void setSelectedReturnFare(FareClass selectedReturnFare) {
        this.selectedReturnFare = selectedReturnFare;
    }

    public boolean isRoundTrip() {
        return isRoundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        isRoundTrip = roundTrip;
    }

    public int getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(int adultCount) {
        this.adultCount = adultCount;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getBabyCount() {
        return babyCount;
    }

    public void setBabyCount(int babyCount) {
        this.babyCount = babyCount;
    }
}
