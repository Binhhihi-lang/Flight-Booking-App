package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
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

    private EditText etFullName, etEmail, etPassword;
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

        bindViews();
        setupGoogleSignIn();
        setupViewModel();
        setupClickListeners();
    }

    private void bindViews() {
        etFullName     = findViewById(R.id.et_name);
        etEmail        = findViewById(R.id.et_email);
        etPassword     = findViewById(R.id.et_password);
        btnSignUp      = findViewById(R.id.btn_signup);
        btnGoogleSignUp = findViewById(R.id.btn_google_signup);
        tvGoToSignIn   = findViewById(R.id.tv_sign_in_here);
        progressBar    = findViewById(R.id.progress_bar);
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
                    startActivity(new Intent(this, LoginActivity.class));
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

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> handleSignUp());

        btnGoogleSignUp.setOnClickListener(v ->
                googleLauncher.launch(gClient.getSignInIntent()));

        tvGoToSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleSignUp() {
        String fullName = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên phải được nhập");
            etFullName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email phải được nhập");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu phải được nhập");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            etPassword.requestFocus();
            return;
        }

        authViewModel.signUp(fullName, email, password);
    }
}