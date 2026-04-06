package com.example.flight_booking_app.Models;

public class User {
    public String fullName;
    public String email;
    public String phoneNumber;
    public int role;


    public User() {
    }

    public User(String fullName, String email, String phoneNumber, int role) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}
