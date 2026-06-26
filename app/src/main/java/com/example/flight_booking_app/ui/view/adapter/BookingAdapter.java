package com.example.flight_booking_app.ui.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.ui.view.activity.OrderDetailActivity; // Đã đổi đường dẫn theo yêu cầu
import com.example.flight_booking_app.utils.PriceFormatter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private Context context;
    private SimpleDateFormat dateTimeFormatter;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
        // Định dạng thời gian hiển thị dạng "08:35 24/06" giống như file XML mẫu của bạn
        this.dateTimeFormatter = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
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
        holder.tvOrderCode.setText("Đơn hàng: #" + booking.getBookingCode());

        // 2. Tính toán số lượng loại hành khách (Adult, Child, Baby) từ mảng passengers
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        if (booking.getPassengers() != null) {
            for (Passenger p : booking.getPassengers()) {
                if ("ADULT".equals(p.getType())) {
                    adultCount++;
                } else if ("CHILD".equals(p.getType())) {
                    childCount++;
                } else if ("BABY".equals(p.getType()) ) {
                    infantCount++;
                }
            }
        }
        holder.tvAdultCount.setText(String.valueOf(adultCount));
        holder.tvChildCount.setText(String.valueOf(childCount));
        holder.tvInfantCount.setText(String.valueOf(infantCount));

        // 3. Đổ dữ liệu Tuyến bay (Thành phố đi/đến & Thời gian)
        // Lưu ý: Đảm bảo bạn đã thêm các trường này vào Booking model hoặc lấy từ chặng bay liên kết
        holder.tvDepartureCity.setText(booking.getDepartureCity());
        holder.tvArrivalCity.setText(booking.getArrivalCity());

        if (booking.getDepartureTime() != null) {
            holder.tvDepartureTime.setText(dateTimeFormatter.format(booking.getDepartureTime().toDate()));
        }
        if (booking.getArrivalTime() != null) {
            holder.tvArrivalTime.setText(dateTimeFormatter.format(booking.getArrivalTime().toDate()));
        }


        holder.tvOrderTotalPrice.setText(PriceFormatter.formatPrice(booking.getTotalAmount()));

        // 5. Xử lý màu sắc và text hiển thị dựa trên Trạng thái Tiếng Anh
        String status = booking.getStatus();
        if ("RESERVATION_SUCCESS".equals(status)) {
            holder.tvOrderStatus.setText("Giữ chỗ thành công");
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPending)); // Màu vàng/cam
        } else if ("RESERVATION_FAILED".equals(status)) {
            holder.tvOrderStatus.setText("Giữ chỗ thất bại");
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.colorFailed)); // Màu đỏ
        } else if ("PAYMENT_EXPIRED".equals(status)) {
            holder.tvOrderStatus.setText("Hết hạn thanh toán");
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.colorExpired)); // Màu xám
        } else if ("PAYMENT_SUCCESS".equals(status)) {
            holder.tvOrderStatus.setText("Đã thanh toán");
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.colorSuccess)); // Màu xanh lá
        }

        // 6. Sự kiện click vào thẻ đơn hàng điều hướng sang màn hình Chi tiết đơn hàng
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("booking_id", booking.getBookingId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }
    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode;
        TextView tvAdultCount, tvChildCount, tvInfantCount;
        TextView tvDepartureCity, tvDepartureTime;
        TextView tvArrivalCity, tvArrivalTime;
        TextView tvOrderStatus, tvOrderTotalPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tv_order_code);
            tvAdultCount = itemView.findViewById(R.id.tv_adult_count);
            tvChildCount = itemView.findViewById(R.id.tv_child_count);
            tvInfantCount = itemView.findViewById(R.id.tv_infant_count);
            tvDepartureCity = itemView.findViewById(R.id.tv_outbound_departure_city);
            tvDepartureTime = itemView.findViewById(R.id.tv_departure_time);
            tvArrivalCity = itemView.findViewById(R.id.tv_outbound_arrival_city);
            tvArrivalTime = itemView.findViewById(R.id.tv_arrival_time);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTotalPrice = itemView.findViewById(R.id.tv_order_total_price);
        }
    }
}