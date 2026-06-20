package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ui.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnGoogleSignUp;
    private TextView tvGoToSignIn;
    private ProgressBar progressBar;

    private AuthViewModel authViewModel;

    private GoogleSignInClient gClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        GoogleSignInAccount account = GoogleSignIn
                                .getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);

                        authViewModel.signInWithGoogle(
                                GoogleAuthProvider.getCredential(account.getIdToken(), null),
                                account.getDisplayName(),
                                account.getEmail()
                        );
                    } catch (ApiException e) {
                        Toast.makeText(this, "Lỗi Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bindViews();
        setupGoogleSignIn();
        setupViewModel();
        setupClickListeners();
        setupRealtimeValidation();
    }

    private void bindViews() {
        etFullName = findViewById(R.id.et_name_sigup);
        etEmail = findViewById(R.id.et_email_signup);
        etPassword = findViewById(R.id.et_password_signup);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignUp = findViewById(R.id.btn_signup);
        btnGoogleSignUp = findViewById(R.id.btn_google_signup);
        tvGoToSignIn = findViewById(R.id.tv_sign_in_here);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        gClient = GoogleSignIn.getClient(this, gOptions);
    }

    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getAuthState().observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnSignUp.setEnabled(false);
                    btnGoogleSignUp.setEnabled(false);
                    break;

                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;

                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnSignUp.setEnabled(true);
                    btnGoogleSignUp.setEnabled(true);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupRealtimeValidation() {
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                if (name.isEmpty()) {
                    etFullName.setError("Họ tên không được để trống");
                } else {
                    // Xóa cảnh báo lỗi nếu người dùng đã nhập hợp lệ
                    etFullName.setError(null);
                }
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString().trim();

                if (password.isEmpty()) {
                    etPassword.setError("Mật khẩu không được để trống");

                }
                // Kiểm tra độ dài hợp lý
                else if (password.length() < 6) {
                    etPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");

                } else {
                    etPassword.setError(null);
                }
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = etPassword.getText().toString().trim();
                String confirmPassword = s.toString().trim();

                if (confirmPassword.isEmpty()) {
                    etConfirmPassword.setError("Mật khẩu không được để trống");

                }
                // Kiểm tra độ dài hợp lý
                else if (confirmPassword.length() < 6) {
                    etConfirmPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");

                } else if (!confirmPassword.equals(password)) {
                    etConfirmPassword.setError("Mật khẩu không khớp");
                } else {
                    etConfirmPassword.setError(null);
                }

            }
        });

        // check
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError("Email không được để trống");

                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không đúng định dạng hợp lệ");

                } else {
                    etEmail.setError(null);
                }

            }

        });
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                etFullName.setError("Vui lòng nhập họ và tên");
                etFullName.requestFocus();
            }
            else if (email.isEmpty()) {
                etEmail.setError("Vui lòng nhập email");
                etEmail.requestFocus();
            }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Định dạng email không hợp lệ");
                etEmail.requestFocus();
            }
            else if (password.isEmpty()) {
                etPassword.setError("Vui lòng nhập mật khẩu");
                etPassword.requestFocus();
            }
            else if (password.length() < 6) {
                etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                etPassword.requestFocus();
            }
            else if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Vui lòng xác nhận lại mật khẩu");
                etConfirmPassword.requestFocus();
            }
            else if (!confirmPassword.equals(password)) {
                etConfirmPassword.setError("Mật khẩu xác nhận không trùng khớp");
                etConfirmPassword.requestFocus();
            } else {
                authViewModel.signUp(fullName, email, password);
            }
        });

        btnGoogleSignUp.setOnClickListener(v ->
                googleLauncher.launch(gClient.getSignInIntent()));

        tvGoToSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

}