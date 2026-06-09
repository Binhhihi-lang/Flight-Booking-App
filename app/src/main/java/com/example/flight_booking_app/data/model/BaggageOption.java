package com.example.flight_booking_app.data.model;


import java.io.Serializable;

public class BaggageOption implements Serializable {
    private String baggageId;
    private int weight;
    private double price;

    // Thuộc tính (bấm vào thì đổi màu viền)
    private transient boolean isSelected = false;

    public BaggageOption() {}

    public BaggageOption(String baggageId, int weight, double price) {
        this.baggageId = baggageId;
        this.weight = weight;
        this.price = price;
    }

    // Getters and Setters
    public String getBaggageId() { return baggageId; }
    public void setBaggageId(String baggageId) { this.baggageId = baggageId; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}