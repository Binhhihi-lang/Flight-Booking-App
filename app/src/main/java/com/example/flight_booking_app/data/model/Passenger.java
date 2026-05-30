package com.example.flight_booking_app.data.model;

import java.io.Serializable;

public class Passenger implements Serializable {
    private String passengerId;
    private String type;          // "ADULT", "CHILD", "BABY"
    private int index;
    private String label;
    private String fullName;
    private String gender;
    private String dateOfBirth;
    private String nationality;
    private String seatNumber;
    private double seatPrice;

    public Passenger() {}

    public Passenger(String type, int index, String label) {
        this.type = type;
        this.index = index;
        this.label = label;
    }

    public boolean isComplete() {
        return fullName != null && !fullName.trim().isEmpty()
                && gender != null && !gender.trim().isEmpty()
                && dateOfBirth != null && !dateOfBirth.trim().isEmpty();
    }


    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public double getSeatPrice() { return seatPrice; }
    public void setSeatPrice(double seatPrice) { this.seatPrice = seatPrice; }
}