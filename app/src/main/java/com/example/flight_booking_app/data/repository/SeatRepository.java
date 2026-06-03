package com.example.flight_booking_app.data.repository;

import androidx.annotation.NonNull;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.data.model.SeatMapMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatRepository {

    private final DatabaseReference dbTemplates;
    private final DatabaseReference dbFlightSeats;

    public interface OnSeatsLoadedListener {
        void onLoaded(List<Seat> seats, SeatMapMetadata metadata);
        void onError(String error);
    }

    public SeatRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        dbTemplates = db.getReference("seatMapTemplates");
        dbFlightSeats = db.getReference("flightSeats");
    }

    /**
     * Tải sơ đồ ghế trộn (Merge): Khung xương (Template) + Trạng thái thực tế (FlightSeats)
     */
    public void fetchSeatsForFlight(String templateId, String flightId, OnSeatsLoadedListener listener) {
        // BƯỚC 1: Lấy cấu trúc khung xương ghế từ Template
        dbTemplates.child(templateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot templateSnapshot) {
                List<Seat> templateSeats = new ArrayList<>();
                SeatMapMetadata tempMetadata = null;
                for (DataSnapshot ds : templateSnapshot.getChildren()) {
                    if (ds.getKey().equals("metadata")) {
                        tempMetadata = ds.getValue(SeatMapMetadata.class);
                    }
                    // Kiểm tra xem key có phải là "seats" hoặc bỏ qua các thuộc tính như "modelName"
                    if (ds.getKey().equals("modelName")) continue;

                    // Nếu dữ liệu nằm trong nhánh "seats"
                    if (ds.getKey().equals("seats")) {
                        for (DataSnapshot seatDs : ds.getChildren()) {
                            Seat seat = seatDs.getValue(Seat.class);
                            if (seat != null) {
                                // Lấy key của seatDs (Ví dụ: "10A")
                                String realSeatId = seatDs.getKey();
                                seat.setSeatId(realSeatId);

                                seat.setStatus("AVAILABLE");
                                templateSeats.add(seat);
                            }
                        }
                    }
                }

                //
                final SeatMapMetadata finalMetadata = tempMetadata;

                // Lấy trạng thái đặt chỗ thực tế của chuyến bay
                dbFlightSeats.child(flightId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot statusSnapshot) {
                        // Băm dữ liệu trạng thái vào Map
                        Map<String, Seat> bookedSeatsMap = new HashMap<>();
                        for (DataSnapshot ds : statusSnapshot.getChildren()) {
                            // lấy ra ghế đã đặt hoặc bị hỏng
                            Seat bookedInfo = ds.getValue(Seat.class);
                            if (bookedInfo != null) {
                                // Lấy key (VD: "1A") làm key cho Map chứa các ghế đã được sử dụng
                                bookedSeatsMap.put(ds.getKey(), bookedInfo);
                            }
                        }

                        // Trộn dữ liệu ghế được sử dụng hoặc được chọn vào khung
                        for (Seat seat : templateSeats) {
                            Seat dynamicInfo = bookedSeatsMap.get(seat.getSeatId());
                            if (dynamicInfo != null) {
                                // Ghi đè trạng thái và passengerId nếu ghế này đã có biến động
                                seat.setStatus(dynamicInfo.getStatus());
                                seat.setPassengerId(dynamicInfo.getPassengerId());
                            }
                        }

                        // Trả kết quả đã trộn hoàn chỉnh về cho ViewModel
                        listener.onLoaded(templateSeats, finalMetadata);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }
}