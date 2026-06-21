package com.example.flight_booking_app.data.model;


import java.io.Serializable;

public class BaggageOption implements Serializable {
    private String baggageId;
    private int weightKg;        // 0, 20, 25, 30...
    private double priceVnd;     // 0 hoặc số tiền phải trả thêm
    private transient boolean isSelected = false;

    // Constructor mặc định cho Firebase
    public BaggageOption() {}


    public int getWeightKg() { return weightKg; }
    public void setWeightKg(int weightKg) { this.weightKg = weightKg; }

    public double getPriceVnd() { return priceVnd; }
    public void setPriceVnd(double priceVnd) { this.priceVnd = priceVnd; }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getBaggageId() {
        return baggageId;
    }

    public void setBaggageId(String baggageId) {
        this.baggageId = baggageId;
    }
}