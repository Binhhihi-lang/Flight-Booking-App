package com.example.flight_booking_app.data.model;

import java.util.List;

public class User {
    private String uId ;
    private String fullName;
    private String email;
    private String phoneNumber;
    private int role;
    private String dob;
    private String citizenCard;
    private String gender;
    private String avatar;

    public User() {
    }

    public User(String uId, String fullName, String email, String phoneNumber, int role, String dob, String citizenCard, String gender, String avatar) {
        this.uId = uId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.dob = dob;
        this.citizenCard = citizenCard;
        this.gender = gender;
        this.avatar = avatar;
    }

    public User(String id, String fullName, String email, int role) {
        this.uId = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCitizenCard() {
        return citizenCard;
    }

    public void setCitizenCard(String citizenCard) {
        this.citizenCard = citizenCard;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}
