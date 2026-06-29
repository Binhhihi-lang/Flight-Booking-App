package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Notification;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.repository.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;

    private final MutableLiveData<List<Notification>> notificationListLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();

    public NotificationViewModel() {
        notificationRepository = new NotificationRepository();
    }

    public LiveData<List<Notification>> getNotificationListLive() {
        return notificationListLive;
    }

    public LiveData<UiState> getLoadState() {
        return loadState;
    }

    /**
     * Bắt đầu lắng nghe realtime notifications của một userId cụ thể.
     */
    public void startListening(String userId) {
        if (userId == null || userId.trim().isEmpty() || "GUEST_USER".equals(userId)) {
            loadState.setValue(UiState.error("Chưa đăng nhập hoặc tài khoản không hợp lệ"));
            return;
        }

        // Báo hiệu đang tải lần đầu
        loadState.setValue(UiState.loading());

        notificationRepository.observeNotifications(userId,
                new NotificationRepository.OnNotificationsLoaded() {
                    @Override
                    public void onLoaded(List<Notification> notifications) {
                        notificationListLive.postValue(notifications);
                        loadState.postValue(UiState.success());
                    }

                    @Override
                    public void onError(String error) {
                        // Báo lỗi về UI
                        loadState.postValue(UiState.error(error));
                    }
                });
    }

    // đánh dấu đã đọc thông báo
    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        notificationRepository.removeListener(); // Giải phóng Firestore listener để tránh leak bộ nhớ
    }
}