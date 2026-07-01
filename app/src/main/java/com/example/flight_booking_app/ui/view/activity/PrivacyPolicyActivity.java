package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flight_booking_app.R;

/**
 * Màn hình "Chính sách bảo mật", mở từ ProfileFragment
 * (mục Cấu hình hệ thống → "Chính sách bảo mật").
 *
 * Nội dung pháp lý quá dài để hard-code trong app nên dùng WebView
 * để load trực tiếp trang chính sách bảo mật chính thức trên 12bay.vn.
 * Nếu sau này có API/CMS riêng cho nội dung này, chỉ cần đổi URL
 * hoặc thay loadUrl() bằng loadDataWithBaseURL() để hiển thị HTML tĩnh.
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY_URL = "https://12bay.vn/chinh-sach-bao-mat";

    private ImageView btnBack;
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupWebView();
        setupClickListeners();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btn_back_privacy);
        webView = findViewById(R.id.webview_privacy);
        progressBar = findViewById(R.id.progress_privacy);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(PRIVACY_POLICY_URL);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }


}
