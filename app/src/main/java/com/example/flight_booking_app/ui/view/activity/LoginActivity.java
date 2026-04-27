package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvGoToSignUp, tvForgotPassword;
    private ProgressBar progressBar;

    private AuthViewModel authViewModel;

    //Google Sign-In (launcher phải nằm ở Activity vì gắn với lifecycle)

    private GoogleSignInClient gClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        GoogleSignInAccount account = GoogleSignIn
                                .getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);

                        // Đẩy credential lên ViewModel
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ UI
        bindViews();

        setupGoogleSignIn();

        // Observe LiveData và phản ứng (Toast, chuyển màn, hiện/ẩn loading)

        setupViewModel();

        setupClickListeners();
    }

    private void bindViews() {
        etEmail        = findViewById(R.id.et_email);
        etPassword     = findViewById(R.id.et_password);
        btnLogin       = findViewById(R.id.btn_login);
        btnGoogleLogin = findViewById(R.id.btn_google_login);
        tvGoToSignUp   = findViewById(R.id.tv_create_account);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
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
        // gọi viewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe LiveData phản ứng theo status
        authViewModel.getAuthState().observe(this, result -> {
            switch (result.getStatus()) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);
                    btnGoogleLogin.setEnabled(false);
                    break;

                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;

                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    btnGoogleLogin.setEnabled(true);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        btnGoogleLogin.setOnClickListener(v ->
                googleLauncher.launch(gClient.getSignInIntent()));

        tvGoToSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }


    private void handleLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


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

        //  xử lý cho ViewModel
        authViewModel.login(email, password);
    }
}