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
import com.example.flight_booking_app.data.model.Flight;

import java.util.Locale;

/**
 * - Dùng flight.getDisplayPrice() thay vì flight.getPrice()
 * (Flight không còn field price — giá lấy từ fareOptions sau JOIN)
 * - Dùng flight.getFromIata() / getToIata() — là transient field, điền sau JOIN
 */
public class FlightAdapter extends ListAdapter<Flight, FlightAdapter.FlightViewHolder> {

    public interface OnFlightClickListener {
        void onFlightClick(Flight flight);
    }

    private final OnFlightClickListener listener;

    public FlightAdapter(OnFlightClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Flight> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Flight>() {
                @Override
                public boolean areItemsTheSame(@NonNull Flight a, @NonNull Flight b) {
                    // So sánh bằng flightId (unique key trên Firebase)
                    if (a.getFlightId() == null || b.getFlightId() == null) return false;
                    return a.getFlightId().equals(b.getFlightId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Flight a, @NonNull Flight b) {
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
        holder.bind(getItem(position), listener);
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

        void bind(Flight flight, OnFlightClickListener listener) {
            tvFlightNumber.setText(flight.getFlightNumber());
            tvFlightAirline.setText(flight.getAirlineName());
            tvDuration.setText(flight.getDuration());
            tvDepartureDate.setText(flight.getDepartureDate());
            tvDepartureTime.setText(flight.getDepartureTime());
            tvArrivalTime.setText(flight.getArrivalTime());

            // transient fields — điền từ JOIN trong Repository
            tvDepartureLocation.setText(flight.getFromIata());
            tvArrivalLocation.setText(flight.getToIata());
            tvFareClass.setText(flight.getFareClassName());

            // displayPrice — giá rẻ nhất từ fareOptions, tính trong Repository
            tvPrice.setText(String.format(Locale.getDefault(),
                    "%,d đ", (long) flight.getDisplayPrice()));

            // airlineLogo — transient field từ JOIN Airline
            Glide.with(itemView.getContext())
                    .load(flight.getAirlineLogo())
                    .placeholder(R.drawable.ic_airline)
                    .error(R.drawable.ic_airline)
                    .into(imgAirlineLogo);

            itemView.setOnClickListener(v -> listener.onFlightClick(flight));
        }
    }
}