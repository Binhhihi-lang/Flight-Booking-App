package com.example.flight_booking_app.ui.view.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.ui.viewmodel.PassengerInputViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class PassengerInputActivity extends AppCompatActivity {

    private PassengerInputViewModel viewModel;

    private AutoCompleteTextView actvTitle;
    private TextInputEditText etFullName, etDob, etIdentity;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_input);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupViewModel();
        setupUIListeners();
    }

    private void bindViews() {
        actvTitle = findViewById(R.id.actv_personal_gender);
        etFullName = findViewById(R.id.et_personal_full_name);
        etDob = findViewById(R.id.et_personal_dob);
        etIdentity = findViewById(R.id.et_personal_identity);
        btnSubmit = findViewById(R.id.btn_personal_submit);

        findViewById(R.id.toolbar_personal_info).setOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PassengerInputViewModel.class);

        // Hứng Passenger truyền sang từ BookingInfoActivity
        Passenger initialPassenger = (Passenger) getIntent().getSerializableExtra("passenger");
        viewModel.initPassenger(initialPassenger);

        // 1. Observer lỗi Validate
        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Observer khi lưu thành công -> Đóng Activity và trả kết quả về BookingInfo
        viewModel.getSaveSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updated_passenger", viewModel.getPassengerLive().getValue());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        // 3. Khôi phục dữ liệu lên UI (chống mất data khi xoay màn hình)
        viewModel.getPassengerLive().observe(this, passenger -> {
            if (passenger != null) {
                setupTitleDropdown(passenger.getType());

                // Chỉ set text nếu UI đang trống để tránh xung đột con trỏ nhấp nháy khi đang gõ
                if (etFullName.getText().toString().isEmpty() && passenger.getFullName() != null) {
                    etFullName.setText(passenger.getFullName());
                }
                if (etDob.getText().toString().isEmpty() && passenger.getDateOfBirth() != null) {
                    etDob.setText(passenger.getDateOfBirth());
                }
                if (actvTitle.getText().toString().isEmpty() && passenger.getTitle() != null) {
                    // filter=false để dropdown không bị lọc mất các lựa chọn khác
                    actvTitle.setText(passenger.getTitle(), false);
                }
            }
        });
    }

    private void setupTitleDropdown(String passengerType) {
        String[] titles;
        if ("ADULT".equals(passengerType)) {
            titles = new String[]{"Ông", "Bà"};
        } else {
            titles = new String[]{"Bé trai", "Bé gái"};
        }

        // Sử dụng layout dropdown mặc định của Material Design
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, titles);
        actvTitle.setAdapter(adapter);
    }

    private void setupUIListeners() {
        // --- Danh xưng ---
        actvTitle.setOnItemClickListener((parent, view, position, id) -> {
            viewModel.updateTitle(parent.getItemAtPosition(position).toString());
        });

        // --- Ngày sinh (DatePicker) ---
        etDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        // Format chuẩn 2 số (Ví dụ: 05/09/1998 thay vì 5/9/1998)
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                        etDob.setText(formattedDate);
                        viewModel.updateDob(formattedDate);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // --- Tên ---
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.updateFullName(s.toString());
            }
        });

        // --- Nút Submit ---
        btnSubmit.setOnClickListener(v -> viewModel.validateAndSave());
    }
}