package com.example.flight_booking_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flight_booking_app.Models.User;
import com.example.flight_booking_app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    TextView tvGoToSignUp ;

    private EditText etFullName, etEmail, etPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient gClient;
    private GoogleSignInOptions gOptions;

    private Button btnGoogleSignUp;

    // Cấu hình Launcher để hứng kết quả trả về
    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        // Lấy Token thành công, ném lên Firebase
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(this, "Lỗi Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                Log.d("TAG", "onActivityResult: " + result.getResultCode());
            }
    );

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

        btnGoogleSignUp = findViewById(R.id.btn_google_signup);

        // Khởi tạo database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Sự kiện click nút đăng ký
        btnSignUp.setOnClickListener(v -> registerUser());

        // Cấu hình Google Sign-In
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        gClient = GoogleSignIn.getClient(this, gOptions);

        // Sự kiến đăng ký tk google
        btnGoogleSignUp.setOnClickListener(v -> {
            Intent intent = gClient.getSignInIntent();
            googleLauncher.launch(intent);
        });

    }
    //
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập Firebase thành công
                        String userId = mAuth.getCurrentUser().getUid();

                        // Lấy thông tin từ tài khoản Google để lưu vào DB
                        String fullName = account.getDisplayName();
                        String email = account.getEmail();

                        // Tạo đối tượng User
                        User user = new User(fullName, email, null, 0);

                        // Đẩy dữ liệu lên Realtime Database
                        mDatabase.child("Users").child(userId).setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Đăng ký Google thành công!", Toast.LENGTH_SHORT).show();
                                        // Chuyển hướng về trang chính
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Xác thực Firebase thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
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

                        // Tạo đối tượng User
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