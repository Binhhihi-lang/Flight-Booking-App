package com.example.flight_booking_app.ui.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Notification;
import com.example.flight_booking_app.ui.view.activity.OrderDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private Context context;
    private String currentUserId; // Cần dùng để update trạng thái đã đọc

    public NotificationAdapter(List<Notification> notificationList, Context context, String currentUserId) {
        this.notificationList = notificationList;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).
                inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvBody.setText(notification.getBody());

        if (!notification.isRead()) {
            holder.viewUnreadDot.setVisibility(View.VISIBLE); // CHƯA ĐỌC: Hiện chấm đỏ lên
        } else {
            holder.viewUnreadDot.setVisibility(View.GONE);    // ĐÃ ĐỌC: Ẩn chấm đỏ đi
        }

        // sự kiện click
        holder.itemView.setOnClickListener(v -> {

            // Cập nhật trạng thái đã đọc (isRead = true) lên Firestore cho đẹp bài
            if (!notification.isRead()) {
                FirebaseFirestore.getInstance()
                        .collection("users").document(currentUserId)
                        .collection("notifications").document(notification.getNotificationId())
                        .update("isRead", true);
            }

            // Lấy bookingId đính kèm trong thông báo chuyển thẳng sang màn hình Chi tiết đơn hàng
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("booking_id", notification.getBookingId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvDate;
        View viewUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvBody = itemView.findViewById(R.id.tv_notification_body);
            tvDate = itemView.findViewById(R.id.tv_notification_date);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}