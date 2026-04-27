package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * giao tiếp với Firebase Auth & Database.
 * Kết quả trả về qua interface Callback để ViewModel xử lý tiếp.
 */
public class AuthRepository {

    // Callback interfaces

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
    private final DatabaseReference mDatabase;

    public AuthRepository() {
        mAuth    = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

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

    /**
     * Đăng ký tài khoản mới bằng email/password.
     * Tạo user trên Firebase Auth rồi lưu thông tin vào Realtime DB.
     */
    public void signUp(String fullName, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        User newUser = new User(fullName, email, 0);
                        saveUserToDatabase(uid, newUser, callback);
                    } else {
                        String msg = "Đăng ký thất bại";
                        callback.onError(msg);
                    }
                });
    }


    public void signInWithGoogle(AuthCredential credential,
                                 String displayName, String email,
                                 RoleCallback callback) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onError("Xác thực Google thất bại");
                        return;
                    }
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String uid = firebaseUser.getUid();

                    // Kiểm tra user đã tồn tại trong DB chưa
                    mDatabase.child("Users").child(uid).get()
                            .addOnCompleteListener(dbTask -> {
                                if (!dbTask.isSuccessful()) {
                                    callback.onError("Không thể kết nối Database");
                                    return;
                                }
                                if (dbTask.getResult().exists()) {
                                    // Đã có thì kiểm tra role
                                    checkUserRole(callback);
                                } else {
                                    // Chưa có tạo mới
                                    User newUser = new User(displayName, email, 0);
                                    saveUserToDatabase(uid, newUser, new AuthCallback() {
                                        @Override public void onSuccess() { callback.onRoleVerified(); }
                                        @Override public void onError(String msg) { callback.onError(msg); }
                                    });
                                }
                            });
                });
    }


    // đăng xuất
    public void signOut() {
        mAuth.signOut();
    }


    private void checkUserRole(RoleCallback callback) {
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(uid).get()
                .addOnCompleteListener(dbTask -> {
                    if (!dbTask.isSuccessful()) {
                        callback.onError("Không thể lấy dữ liệu người dùng");
                        return;
                    }
                    User user = dbTask.getResult().getValue(User.class);
                    if (user != null && user.getRole() == 0) {
                        callback.onRoleVerified();
                    } else {
                        mAuth.signOut();
                        callback.onAccessDenied();
                    }
                });
    }

    private void saveUserToDatabase(String uid, User user, AuthCallback callback) {
        mDatabase.child("Users").child(uid).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Lưu thông tin người dùng thất bại");
                    }
                });
    }
}