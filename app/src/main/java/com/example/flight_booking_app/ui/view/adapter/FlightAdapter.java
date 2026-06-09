package com.example.flight_booking_app.ui.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.Flight;

import java.util.Locale;

public class FlightAdapter extends ListAdapter<Flight, FlightAdapter.FlightViewHolder> {

    // Interface lúc này CHỈ CẦN truyền Flight là đủ (vì FareClass đã nằm gọn bên trong Flight)
    public interface OnFlightClickListener {
        void onFlightClick(Flight flight);
    }

    private final OnFlightClickListener listener;
    private boolean showFullPrice = true;

    public FlightAdapter(OnFlightClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setShowFullPrice(boolean showFullPrice) {
        if (this.showFullPrice != showFullPrice) {
            this.showFullPrice = showFullPrice;
            notifyDataSetChanged();
        }
    }

    private static final DiffUtil.ItemCallback<Flight> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Flight>() {
                @Override
                public boolean areItemsTheSame(@NonNull Flight a, @NonNull Flight b) {
                    if (a.getFlightId() == null || b.getFlightId() == null) return false;
                    return a.getFlightId().equals(b.getFlightId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Flight a, @NonNull Flight b) {
                    // Cập nhật so sánh giá displayPrice trực tiếp từ flightCard
                    return a.getDisplayPrice() == b.getDisplayPrice()
                            && a.getDepartureTime() != null
                            && a.getDepartureTime().equals(b.getDepartureTime());
                }
            };

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        // KHÔNG truyền biến fareClass dùng chung của adapter nữa, tí nữa sẽ lấy từ trong từng item Flight ra
        holder.bind(getItem(position), listener, showFullPrice);
    }

    static class FlightViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgAirlineLogo;
        private final TextView tvFlightNumber, tvFlightAirline, tvDuration;
        private final TextView tvDepartureDate, tvDepartureTime, tvArrivalTime;
        private final TextView tvDepartureLocation, tvArrivalLocation;
        private final TextView tvFareClass, tvPrice;

        FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAirlineLogo = itemView.findViewById(R.id.imgAirlineLogo);
            tvFlightNumber = itemView.findViewById(R.id.tv_flight_number);
            tvFlightAirline = itemView.findViewById(R.id.tv_flight_airline);
            tvDepartureDate = itemView.findViewById(R.id.tv_date);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvDepartureTime = itemView.findViewById(R.id.tv_departure_time);
            tvArrivalTime = itemView.findViewById(R.id.tv_arrival_time);
            tvDepartureLocation = itemView.findViewById(R.id.tv_departure_location);
            tvArrivalLocation = itemView.findViewById(R.id.tv_arrival_location);
            tvFareClass = itemView.findViewById(R.id.tv_fare_class);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }

        // Bỏ tham số FareClass truyền từ ngoài vào
        void bind(Flight flight, OnFlightClickListener listener, boolean showFullPrice) {
            // ── LẤY FARECLASS RIÊNG ĐƯỢC NHỒI TRONG TỪNG CHUYẾN BAY ──
            FareClass currentFareClass = flight.getSelectedFareClass();
            tvFlightNumber.setText(flight.getFlightNumber());
            tvFlightAirline.setText(flight.getAirlineName());
            tvDuration.setText(flight.getDuration());
            tvDepartureDate.setText(flight.getDepartureDate());
            tvDepartureTime.setText(flight.getDepartureTime());
            tvArrivalTime.setText(flight.getArrivalTime());

            tvDepartureLocation.setText(flight.getFromIata());
            tvArrivalLocation.setText(flight.getToIata());
            tvFareClass.setText(currentFareClass.getTitle());

            double finalPriceToDisplay = 0;
            if (currentFareClass != null) {
                if (showFullPrice) {
                    finalPriceToDisplay = currentFareClass.getBasePrice() + flight.getTaxFee(); // Giá gói + Thuế
                } else {
                    finalPriceToDisplay = currentFareClass.getBasePrice(); // Chỉ giá NET của gói
                }
            }

            tvPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", finalPriceToDisplay));

            Glide.with(itemView.getContext())
                    .load(flight.getAirlineLogo())
                    .placeholder(R.drawable.ic_airline)
                    .error(R.drawable.ic_airline)
                    .into(imgAirlineLogo);

            itemView.setOnClickListener(v -> listener.onFlightClick(flight));
        }
    }
}