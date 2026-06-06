package com.example.flight_booking_app.ui.view.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Seat;

import java.util.ArrayList;
import java.util.List;

/**
 * SeatAdapter – Hiển thị lưới ghế cho RecyclerView.
 * Hỗ trợ phân quyền chọn ghế động theo hạng vé của từng hành khách.
 */
public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat, int position);
    }

    private List<Seat> seatList = new ArrayList<>();
    private final OnSeatClickListener listener;

    // BIẾN MỚI: Lưu trữ hạng vé của khách đang thực hiện đặt chỗ
    private final String passengerCabinClass;

    // CẬP NHẬT CONSTRUCTOR: Nhận thêm hạng vé (BUSINESS, PREMIUM_ECONOMY, ECONOMY)
    public SeatAdapter(String passengerCabinClass, OnSeatClickListener listener) {
        this.passengerCabinClass = passengerCabinClass;
        this.listener = listener;
        setHasStableIds(false);
    }

    public void setSeats(List<Seat> seats) {
        this.seatList = seats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat_cell, parent, false);
        return new SeatViewHolder(view);
    }

        @Override
        public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
            Seat seat = seatList.get(position);
            String seatType = seat.getType() == null ? "" : seat.getType().toUpperCase();

            switch (seatType) {
                case "AISLE":
                    // ── Ô lối đi: hiện số hàng, không click ──────────────────
                    holder.tvSeatLabel.setVisibility(View.VISIBLE);
                    holder.tvSeatLabel.setText(seat.getSeatNumber());
                    holder.tvSeatLabel.setBackgroundColor(Color.TRANSPARENT);
                    holder.tvSeatLabel.setTextColor(Color.parseColor("#888888"));
                    holder.itemView.setOnClickListener(null);
                    holder.itemView.setClickable(false);
                    break;

                case "HIDDEN":
                    // ── Ô ẩn: hoàn toàn tàng hình ────────────────────────────
                    holder.tvSeatLabel.setVisibility(View.INVISIBLE);
                    holder.itemView.setOnClickListener(null);
                    holder.itemView.setClickable(false);
                    break;

                default:
                    // ── GHẾ NGỒI THỰC TẾ (PREMIUM, FRONT_ROW, STANDARD, EXTRA_LEGROOM) ──
                    holder.tvSeatLabel.setVisibility(View.VISIBLE);
                    holder.tvSeatLabel.setText(seat.getColumn());

                    // BƯỚC 1: KIỂM TRA QUYỀN HẠN HẠNG VÉ (MAPPING RULES)
                    boolean isAllowedClass = false;
                    String safeCabinClass = passengerCabinClass != null ? passengerCabinClass.toUpperCase() : "ECONOMY";

                    if (safeCabinClass.contains("BUSINESS")) {
                        // Chỉ cần tên vé chứa chữ "BUSINESS" (VD: BUSINESS STANDARD, BUSINESS FLEX)
                        // thì khách được quyền chọn khu ghế PREMIUM
                        isAllowedClass = "PREMIUM".equals(seatType); // nếu giống thì gán bằng true để sang ghế hợp lệ
                    }
                    else if (safeCabinClass.contains("PREMIUM")) {
                        // Tên vé chứa chữ "PREMIUM" (VD: PREMIUM ECONOMY) thì được chọn hàng FRONT_ROW
                        isAllowedClass = "FRONT_ROW".equals(seatType);
                    }
                    else {
                        // Các trường hợp còn lại (ECONOMY STANDARD, ECONOMY LITE, ECONOMY FLEX...)
                        isAllowedClass = "STANDARD".equals(seatType) || "EXTRA_LEGROOM".equals(seatType);
                    }

                    // BƯỚC 2: RẼ NHÁNH GIAO DIỆN VÀ CLICK EVENT DỰA TRÊN KẾT QUẢ KIỂM TRA
                    if (!isAllowedClass) {
                        // TRƯỜNG HỢP A: Sai khoang hạng vé -> Khóa mờ, dùng background mới tạo
                        holder.tvSeatLabel.setBackgroundResource(R.drawable.bg_seat_booked);
                        holder.tvSeatLabel.setTextColor(Color.parseColor("#A0A0A0")); // Chữ xám nhạt mờ đi
                        holder.itemView.setOnClickListener(null);
                        holder.itemView.setClickable(false);
                    }
                    else if ("BOOKED".equalsIgnoreCase(seat.getStatus())) {
                        // TRƯỜNG HỢP B: Đúng khoang hạng vé nhưng ghế này đã ĐƯỢC ĐẶT TRƯỚC
                        holder.tvSeatLabel.setTextColor(Color.WHITE);
                        holder.tvSeatLabel.setBackgroundResource(R.drawable.bg_seat_booked);
                        holder.itemView.setOnClickListener(null);
                        holder.itemView.setClickable(false);
                    }
                    else {
                        // TRƯỜNG HỢP C: Ghế hợp lệ, còn trống -> Cho phép click chọn bình thường
                        // Chữ trắng nền xanh đậm khi selected, chữ trắng nền màu loại ghế khi trống
                        holder.tvSeatLabel.setTextColor(Color.WHITE);
                        holder.tvSeatLabel.setBackgroundResource(resolveSeatBackground(seat));

                        holder.itemView.setClickable(true);
                        holder.itemView.setOnClickListener(v ->
                                listener.onSeatClick(seat, holder.getAdapterPosition()));
                    }
                    break;
            }
        }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSeatLabel;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeatLabel = itemView.findViewById(R.id.tv_seat_label);
        }
    }

    /**
     * Chọn drawable background phù hợp theo trạng thái và loại ghế.
     */
    private int resolveSeatBackground(Seat seat) {
        if (seat.isSelected()) {
            return R.drawable.bg_seat_selecting; // Đang chọn (Xanh lam thẫm)
        }
        if ("BOOKED".equalsIgnoreCase(seat.getStatus())) {
            return R.drawable.bg_seat_booked;
        }
        switch (seat.getType() != null ? seat.getType().toUpperCase() : "") {
            case "PREMIUM":
                return R.drawable.bg_seat_premium;
            case "FRONT_ROW":
                return R.drawable.bg_seat_front;
            case "EXTRA_LEGROOM":
                return R.drawable.bg_seat_exit_row;
            default:
                return R.drawable.bg_seat_standard;
        }
    }
}