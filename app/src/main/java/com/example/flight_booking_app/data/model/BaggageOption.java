package com.example.flight_booking_app.data.model;


import java.io.Serializable;

public class BaggageOption implements Serializable {
    private String baggageId;
    private int weightKg;        // 0, 20, 25, 30...
    private boolean isFree;      // true nếu là lựa chọn mặc định/miễn phí, false nếu mua thêm
    private double priceVnd;     // 0 hoặc số tiền phải trả thêm
    private transient boolean isSelected = false;

    // Constructor mặc định cho Firebase
    public BaggageOption() {}

    public BaggageOption(int weightKg, boolean isFree, double priceVnd) {
        this.weightKg = weightKg;
        this.isFree = isFree;
        this.priceVnd = priceVnd;
    }

    public int getWeightKg() { return weightKg; }
    public void setWeightKg(int weightKg) { this.weightKg = weightKg; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

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