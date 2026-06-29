package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private final FirebaseFirestore db;
    private ListenerRegistration listenerReg;

    public interface OnNotificationsLoaded {
        void onLoaded(List<Notification> notifications);
        void onError(String error);
    }

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lắng nghe realtime notifications của user.
     * Lấy từ: notifications (root collection) filter theo userId.
     * Gọi removeListener() trong ViewModel.onCleared() để tránh leak.
     */
    public void observeNotifications(String userId, OnNotificationsLoaded callback) {
        listenerReg = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    if (querySnapshot == null) return;

                    List<Notification> list = new ArrayList<>(querySnapshot.toObjects(Notification.class));
                    callback.onLoaded(list);
                });
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    public void markAsRead( String notificationId) {
        db.collection("notifications")
                .document(notificationId)
                .update("read", true);
    }

    public void removeListener() {
        if (listenerReg != null) {
            listenerReg.remove();
            listenerReg = null;
        }
    }
}