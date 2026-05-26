package com.example.flight_booking_app.data.repository;

import androidx.annotation.NonNull;

import com.example.flight_booking_app.data.model.Seat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * SeatRepository – Tầng dữ liệu cho sơ đồ ghế.
 *
 * Cấu trúc Firebase Realtime Database:
 * Seats/
 *   {seatMapId}/
 *     {seatId}/
 *       seatId, seatNumber, row, column, type, price, status
 */
public class SeatRepository {

    private final DatabaseReference dbRef;

    public interface OnSeatsLoadedListener {
        void onLoaded(List<Seat> seats);
        void onError(String error);
    }

    public SeatRepository() {
        this.dbRef = FirebaseDatabase.getInstance().getReference("Seats");
    }

    /**
     * Lấy toàn bộ ghế của một sơ đồ ghế từ Firebase.
     * Callback luôn chạy trên main thread (Firebase đảm bảo điều này
     * khi sử dụng addListenerForSingleValueEvent trên Android).
     */
    public void fetchSeatsByMapId(String seatMapId, OnSeatsLoadedListener listener) {
        dbRef.child(seatMapId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Seat> seatList = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Seat seat = data.getValue(Seat.class);
                            if (seat != null) {
                                seatList.add(seat);
                            }
                        }
                        listener.onLoaded(seatList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }
}