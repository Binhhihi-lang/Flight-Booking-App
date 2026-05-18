package com.example.flight_booking_app.data.model;

public class Seat {
    private String seatId;
    private String seatNumber; // VD: 1A, 12C
    private String type;       // PREMIUM, STANDARD, EXIT_ROW
    private double price;      // Phí cộng thêm
    private String status;     // AVAILABLE, BOOKED

    public Seat() {}

    public Seat(String seatId, String seatNumber, String type, double price, String status) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.type = type;
        this.price = price;
        this.status = status;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    // Getters and Setters
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
