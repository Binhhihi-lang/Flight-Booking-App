package com.example.flight_booking_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flight_booking_app.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvCreateAccount;

    private EditText etEmail;
    private Button btnPasswordReset;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        tvCreateAccount = findViewById(R.id.tv_create_account);
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Ánh xạ giao diện
        etEmail = findViewById(R.id.edtEmailForgot);
        btnPasswordReset = findViewById(R.id.btnReset);

        // Bắt sự kiện bấm nút Password Reset
        btnPasswordReset.setOnClickListener(v -> resetPassword());
    }

    // Hàm xử lý logic đặt lại mật khẩu
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        // Kiểm tra xem người dùng đã nhập email chưa
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ email!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem định dạng email có hợp lệ không
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- ĐOẠN MỚI THÊM TỐI ƯU UI ---
        // Vô hiệu hóa nút bấm và đổi text để người dùng biết app đang xử lý
        btnPasswordReset.setEnabled(false);
        btnPasswordReset.setText("Đang gửi...");

        // Gọi Firebase gửi email reset
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            // --- ĐOẠN MỚI THÊM TỐI ƯU UI ---
            // Bật lại nút bấm sau khi có kết quả từ Firebase
            btnPasswordReset.setEnabled(true);
            btnPasswordReset.setText("Password Reset");

            if (task.isSuccessful()) {
                Toast.makeText(ForgotPasswordActivity.this, "Đã gửi link đặt lại mật khẩu! Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show();
                // Gửi thành công thì đóng trang này, quay lại Login
                finish();
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}