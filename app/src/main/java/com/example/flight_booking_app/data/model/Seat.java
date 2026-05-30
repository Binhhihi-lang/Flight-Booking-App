package com.example.flight_booking_app.data.model;

import java.util.Objects;

public class Seat {
    private String seatId;      // "seat_1A"
    private String seatNumber;  // "1A"
    private int row;            // 1, 2, 3…
    private String column;      // "A", "B", "C", "D", "E", "F"

    /**
     * Loại ghế:
     * PREMIUM    – Ghế cao cấp (hàng đầu, chỗ gác chân rộng)
     * FRONT_ROW  – Ghế hàng trước (gần cửa ra vào)
     * STANDARD   – Ghế tiêu chuẩn (phổ thông)
     * EXIT_ROW   – Ghế cửa thoát hiểm (chân rộng hơn)
     * AISLE      – Ô lối đi (UI only, không phải ghế thật)
     * HIDDEN     – Ghế bị khoá / không tồn tại (UI only)
     */
    private String type;

    private double price;  // Phí chọn ghế (VD: 60 000đ)

    /**
     * Trạng thái ghế:
     * AVAILABLE – Còn trống, có thể chọn
     * BOOKED    – Đã đặt bởi người khác
     * BLOCKED   – Bị khoá (lỗi kỹ thuật, không bán)
     */
    private String status;
    private String passengerId;



    // ─── Transient fields – KHÔNG lưu Firebase ───────────────────────────
    private transient boolean isSelected  = false;
    private transient boolean isSelecting = false;

    public Seat() {}

    public Seat(String seatId, String seatNumber, int row, String column,
                String type, double price, String status) {
        this.seatId     = seatId;
        this.seatNumber = seatNumber;
        this.row        = row;
        this.column     = column;
        this.type       = type;
        this.price      = price;
        this.status     = status;
    }

    // ─── equals / hashCode theo seatId ───────────────────────────────────
    // Cần thiết để selectedSeats.remove(seat) hoạt động đúng khi bỏ chọn

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        Seat other = (Seat) o;
        return Objects.equals(seatId, other.seatId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seatId);
    }

    // ─── Getters & Setters ────────────────────────────────────────────────
    public String getSeatId()                  { return seatId; }
    public void   setSeatId(String seatId)     { this.seatId = seatId; }

    public String getSeatNumber()              { return seatNumber; }
    public void   setSeatNumber(String v)      { this.seatNumber = v; }

    public int    getRow()                     { return row; }
    public void   setRow(int row)              { this.row = row; }

    public String getColumn()                  { return column; }
    public void   setColumn(String column)     { this.column = column; }

    public String getType()                    { return type; }
    public void   setType(String type)         { this.type = type; }

    public double getPrice()                   { return price; }
    public void   setPrice(double price)       { this.price = price; }

    public String getStatus()                  { return status; }
    public void   setStatus(String status)     { this.status = status; }

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public boolean isSelected()                { return isSelected; }
    public void    setSelected(boolean v)      { this.isSelected = v; }

    public boolean isSelecting()               { return isSelecting; }
    public void    setSelecting(boolean v)     { this.isSelecting = v; }
}