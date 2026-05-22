package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ui.viewmodel.AuthViewModel;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView imgBack;
    private TextView tvCreateAccount;

    private EditText etEmail;
    private Button btnPasswordReset;

    private AuthViewModel authViewModel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindView();
        setupViewModel();
        setupClickListeners();

    }

    public void bindView() {
        imgBack = findViewById(R.id.btnBack);
        tvCreateAccount = findViewById(R.id.tv_create_account);
        etEmail = findViewById(R.id.edtEmailForgot);
        btnPasswordReset = findViewById(R.id.btnReset);
    }

    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getResetState().observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    btnPasswordReset.setEnabled(false);
                    btnPasswordReset.setText("Đang gửi...");
                    break;

                case SUCCESS:
                    // Bật lại nút bấm sau khi có kết quả từ Firebase
                    btnPasswordReset.setEnabled(true);
                    btnPasswordReset.setText("Password Reset");
                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi link đặt lại mật khẩu! Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show();
                    // quay lại Login
                    finish();
                    break;

                case ERROR:
                    btnPasswordReset.setEnabled(true);
                    btnPasswordReset.setText("Password Reset");
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

    }

    private void setupClickListeners() {
        imgBack.setOnClickListener(v -> {
            finish();
        });

        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Bắt sự kiện bấm nút Password Reset
        btnPasswordReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            authViewModel.resetPassword(email);
        });
    }

}