package com.example.flight_booking_app.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlightFilterState {
    public final boolean showFullPrice;
    public final String sortMode;              // "PRICE_ASC", "DEPART_EARLY", "DURATION"
    public final List<String> selectedAirlines; // Danh sách hãng được tích chọn
    public final List<Boolean> timeSlots;       // 3 phần tử: [Sáng, Chiều, Tối]
    public final String seatClass;             // "ALL", "ECONOMY", "PREMIUM", "BUSINESS"

    // Constructor toàn vẹn
    public FlightFilterState(boolean showFullPrice, String sortMode,
                             List<String> selectedAirlines, List<Boolean> timeSlots, String seatClass) {
        this.showFullPrice = showFullPrice;
        this.sortMode = sortMode;
        this.selectedAirlines = selectedAirlines;
        this.timeSlots = timeSlots;
        this.seatClass = seatClass;
    }

    // Trạng thái mặc định khi mới vào màn hình (Chưa lọc gì cả)
    public static FlightFilterState defaultState() {
        return new FlightFilterState(
                true,
                "PRICE_ASC",
                new ArrayList<>(), // Trống nghĩa là chọn tất cả
                Arrays.asList(false, false, false),
                "ALL"
        );
    }

    // ─── CÁC HÀM TẠO SAO CHÚP (IMMUTABLE MUTATION)
    public FlightFilterState withShowFullPrice(boolean showFullPrice) {
        return new FlightFilterState(showFullPrice, this.sortMode, this.selectedAirlines, this.timeSlots, this.seatClass);
    }

    public FlightFilterState withSortMode(String sortMode) {
        return new FlightFilterState(this.showFullPrice, sortMode, this.selectedAirlines, this.timeSlots, this.seatClass);
    }

    public FlightFilterState withSelectedAirlines(List<String> selectedAirlines) {
        // ArrayList mới để không bị dính vùng nhớ với object cũ
        return new FlightFilterState(this.showFullPrice, this.sortMode, new ArrayList<>(selectedAirlines), this.timeSlots, this.seatClass);
    }

    public FlightFilterState withTimeSlots(List<Boolean> timeSlots) {
        return new FlightFilterState(this.showFullPrice, this.sortMode, this.selectedAirlines, new ArrayList<>(timeSlots), this.seatClass);
    }

    public FlightFilterState withSeatClass(String seatClass) {
        return new FlightFilterState(this.showFullPrice, this.sortMode, this.selectedAirlines, this.timeSlots, seatClass);
    }
}