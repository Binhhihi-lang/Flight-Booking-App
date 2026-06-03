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

public class FlightAdapter extends ListAdapter<Flight, FlightAdapter.FlightViewHolder> {

    public interface OnFlightClickListener {
        void onFlightClick(Flight flight);
    }

    private final OnFlightClickListener listener;

    // 1. MỚI THÊM: Biến lưu trạng thái hiển thị giá (Mặc định là Giá đầy đủ = true)
    private boolean showFullPrice = true;

    public FlightAdapter(OnFlightClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // 2. MỚI THÊM: Hàm để Activity/Fragment gọi vào khi người dùng đổi bộ lọc
    public void setShowFullPrice(boolean showFullPrice) {
        if (this.showFullPrice != showFullPrice) {
            this.showFullPrice = showFullPrice;
            notifyDataSetChanged(); // Yêu cầu Adapter vẽ lại toàn bộ danh sách đang hiển thị để cập nhật giá
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
        // 3. MỚI THÊM: Truyền biến showFullPrice vào cho ViewHolder để nó biết đường tính toán
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

        // 4. MỚI THÊM: Nhận thêm biến boolean showFullPrice
        void bind(Flight flight, OnFlightClickListener listener, boolean showFullPrice) {
            tvFlightNumber.setText(flight.getFlightNumber());
            tvFlightAirline.setText(flight.getAirlineName());
            tvDuration.setText(flight.getDuration());
            tvDepartureDate.setText(flight.getDepartureDate());
            tvDepartureTime.setText(flight.getDepartureTime());
            tvArrivalTime.setText(flight.getArrivalTime());

            tvDepartureLocation.setText(flight.getFromIata());
            tvArrivalLocation.setText(flight.getToIata());
            tvFareClass.setText(flight.getFareClassName());

            // 5. MỚI THÊM: Logic tính toán giá dựa theo lựa chọn của người dùng
            double finalPriceToDisplay;
            if (showFullPrice) {
                finalPriceToDisplay = flight.getDisplayPrice() + flight.getTaxFee(); // Giá + Thuế
            } else {
                finalPriceToDisplay = flight.getDisplayPrice(); // Chỉ giá NET
            }

            // Dùng %,.0f để format số double tránh lỗi thay vì ép kiểu (long)
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