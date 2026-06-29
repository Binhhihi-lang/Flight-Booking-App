package com.example.flight_booking_app.ui.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Notification;
import com.example.flight_booking_app.utils.PriceFormatter;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onClick(Notification notification);
    }

    private final List<Notification> notificationList;
    private final Context context;
    private final OnNotificationClickListener clickListener;

    public NotificationAdapter(List<Notification> notificationList, Context context,
                               OnNotificationClickListener clickListener) {
        this.notificationList = notificationList;
        this.context          = context;
        this.clickListener    = clickListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvBody.setText(notification.getBody());
        holder.tvDate.setText(PriceFormatter.formatDateOnly(notification.getCreatedAt()));

        if (!notification.isRead()) {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
        } else {
            holder.viewUnreadDot.setVisibility(View.GONE);
        }

        // Click → Fragment xử lý điều hướng + markAsRead
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(notification);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvDate;
        View viewUnreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle       = itemView.findViewById(R.id.tv_notification_title);
            tvBody        = itemView.findViewById(R.id.tv_notification_body);
            tvDate        = itemView.findViewById(R.id.tv_notification_date);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}