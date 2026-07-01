package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flight_booking_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentNotificationActivity extends AppCompatActivity {

    // ── Views: AppBar ──────────────────────────────────────────────────────
    private MaterialToolbar toolbar;

    // ── Views: Icon + tiêu đề trạng thái ─────────────────────────────────
    private FrameLayout layoutIconCircle;
    private ImageView imgStatusIcon;
    private TextView tvStatusTitle, tvStatusMessage;

    // ── Views: Card thông tin đơn hàng ─────────────────────────────────────
    private View rowOrderCode, dividerAfterOrderCode;
    private TextView tvOrderCodeValue;
    private TextView tvPaymentMethodValue;
    private TextView tvTimeValue;
    private View rowAmount, dividerBeforeAmount;
    private TextView tvAmountValue;


    private MaterialButton btnPrimary, btnSecondary;
    private View layoutLoading;

    // ── Dữ liệu nhận từ Intent ───────────────────────────────────────────
    private String result;
    private String bookingId;
    private static final String RESULT_SUCCESS = "Success Payment";
    private static final String RESULT_CANCELLED = "Cancel Payment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();

        // Đọc dữ liệu được truyền qua từ Intent
        Intent intent = getIntent();
        result = intent.getStringExtra("result");
        bookingId = intent.getStringExtra("booking_id");

        applyPaymentResult(result);
        populateOrderInfo();
        setupClickListeners();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_payment_notification);

        layoutIconCircle = findViewById(R.id.layout_icon_circle_payment);
        imgStatusIcon = findViewById(R.id.img_status_icon_payment);
        tvStatusTitle = findViewById(R.id.tv_status_title_payment);
        tvStatusMessage = findViewById(R.id.tv_status_message_payment);

        rowOrderCode = findViewById(R.id.row_order_code_payment);
        dividerAfterOrderCode = findViewById(R.id.divider_after_order_code_payment);
        tvOrderCodeValue = findViewById(R.id.tv_order_code_value_payment);

        tvPaymentMethodValue = findViewById(R.id.tv_payment_method_value_payment);
        tvTimeValue = findViewById(R.id.tv_time_value_payment);

        rowAmount = findViewById(R.id.row_amount_payment);
        dividerBeforeAmount = findViewById(R.id.divider_before_amount_payment);
        tvAmountValue = findViewById(R.id.tv_amount_value_payment);

        btnPrimary = findViewById(R.id.btn_primary_payment);
        btnSecondary = findViewById(R.id.btn_secondary_payment);
        layoutLoading = findViewById(R.id.layout_loading);
    }

    /**
     * Set icon, màu, tiêu đề theo trạng thái thanh toán
     */
    private void applyPaymentResult(String result) {
        int color;
        int iconRes;
        String title;
        String message;
        String primaryBtnText;

        if (RESULT_SUCCESS.equals(result)) {
            color = Color.parseColor("#16A34A"); // Xanh lá
            iconRes = R.drawable.ic_checked;
            title = getString(R.string.title_payment_success);
            message = getString(R.string.msg_payment_success);
            primaryBtnText = getString(R.string.btn_view_order_detail);

        } else if (RESULT_CANCELLED.equals(result)) {
            color = Color.parseColor("#F59E0B"); // Vàng Amber
            iconRes = R.drawable.ic_close;
            title = getString(R.string.title_payment_cancelled);
            message = getString(R.string.msg_payment_cancelled);
            primaryBtnText = getString(R.string.btn_retry_payment);

        } else {
            color = Color.parseColor("#DC2626"); // Đỏ
            iconRes = R.drawable.ic_error_circle;
            title = getString(R.string.title_payment_failed);
            message = getString(R.string.msg_payment_failed);
            primaryBtnText = getString(R.string.btn_retry_payment);
        }

        // Áp dụng màu sắc (Sử dụng ViewCompat để tương thích tốt nhất)
        ViewCompat.setBackgroundTintList(layoutIconCircle, ColorStateList.valueOf(color));
        imgStatusIcon.setImageResource(iconRes);
        tvStatusTitle.setText(title);
        tvStatusMessage.setText(message);

        btnPrimary.setText(primaryBtnText);
        btnPrimary.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void populateOrderInfo() {
        // Mã đơn hàng
        boolean hasBookingId = bookingId != null && !bookingId.trim().isEmpty();
        rowOrderCode.setVisibility(hasBookingId ? View.VISIBLE : View.GONE);
        dividerAfterOrderCode.setVisibility(hasBookingId ? View.VISIBLE : View.GONE);
        if (hasBookingId) {
            tvOrderCodeValue.setText(getString(R.string.label_order_code_format, bookingId));
        }

        tvPaymentMethodValue.setText(getString(R.string.label_payment_method_zalopay));

        // Thời gian hiện tại
        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        tvTimeValue.setText(now);

        // Số tiền
        String amount = getIntent().getStringExtra("amount");
        boolean hasAmount = amount != null && !amount.trim().isEmpty();
        rowAmount.setVisibility(hasAmount ? View.VISIBLE : View.GONE);
        dividerBeforeAmount.setVisibility(hasAmount ? View.VISIBLE : View.GONE);
        if (hasAmount) {
            tvAmountValue.setText(amount);
        }
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        btnPrimary.setOnClickListener(v -> {
            if (RESULT_SUCCESS.equals(result)) {
                Intent intent = new Intent(this, OrderDetailActivity.class);
                intent.putExtra("booking_id", bookingId);
                startActivity(intent);
                finish();
            } else {
                finish();
            }
        });

        btnSecondary.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
