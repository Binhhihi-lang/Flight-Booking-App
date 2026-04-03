package com.example.flight_booking_app.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.flight_booking_app.R;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    TextView tvGoToSignUp ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        MaterialButton btnGoogle = findViewById(R.id.btn_google_signin);
//         //Tải icon Google glide
//        Glide.with(this)
//                .load("https://img.icons8.com/color/48/000000/google-logo.png")
//                .into(new CustomTarget<Drawable>() { // xử lý
//                    // khi tải xong
//                    @Override
//                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                        // Thiết kế kích thước icon (tùy chọn)
//                        resource.setBounds(0, 0, 60, 60);
//                        btnGoogle.setIcon(resource);
//                    }
//                    //
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                        btnGoogle.setIcon(null);
//                    }
//                });

        tvGoToSignUp = findViewById(R.id.tv_create_account);
        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

    }
}