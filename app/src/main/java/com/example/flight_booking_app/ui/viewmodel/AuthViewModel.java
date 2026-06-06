package com.example.flight_booking_app.ui.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.repository.AuthRepository;
import com.google.firebase.auth.AuthCredential;

/**
 * Dùng MutableLiveData để đẩy trạng thái lên View.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    // View observe cái này để biết trạng thái xác thực
    private final MutableLiveData<UiState> authState = new MutableLiveData<>();
    private final MutableLiveData<UiState> resetState = new MutableLiveData<>();

    public AuthViewModel() {
        this.repository = new AuthRepository();
    }

    // View chỉ có quyền observe() (quan sát) để cập nhật giao diện.
    public LiveData<UiState> getAuthState() {
        return this.authState;
    }

    public LiveData<UiState> getResetState() {
        return resetState;
    }

    // Actions được gọi từ View
    public void login(String email, String password) {
        // validate
        if (email.isEmpty()) {
            authState.setValue(UiState.error("Email phải được nhập"));
            return;
        }
        // Kiểm tra xem định dạng email có hợp lệ không
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            resetState.setValue(UiState.error("Email không hợp lệ!"));
            return;
        }

        if (password.isEmpty()) {
            authState.setValue(UiState.error("Mật khẩu phải được nhập"));
            return;
        }
        if (password.length() < 6) {
            authState.setValue(UiState.error("Mật khẩu phải từ 6 ký tự trở lên"));
            return;
        }
        authState.setValue(UiState.loading());

        repository.login(email, password, new AuthRepository.RoleCallback() {
            @Override
            public void onRoleVerified() {
                authState.setValue(UiState.success());
            }

            @Override
            public void onAccessDenied() {
                authState.setValue(UiState.error("Bạn không có quyền truy cập!"));
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(UiState.error(errorMessage));
            }
        });
    }

    public void signUp(String fullName, String email, String password) {
        if (fullName.isEmpty()) {
            authState.setValue(UiState.error("Họ tên phải được nhập"));
            return;
        }
        if (email.isEmpty()) {
            authState.setValue(UiState.error("Email phải được nhập"));
            return;
        }
        // Kiểm tra xem định dạng email có hợp lệ không
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            resetState.setValue(UiState.error("Email không hợp lệ!"));
            return;
        }
        if (password.isEmpty()) {
            authState.setValue(UiState.error("Mật khẩu phải được nhập"));
            return;
        }
        if (password.length() < 6) {
            authState.setValue(UiState.error("Mật khẩu phải từ 6 ký tự trở lên"));
            return;
        }
        authState.setValue(UiState.loading());

        repository.signUp(fullName, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authState.setValue(UiState.success());
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(UiState.error(errorMessage));
            }
        });
    }

    // đăng nhập google
    public void signInWithGoogle(AuthCredential credential, String displayName, String email) {
        authState.setValue(UiState.loading());

        repository.signInWithGoogle(credential, displayName, email,
                new AuthRepository.RoleCallback() {
                    @Override
                    public void onRoleVerified() {
                        authState.setValue(UiState.success());
                    }

                    @Override
                    public void onAccessDenied() {
                        authState.setValue(UiState.error("Bạn không có quyền truy cập!"));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        authState.setValue(UiState.error(errorMessage));
                    }
                });
    }

    // gửi resetPassword
    public void resetPassword(String email) {
        // Kiểm tra xem người dùng đã nhập email chưa
        if (email.isEmpty()) {
            resetState.setValue(UiState.error("Vui lòng nhập địa chỉ email!"));
            return;
        }

        // Kiểm tra xem định dạng email có hợp lệ không
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            resetState.setValue(UiState.error("Email không hợp lệ!"));
            return;
        }
        authState.setValue(UiState.loading());
        repository.sendPasswordReset(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authState.setValue(UiState.success());
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(UiState.error(errorMessage));
            }
        });
    }
}