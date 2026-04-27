package com.example.flight_booking_app.data.repository;

import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.flight_booking_app.data.model.User;
import com.example.flight_booking_app.ui.view.activity.UserEditProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Có 2 kiểu lấy data:
 *   getCurrentUser(): lấy MỘT LẦN (.get())
 *   observeCurrentUser(): lắng nghe LIÊN TỤC (addValueEventListener)
 */
public class UserRepository {

    public interface GetUserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface UpdateUserCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private final FirebaseAuth mAuth;
    private final DatabaseReference usersRef;

    // tham chiếu để xóa listener khi cần
    private ValueEventListener userListener;
    private DatabaseReference activeUserRef;

    public UserRepository() {
        mAuth    = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    // Lấy data một lần
    public void getCurrentUser(GetUserCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        usersRef.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    callback.onSuccess(user);
                }
                else {
                    callback.onError("Không tìm thấy người dùng");
                }
            } else {
                callback.onError("Không thể tải thông tin người dùng");
            }
        });
    }

    // Lắng nghe realtime dữ liệu sẽ thay đổi khi cập nhật người dùng
    public void observeCurrentUser(GetUserCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        activeUserRef = usersRef.child(uid);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) callback.onSuccess(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        activeUserRef.addValueEventListener(userListener);
    }

    /** ViewModel.onCleared() để giải phóng listener khi ko dùng observe (người dùng đăng xuất) */
    public void removeUserObserver() {
        if (activeUserRef != null && userListener != null) {
            activeUserRef.removeEventListener(userListener);
            userListener  = null;
            activeUserRef = null;
        }
    }

    // Cập nhật
    public void updateProfile(Uri newPhotoUri, HashMap<String,Object> updates, String newName, UpdateUserCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // lấy uid của Firebase Auth để cập nhật
        String uid = currentUser.getUid();

        usersRef.child(uid).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                //  Cập nhật UserProfile của Firebase Auth
                UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName);
                if (newPhotoUri != null) {
                    profileUpdatesBuilder.setPhotoUri(newPhotoUri);
                }
                UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();


                currentUser.updateProfile(profileUpdates).addOnCompleteListener(authTask -> {
                    callback.onSuccess();
                });

            } else {
                callback.onError("Cập nhật Database thất bại!");
            }
        });
    }

}