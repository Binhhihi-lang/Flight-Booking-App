package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.repository.AuthRepository;
import com.google.firebase.auth.AuthCredential;

/**
 * Dùng MutableLiveData để đẩy trạng thái lên View.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    // View observe cái này để biết trạng thái xác thực
    private final MutableLiveData<AuthResult> authState = new MutableLiveData<>();

    public AuthViewModel() {
        this.repository = new AuthRepository();
    }

    public LiveData<AuthResult> getAuthState() {
        return this.authState;
    }

    // Actions được gọi từ View
    public void login(String email, String password) {
        authState.setValue(AuthResult.loading());

        repository.login(email, password, new AuthRepository.RoleCallback() {
            @Override public void onRoleVerified() {
                authState.setValue(AuthResult.success());
            }
            @Override public void onAccessDenied() {
                authState.setValue(AuthResult.error("Bạn không có quyền truy cập!"));
            }
            @Override public void onError(String errorMessage) {
                authState.setValue(AuthResult.error(errorMessage));
            }
        });
    }

    public void signUp(String fullName, String email, String password) {
        authState.setValue(AuthResult.loading());

        repository.signUp(fullName, email, password, new AuthRepository.AuthCallback() {
            @Override public void onSuccess() {
                authState.setValue(AuthResult.success());
            }
            @Override public void onError(String errorMessage) {
                authState.setValue(AuthResult.error(errorMessage));
            }
        });
    }


    public void signInWithGoogle(AuthCredential credential, String displayName, String email) {
        authState.setValue(AuthResult.loading());

        repository.signInWithGoogle(credential, displayName, email,
                new AuthRepository.RoleCallback() {
                    @Override public void onRoleVerified() {
                        authState.setValue(AuthResult.success());
                    }
                    @Override public void onAccessDenied() {
                        authState.setValue(AuthResult.error("Bạn không có quyền truy cập!"));
                    }
                    @Override public void onError(String errorMessage) {
                        authState.setValue(AuthResult.error(errorMessage));
                    }
                });
    }
}