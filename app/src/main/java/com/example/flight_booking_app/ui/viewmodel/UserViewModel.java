package com.example.flight_booking_app.ui.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.User;
import com.example.flight_booking_app.data.repository.AppCacheManager;
import com.example.flight_booking_app.data.repository.AuthRepository;
import com.example.flight_booking_app.data.repository.UserRepository;

import java.util.HashMap;

public class UserViewModel extends ViewModel {
    private final UserRepository repository;
    private final AuthRepository authRepository;
    private final AppCacheManager appCacheManager;

    // MutableLiveData để có quyền cập nhật dữ liệu khi các hàm callback từ Repository

    // Dữ liệu user Fragment observe để cập nhật UI
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    // Trạng thái update profile (loading/success/error)
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();

    // Trạng thái logout  Fragment observe để chuyển màn hình
    private final MutableLiveData<Boolean> logoutState = new MutableLiveData<>();

    private final MutableLiveData<Uri> selectedImageUri = new MutableLiveData<>();



    // ViewModel không có context
    public UserViewModel() {
        repository = new UserRepository();
        authRepository = new AuthRepository();
        appCacheManager = new AppCacheManager();
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<UiState> getUiState() { return uiState; }
    public LiveData<Boolean> getLogoutState() { return logoutState; }

    public LiveData<Uri> getSelectedImageUri() { return selectedImageUri; }

    public void setSelectedImageUri(Uri uri) {
        selectedImageUri.setValue(uri);
    }

    // Lắng nghe sự kiện thay đổi
    public void startObservingUser() {
        repository.observeCurrentUser(new UserRepository.GetUserCallback() {
            @Override public void onSuccess(User user) {
                currentUser.setValue(user);  // cập nhật LiveData đến Fragment tự vẽ lại
            }
            @Override public void onError(String errorMessage) {
                uiState.setValue(UiState.error(errorMessage));
            }
        });
    }

    // Đăng xuất
    public void logout() {
        repository.removeUserObserver(); // dọn listener trước khi đăng xuất
        authRepository.signOut();
        appCacheManager.clearCache();
        logoutState.setValue(true);
    }

    public void updateProfile(Uri newPhotoUri, HashMap<String,Object> updates, String fullName) {
        uiState.setValue(UiState.loading());
        repository.updateProfile(newPhotoUri, updates, fullName, new UserRepository.UpdateUserCallback() {
            @Override
            public void onSuccess() { uiState.setValue(UiState.success()); }
            @Override
            public void onError(String msg) { uiState.setValue(UiState.error(msg)); }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.removeUserObserver(); // xóa Firebase listener
    }

    // lấy data user
    public void loadUserOnce() {
        if (currentUser.getValue() != null) return;
        repository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
            }
            @Override
            public void onError(String msg) {
                uiState.setValue(UiState.error(msg));
            }
        });
    }

    // Validate dữ liệu
    public boolean validateInfo(String fullName, String email, String phone) {
        if (fullName.isEmpty()) {
            return false; // Dừng lại
        }
        if (email.isEmpty()) {
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        if (phone.isEmpty()) {
            return false;
        }
        if (!phone.startsWith("0") || phone.length() != 10) {
            return false;
        }

        return true;
    }

}