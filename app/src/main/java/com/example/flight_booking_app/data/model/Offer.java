package com.example.flight_booking_app.data.model;

public class Offer {
    private String offerId;
    private String title;
    private String code;
    private String description;
    private double discountValue;
    private String discountType; // FIXED hoặc PERCENT
    private double minOrderValue;
    private String imageUrl;
    private String expiryDate;
    private int pointCost;      // Số điểm cần để đổi
    private boolean isRedeemable;

    public Offer() {}

    // Getters and Setters

    public Offer(String offerId, String title, String code, String description, double discountValue, String discountType, double minOrderValue, String imageUrl, String expiryDate, int pointCost, boolean isRedeemable) {
        this.offerId = offerId;
        this.title = title;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.discountType = discountType;
        this.minOrderValue = minOrderValue;
        this.imageUrl = imageUrl;
        this.expiryDate = expiryDate;
        this.pointCost = pointCost;
        this.isRedeemable = isRedeemable;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRedeemable() {
        return isRedeemable;
    }

    public void setRedeemable(boolean redeemable) {
        isRedeemable = redeemable;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public int getPointCost() { return pointCost; }
    public void setPointCost(int pointCost) { this.pointCost = pointCost; }
}
