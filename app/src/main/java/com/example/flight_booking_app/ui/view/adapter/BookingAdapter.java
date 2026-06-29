package com.example.flight_booking_app.ui.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.ui.view.activity.OrderDetailActivity;
import com.example.flight_booking_app.utils.PriceFormatter;
import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private Context context;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // 1. Mã đơn hàng
        holder.tvOrderCode.setText("Đơn hàng: #" + booking.getOrderCode());

        // 2. Tính toán số lượng hành khách
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        if (booking.getPassengers() != null) {
            for (Passenger p : booking.getPassengers()) {
                if ("ADULT".equals(p.getType())) {
                    adultCount++;
                } else if ("CHILD".equals(p.getType())) {
                    childCount++;
                } else if ("BABY".equals(p.getType())) {
                    infantCount++;
                }
            }
        }
        holder.tvAdultCount.setText(String.valueOf(adultCount));
        holder.tvChildCount.setText(String.valueOf(childCount));
        holder.tvInfantCount.setText(String.valueOf(infantCount));

        // 3. Đổ dữ liệu Tuyến bay Lượt đi (Outbound)
        Flight outbound = booking.getOutboundFlight();
        if (outbound != null) {
            holder.tvOutboundDepartureCity.setText(outbound.getFrom());
            holder.tvOutboundArrivalCity.setText(outbound.getTo());
            holder.tvOutDepartureTime.setText(PriceFormatter.formatDateTime(outbound.getDepartureTime()));
            holder.tvOutboundArrivalTime.setText(PriceFormatter.formatDateTime(outbound.getArrivalTime()));

            Glide.with(context)
                    .load(outbound.getAirlineLogo())
                    .placeholder(R.drawable.ic_airline)
                    .error(R.drawable.ic_airline)
                    .into(holder.imgRouteDirection);
        } else {
        }

        // 4. Xử lý Tuyến bay Lượt về (Return)
        if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
            holder.layoutReturnFlight.setVisibility(View.VISIBLE);
            Flight returnFlight = booking.getReturnFlight();
            holder.tvReturnDepartureCity.setText(returnFlight.getFrom());
            holder.tvReturnArrivalCity.setText(returnFlight.getTo());
            holder.tvReturnDepartureTime.setText(PriceFormatter.formatDateTime(returnFlight.getDepartureTime()));
            holder.tvReturnArrivalTime.setText(PriceFormatter.formatDateTime(returnFlight.getArrivalTime()));

            Glide.with(context)
                    .load(returnFlight.getAirlineLogo())
                    .placeholder(R.drawable.ic_airline)
                    .error(R.drawable.ic_airline)
                    .into(holder.imgReturnRouteDirection);
        } else {
            holder.layoutReturnFlight.setVisibility(View.GONE);
        }

        // 5. Giá tiền
        holder.tvOrderTotalPrice.setText(PriceFormatter.formatPrice(booking.getTotalAmount()));

        // 6. Xử lý trạng thái đơn hàng
        String status = booking.getStatus();
        String effectiveStatus = getEffectiveStatus(booking);

        if ("RESERVATION_SUCCESS".equals(effectiveStatus)) {
            // Chỉ hiển thị thời gian nếu chưa quá hạn
            String remaining = getRemainingTime(booking.getPaymentDeadline());
            holder.tvOrderStatus.setText("Đợi thanh toán : " + remaining);
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_blue));
        } else if ("PAYMENT_EXPIRED".equals(effectiveStatus)) {
            holder.tvOrderStatus.setText("Hết hạn thanh toán");
            holder.tvOrderStatus.setTextColor(Color.GRAY);
        } else if ("RESERVATION_FAILED".equals(status)) {
            holder.tvOrderStatus.setText("Giữ chỗ thất bại");
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_red));
        } else if ("PAYMENT_SUCCESS".equals(status)) {
            holder.tvOrderStatus.setText("Đã thanh toán");
            holder.tvOrderStatus.setTextColor(Color.parseColor("#16A34A"));
        }

        // 7. Click mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("booking_id", booking.getBookingId());
            context.startActivity(intent);
        });
    }

    public String getEffectiveStatus(Booking booking) {
        String status = booking.getStatus();

        // Nếu đơn hàng đang ở trạng thái chờ thanh toán
        if ("RESERVATION_SUCCESS".equals(status)) {
            // Kiểm tra xem đã quá hạn chưa?
            if (booking.getPaymentDeadline() != null &&
                    booking.getPaymentDeadline().toDate().getTime() <= System.currentTimeMillis()) {
                return "PAYMENT_EXPIRED"; // Trả về trạng thái "ảo" là đã hết hạn
            }
        }
        return status; // Trả về trạng thái thật nếu chưa quá hạn
    }

    // Hàm này sẽ trả về thời gian còn lại để thanh toán
    // trả về chuỗi hiển thị, không động vào model
    private String getRemainingTime(Timestamp deadline) {
        if (deadline == null) return "";
        long diff = deadline.toDate().getTime() - System.currentTimeMillis();
        if (diff <= 0) return "00:00";
        long minutes = (diff / 1000) / 60;
        long seconds = (diff / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode;
        TextView tvAdultCount, tvChildCount, tvInfantCount;
        ImageView imgRouteDirection, imgReturnRouteDirection;

        // Views cho lượt đi
        TextView tvOutboundDepartureCity, tvOutDepartureTime;
        TextView tvOutboundArrivalCity, tvOutboundArrivalTime;

        // Views cho lượt về
        LinearLayout layoutReturnFlight;
        TextView tvReturnDepartureCity, tvReturnDepartureTime;
        TextView tvReturnArrivalCity, tvReturnArrivalTime;

        // Trạng thái & Tiền
        TextView tvOrderStatus, tvOrderTotalPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tv_order_item_code);
            tvAdultCount = itemView.findViewById(R.id.tv_adult_count_item);
            tvChildCount = itemView.findViewById(R.id.tv_child_count_item);
            tvInfantCount = itemView.findViewById(R.id.tv_baby_count_item);

            tvOutboundDepartureCity = itemView.findViewById(R.id.tv_outbound_item_departure_city);
            tvOutDepartureTime = itemView.findViewById(R.id.tv_outbound_item_departure_time);
            imgRouteDirection = itemView.findViewById(R.id.img_outbound_item_route_direction);
            tvOutboundArrivalCity = itemView.findViewById(R.id.tv_outbound_item_arrival_city);
            tvOutboundArrivalTime = itemView.findViewById(R.id.tv_outbound_item_arrival_time);

            layoutReturnFlight = itemView.findViewById(R.id.layout_return_flight);
            tvReturnDepartureCity = itemView.findViewById(R.id.tv_return_item_departure_city);
            tvReturnDepartureTime = itemView.findViewById(R.id.tv_return_item_departure_time);
            imgReturnRouteDirection = itemView.findViewById(R.id.img_return_item_route_direction);
            tvReturnArrivalCity = itemView.findViewById(R.id.tv_return_item_arrival_city);
            tvReturnArrivalTime = itemView.findViewById(R.id.tv_return_item_arrival_time);

            tvOrderStatus = itemView.findViewById(R.id.tv_order_item_status);
            tvOrderTotalPrice = itemView.findViewById(R.id.tv_order_item_total_price);
        }
    }
}
