package com.example.flight_booking_app.ui.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.User;
import com.example.flight_booking_app.data.repository.AuthRepository;
import com.example.flight_booking_app.data.repository.UserRepository;

import java.util.HashMap;

public class UserViewModel extends ViewModel {
    private final UserRepository repository;
    private final AuthRepository authRepository;

    // MutableLiveData để có quyền cập nhật dữ liệu khi các hàm callback từ Repository

    // Dữ liệu user Fragment observe để cập nhật UI
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    // Trạng thái update profile (loading/success/error)
    private final MutableLiveData<AuthResult> updateState = new MutableLiveData<>();

    // Trạng thái logout  Fragment observe để chuyển màn hình
    private final MutableLiveData<Boolean> logoutState = new MutableLiveData<>();

    // ViewModel không có context
    public UserViewModel() {
        repository = new UserRepository();
        authRepository = new AuthRepository();
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<AuthResult> getUpdateState() { return updateState; }
    public LiveData<Boolean> getLogoutState() { return logoutState; }

    // Lắng nghe sự kiện thay đổi
    public void startObservingUser() {
        repository.observeCurrentUser(new UserRepository.GetUserCallback() {
            @Override public void onSuccess(User user) {
                currentUser.setValue(user);  // cập nhật LiveData đến Fragment tự vẽ lại
            }
            @Override public void onError(String errorMessage) {
                updateState.setValue(AuthResult.error(errorMessage));
            }
        });
    }

    // Đăng xuất
    public void logout() {
        repository.removeUserObserver(); // dọn listener trước khi đăng xuất
        authRepository.signOut();
        logoutState.setValue(true);
    }

    public void updateProfile(Uri newPhotoUri, HashMap<String,Object> updates, String fullName) {
        updateState.setValue(AuthResult.loading());
        repository.updateProfile(newPhotoUri, updates, fullName, new UserRepository.UpdateUserCallback() {
            @Override
            public void onSuccess() { updateState.setValue(AuthResult.success()); }
            @Override
            public void onError(String msg) { updateState.setValue(AuthResult.error(msg)); }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.removeUserObserver(); // xóa Firebase listener
    }

    // lấy data user
    public void loadUserOnce() {
        repository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
            }
            @Override
            public void onError(String msg) {
                updateState.setValue(AuthResult.error(msg));
            }
        });
    }
}