package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.flight_booking_app.R;

public class SplashActivity extends AppCompatActivity {
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (currentLocales.isEmpty()) {
            // Nếu chưa từng cấu hình ngôn ngữ, ép buộc app chọn tiếng Việt ("vi")
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags("vi");
            AppCompatDelegate.setApplicationLocales(appLocale);
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // xử lý intro
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);

    }
}