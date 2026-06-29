package com.example.flight_booking_app.data.repository;

import android.net.Uri;

import com.example.flight_booking_app.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;

/**
 * UserRepository — Firestore edition
 * <p>
 * Hai chế độ giống cũ:
 * getCurrentUser()     → lấy một lần (.get())
 * observeCurrentUser() → lắng nghe realtime (.addSnapshotListener)
 * <p>
 * Thay đổi so với Realtime DB:
 * - Dùng FirebaseFirestore thay FirebaseDatabase
 * - Listener kiểu ListenerRegistration (remove() thay vì removeEventListener)
 * - Không cần @NonNull DataSnapshot / DatabaseError
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
    private final FirebaseFirestore db;

    // Giữ tham chiếu listener realtime để remove khi cần
    private ListenerRegistration userListenerReg;

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }


    // Lấy dữ liệu một lần


    public void getCurrentUser(GetUserCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        String currentUserId = firebaseUser.getUid();

        db.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                            user.setUserId(currentUserId);
                        }
                        else callback.onError("Không parse được dữ liệu người dùng");
                    } else {
                        callback.onError("Không tìm thấy người dùng");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // Lắng nghe realtime — tự động cập nhật UI khi dữ liệu thay đổi
    public void observeCurrentUser(GetUserCallback callback) {
        // không tạo lại trùng, quan sát chỉ 1 lần
        if (userListenerReg != null) {
            return;
        }
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }
        String currentUserId = firebaseUser.getUid();

        DocumentReference userRef = db.collection("users")
                .document(firebaseUser.getUid());

        userListenerReg = userRef.addSnapshotListener((doc, error) -> {
            if (error != null) {
                callback.onError(error.getMessage());
                return;
            }
            if (doc != null && doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setUserId(currentUserId);
                    callback.onSuccess(user);
                }
            }
        });
    }

    /**
     * Gọi trong ViewModel.onCleared() để giải phóng listener realtime.
     */
    public void removeUserObserver() {
        if (userListenerReg != null) {
            userListenerReg.remove();
            userListenerReg = null;
        }
    }


    public void updateProfile(Uri newPhotoUri, HashMap<String, Object> updates,
                              String newName, UpdateUserCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        // Cập nhật Firestore
        db.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    // Cập nhật Firebase Auth profile (displayName + photo)
                    UserProfileChangeRequest.Builder builder =
                            new UserProfileChangeRequest.Builder().setDisplayName(newName);
                    if (newPhotoUri != null) builder.setPhotoUri(newPhotoUri);

                    currentUser.updateProfile(builder.build())
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(
                                    "Cập nhật Auth thất bại: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(
                        "Cập nhật Firestore thất bại: " + e.getMessage()));
    }
}