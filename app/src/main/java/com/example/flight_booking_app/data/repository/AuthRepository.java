package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    public interface AuthCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface RoleCallback {
        void onRoleVerified();          // role == 0 (user thường)
        void onAccessDenied();          // role != 0 (admin, bị khoá)
        void onError(String errorMessage);
    }

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;                          // ← đổi từ DatabaseReference

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();                 // ← đổi từ FirebaseDatabase
    }


    // ─── Đăng nhập ───────────────────────────────────────────────────────────

    public void login(String email, String password, RoleCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(callback);
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng nhập thất bại";
                        callback.onError(msg);
                    }
                });
    }

    // ─── Đăng ký ─────────────────────────────────────────────────────────────

    public void signUp(String fullName, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        User newUser = new User(uid, fullName, email, 0);
                        saveUserToDatabase(uid, newUser, callback);
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng ký thất bại";
                        callback.onError(msg);
                    }
                });
    }

    // ─── Đăng nhập Google ────────────────────────────────────────────────────

    public void signInWithGoogle(AuthCredential credential,
                                 String displayName, String email,
                                 RoleCallback callback) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onError("Xác thực Google thất bại");
                        return;
                    }
                    String uid = mAuth.getCurrentUser().getUid();

                    // Kiểm tra user đã tồn tại trong Firestore chưa
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    // Đã có → kiểm tra role
                                    checkUserRole(callback);
                                } else {
                                    // Chưa có → tạo mới
                                    User newUser = new User(uid, displayName, email, 0);
                                    saveUserToDatabase(uid, newUser, new AuthCallback() {
                                        @Override public void onSuccess() { callback.onRoleVerified(); }
                                        @Override public void onError(String msg) { callback.onError(msg); }
                                    });
                                }
                            })
                            .addOnFailureListener(e -> callback.onError("Không thể kết nối Database"));
                });
    }

    // đăng xuất
    public void signOut() {
        mAuth.signOut();
    }

    // ─── Đặt lại mật khẩu ────────────────────────────────────────────────────

    public void sendPasswordReset(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                String msg = task.getException() != null
                        ? task.getException().getMessage()
                        : "Đặt lại mật khẩu thất bại";
                callback.onError(msg);
            }
        });
    }

    // ─── Internal ────────────────────────────────────────────────────────────

    private void checkUserRole(RoleCallback callback) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError("Không tìm thấy người dùng");
                        return;
                    }
                    User user = doc.toObject(User.class);
                    if (user != null && user.getRole() == 0) {
                        callback.onRoleVerified();
                    } else {
                        mAuth.signOut();
                        callback.onAccessDenied();
                    }
                })
                .addOnFailureListener(e -> callback.onError("Không thể lấy dữ liệu người dùng"));
    }

    private void saveUserToDatabase(String uid, User user, AuthCallback callback) {
        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lưu thông tin người dùng thất bại"));
    }
}