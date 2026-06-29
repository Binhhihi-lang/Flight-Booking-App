package com.example.flight_booking_app.ui.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.flight_booking_app.R;

public class ContactSupportDialog extends DialogFragment {

    private static final String ARG_MESSAGE   = "arg_message";
    private static final String ARG_PHONE     = "arg_phone";

    // Số Zalo / hotline dùng chung
    private static final String HOTLINE_NUMBER = "0355935245";

    // ─── Factory
    public static ContactSupportDialog newInstance(String supportMessage, String phone) {
        ContactSupportDialog dialog = new ContactSupportDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, supportMessage);
        args.putString(ARG_PHONE, phone != null ? phone : HOTLINE_NUMBER);
        dialog.setArguments(args);
        return dialog;
    }

    // ─── Dialog setup ────────────────────────────────────────────────────────
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = getArguments() != null ? getArguments().getString(ARG_MESSAGE, "") : "";
        String phone   = getArguments() != null ? getArguments().getString(ARG_PHONE, HOTLINE_NUMBER) : HOTLINE_NUMBER;

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_contact_support, null);

        // ── Bind views ──
        TextView tvMessage = view.findViewById(R.id.tv_support_message);
        LinearLayout btnZalo = view.findViewById(R.id.btn_contact_zalo);
        LinearLayout btnCall = view.findViewById(R.id.btn_contact_call);
        LinearLayout btnSms  = view.findViewById(R.id.btn_contact_sms);

        tvMessage.setText(message);

        // ── Hành động: mở Zalo ──
        btnZalo.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://zalo.me/" + phone));
                startActivity(intent);
            } catch (Exception e) {
                // Zalo chưa cài — mở trình duyệt
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://zalo.me/" + phone));
                startActivity(intent);
            }
            dismiss();
        });

        // ── Hành động: gọi điện ──
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + phone));
            startActivity(intent);
            dismiss();
        });

        // ── Hành động: nhắn SMS ──
        btnSms.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse("smsto:" + phone));
            startActivity(intent);
            dismiss();
        });

        // ── Build Dialog ──
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Đặt chiều rộng dialog = 90% màn hình
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return dialog;
    }

}
