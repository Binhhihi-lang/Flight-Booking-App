package com.example.flight_booking_app.data.model;

import java.io.Serializable;

public class Passenger implements Serializable {
    private String passengerId;
    private String type;          // "ADULT", "CHILD", "BABY"
    private int index; // chỉ mục trong danh sách hành khách
    private String label;         // Nhãn hiển thị UI: "Người lớn 1", "Trẻ em 1"
    private String title;         // Danh xưng hiển thị: "Ông", "Bà", "Bé trai", "Bé gái"
    private String fullName;
    private String dateOfBirth;
    private String idNumber;

    private String outboundSeat;       // Lưu mã ghế lượt đi (VD: "12A")
    private double outboundSeatPrice;  // Giá ghế lượt đi

    private String returnSeat;         // Lưu mã ghế lượt về (VD: "14B")
    private double returnSeatPrice;

    private String outboundBaggageId;   // Lưu ID gói hành lý lượt đi được chọn
    private double outboundBaggagePrice; // Lưu giá để tính tổng tiền cho nhanh
    private int outboundBaggageWeight;   // Lưu số kg để hiển thị text ở màn hình ngoài

    private String returnBaggageId;      // Lưu ID gói hành lý lượt về được chọn
    private double returnBaggagePrice;
    private int returnBaggageWeight;

    public Passenger() {}

    public Passenger(String type, int index, String label) {
        this.type = type;
        this.index = index;
        this.label = label;
    }

    public boolean isComplete() {
        return fullName != null && !fullName.trim().isEmpty()
                && title!= null && !title.trim().isEmpty()
                && dateOfBirth != null && !dateOfBirth.trim().isEmpty();
    }



    // Tạo thêm hàm tính tổng chi phí của RIÊNG hành khách này

    public double getTotalPriceWithServices() {
        return this.outboundSeatPrice + this.returnSeatPrice
                + this.outboundBaggagePrice + this.returnBaggagePrice;
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
    public String getDateOfBirth() { return dateOfBirth; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getOutboundSeat() {
        return outboundSeat;
    }

    public void setOutboundSeat(String outboundSeat) {
        this.outboundSeat = outboundSeat;
    }

    public double getOutboundSeatPrice() {
        return outboundSeatPrice;
    }

    public void setOutboundSeatPrice(double outboundSeatPrice) {
        this.outboundSeatPrice = outboundSeatPrice;
    }

    public String getReturnSeat() {
        return returnSeat;
    }

    public void setReturnSeat(String returnSeat) {
        this.returnSeat = returnSeat;
    }

    public double getReturnSeatPrice() {
        return returnSeatPrice;
    }

    public void setReturnSeatPrice(double returnSeatPrice) {
        this.returnSeatPrice = returnSeatPrice;
    }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public int getReturnBaggageWeight() {
        return returnBaggageWeight;
    }

    public void setReturnBaggageWeight(int returnBaggageWeight) {
        this.returnBaggageWeight = returnBaggageWeight;
    }

    public double getReturnBaggagePrice() {
        return returnBaggagePrice;
    }

    public void setReturnBaggagePrice(double returnBaggagePrice) {
        this.returnBaggagePrice = returnBaggagePrice;
    }

    public String getReturnBaggageId() {
        return returnBaggageId;
    }

    public void setReturnBaggageId(String returnBaggageId) {
        this.returnBaggageId = returnBaggageId;
    }

    public int getOutboundBaggageWeight() {
        return outboundBaggageWeight;
    }

    public void setOutboundBaggageWeight(int outboundBaggageWeight) {
        this.outboundBaggageWeight = outboundBaggageWeight;
    }

    public double getOutboundBaggagePrice() {
        return outboundBaggagePrice;
    }

    public void setOutboundBaggagePrice(double outboundBaggagePrice) {
        this.outboundBaggagePrice = outboundBaggagePrice;
    }

    public String getOutboundBaggageId() {
        return outboundBaggageId;
    }

    public void setOutboundBaggageId(String outboundBaggageId) {
        this.outboundBaggageId = outboundBaggageId;
    }
}