package com.example.flight_booking_app.ui.view.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Seat;

import java.util.ArrayList;
import java.util.List;

/**
 * SeatAdapter – Hiển thị lưới ghế cho RecyclerView (GridLayoutManager 7 cột).
 * <p>
 * Ba loại ô:
 * - AISLE  : ô lối đi (số hàng, nền trong suốt, không click)
 * - HIDDEN : ô ẩn (ghế BLOCKED / không tồn tại)
 * - Ghế thường: màu theo type + trạng thái selected/booked
 */
public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat, int position);
    }

    private List<Seat> seatList = new ArrayList<>();
    private final OnSeatClickListener listener;

    public  SeatAdapter(OnSeatClickListener listener) {
        this.listener = listener;
        // Tối ưu RecyclerView: mỗi item có id duy nhất giúp tránh flash khi notify
        setHasStableIds(false); // Seat AISLE/HIDDEN không có seatId ổn định
    }

    /**
     * Cập nhật toàn bộ lưới ghế (gọi lần đầu từ ViewModel).
     */
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

        switch (seat.getType() == null ? "" : seat.getType()) {

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
                // ── Ghế thường ────────────────────────────────────────────
                holder.tvSeatLabel.setVisibility(View.VISIBLE);
                holder.tvSeatLabel.setText(seat.getColumn()); // Chỉ hiện A/B/C/D/E/F
                holder.tvSeatLabel.setTextColor(Color.WHITE);
                holder.tvSeatLabel.setBackgroundResource(resolveSeatBackground(seat));

                if ("BOOKED".equalsIgnoreCase(seat.getStatus())) {
                    // Ghế đã đặt: không cho click
                    holder.itemView.setOnClickListener(null);
                    holder.itemView.setClickable(false);
                } else {
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

    // ─── Private helpers ──────────────────────────────────────────────────

    /**
     * Chọn drawable background phù hợp theo trạng thái và loại ghế.
     * Ưu tiên: selected > booked > type
     */
    private int resolveSeatBackground(Seat seat) {
        if (seat.isSelected()) {
            return R.drawable.bg_seat_selecting;
        }
        if ("BOOKED".equalsIgnoreCase(seat.getStatus())) {
            return R.drawable.bg_seat_booked;
        }
        switch (seat.getType()) {
            case "PREMIUM":
                return R.drawable.bg_seat_premium;
            case "FRONT_ROW":
                return R.drawable.bg_seat_front;
            case "EXIT_ROW":
                return R.drawable.bg_seat_exit_row;
            default:
                return R.drawable.bg_seat_standard;
        }
    }
}