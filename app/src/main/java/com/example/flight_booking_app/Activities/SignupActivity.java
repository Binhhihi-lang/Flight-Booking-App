package com.example.flight_booking_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flight_booking_app.Models.User;
import com.example.flight_booking_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    TextView tvGoToSignUp ;

    private EditText etFullName, etEmail, etPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        tvGoToSignUp = findViewById(R.id.tv_sign_in_here);
        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // ánh xạ UI
        etFullName = findViewById(R.id.et_name);
        etPassword = findViewById(R.id.et_password);
        etEmail = findViewById(R.id.et_email);
        btnSignUp = findViewById(R.id.btn_signup);

        // Khởi tạo database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnSignUp.setOnClickListener(v -> registerUser());

    }
    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra dữ liệu trống (Validation cơ bản)
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi Firebase Auth để tạo tài khoản
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Lấy UID mà Firebase vừa tự tạo ra
                        String userId = mAuth.getCurrentUser().getUid();

                        // Tạo đối tượng User (Khách hàng)
                        User user = new User(fullName, email, null, 0);

                        // Đẩy dữ liệu lên Realtime Database vào nhánh "Users" -> "UID"
                        mDatabase.child("Users").child(userId).setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();

                                        // Chuyển hướng về trang Đăng nhập
                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }
}