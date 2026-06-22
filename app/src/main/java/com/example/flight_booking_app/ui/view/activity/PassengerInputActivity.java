package com.example.flight_booking_app.ui.view.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.BaggageOption;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.ui.view.adapter.BaggageAdapter;
import com.example.flight_booking_app.ui.viewmodel.PassengerInputViewModel;
import com.example.flight_booking_app.utils.PriceFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PassengerInputActivity extends AppCompatActivity {

    private PassengerInputViewModel passengerViewModel;

    private AutoCompleteTextView actvTitle;
    private TextInputEditText etFullName, etDob, etIdentity;
    private MaterialToolbar toolbarPasssenger;
    private MaterialButton btnSave;

    private CardView cardBaggageOutbound;
    private CardView cardBaggageReturn;
    private RecyclerView rvBaggageOutbound;
    private RecyclerView rvBaggageReturn;

    private boolean isRoundTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_input);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        isRoundTrip = getIntent().getBooleanExtra("is_round_trip", false);

        bindViews();
        setupViewModel();
        setupRecyclerView();

        setupUIListeners();
    }

    private void bindViews() {
        actvTitle = findViewById(R.id.actv_personal_gender);
        etFullName = findViewById(R.id.et_personal_full_name);
        etDob = findViewById(R.id.et_personal_dob);
        etIdentity = findViewById(R.id.et_personal_identity);
        btnSave = findViewById(R.id.btn_personal_submit);

        rvBaggageOutbound = findViewById(R.id.rv_baggage_outbound);
        rvBaggageReturn = findViewById(R.id.rv_baggage_return);
        cardBaggageOutbound = findViewById(R.id.card_baggage_outbound_container);
        cardBaggageReturn = findViewById(R.id.card_baggage_return_container); // Card bọc phần lượt về

        // Ẩn section hành lý lượt về nếu là vé 1 chiều
        if (cardBaggageReturn != null) {
            cardBaggageReturn.setVisibility(isRoundTrip ? View.VISIBLE : View.GONE);
        }

        toolbarPasssenger = findViewById(R.id.toolbar_personal_info);

    }

    private void setupRecyclerView() {
        Passenger p = passengerViewModel.getPassengerLive().getValue();
        if (!p.getType().equals("BABY")) {
            rvBaggageOutbound.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            if (isRoundTrip) {
                rvBaggageReturn.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            }
        } else {
            cardBaggageReturn.setVisibility(View.GONE);
            cardBaggageOutbound.setVisibility(View.GONE);
        }
    }

    private void setupViewModel() {
        passengerViewModel = new ViewModelProvider(this).get(PassengerInputViewModel.class);

        // Hứng Passenger truyền sang từ BookingInfoActivity
        Passenger initialPassenger = (Passenger) getIntent().getSerializableExtra("passenger");
        passengerViewModel.initPassenger(initialPassenger);

        ArrayList<BaggageOption> outboundOptions = (ArrayList<BaggageOption>) getIntent().getSerializableExtra("outbound_baggage_options");

        ArrayList<BaggageOption> returnOptions = (ArrayList<BaggageOption>) getIntent().getSerializableExtra("return_baggage_options");
        passengerViewModel.initBaggageOptions(outboundOptions, returnOptions);


        passengerViewModel.getUiState().observe(this, result -> {
            if (result == null) return;
            switch (result.getStatus()) {
                case SUCCESS:
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updated_passenger", passengerViewModel.getPassengerLive().getValue());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    break;

                case ERROR:
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });


        // 3. Khôi phục dữ liệu lên UI
        passengerViewModel.getPassengerLive().observe(this, passenger -> {
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


        // quan sát chọn hành lý
        passengerViewModel.getOutboundBaggageLive().observe(this, options -> {
            if (options != null && !options.isEmpty()) {
                setupBaggageRecyclerView(rvBaggageOutbound, options, false);
            }
        });


        if (isRoundTrip) {
            passengerViewModel.getReturnBaggageLive().observe(this, options -> {
                if (options != null && !options.isEmpty()) {
                    setupBaggageRecyclerView(rvBaggageReturn, options, true);
                }
            });
        }
    }

    // RecyclerView hiện thị gói hành lý
    private void setupBaggageRecyclerView(RecyclerView rv,
                                          List<BaggageOption> options,
                                          boolean isReturn) {
        Passenger p = passengerViewModel.getPassengerLive().getValue();

        BaggageAdapter adapter = new BaggageAdapter(options, selected -> {
            if (isReturn) {
                passengerViewModel.updateReturnBaggage(selected);
            } else {
                passengerViewModel.updateOutboundBaggage(selected);
            }
        });

        rv.setAdapter(adapter);

        // khôi phục lựa chọn
        if (p != null) {
            String savedId = isReturn ? p.getReturnBaggageId() : p.getOutboundBaggageId();
            if (savedId != null) {
                adapter.restoreSelection(savedId);
            }
        }

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
        toolbarPasssenger.setOnClickListener(v -> finish());

        //  Danh xưng
        actvTitle.setOnItemClickListener((parent, view, position, id) -> {
            passengerViewModel.updateTitle(parent.getItemAtPosition(position).toString());
            // Tắt báo đỏ
            actvTitle.setError(null);
        });

        //  Ngày sinh (DatePicker)
        etDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {

                        String formattedDate = PriceFormatter.formatDate(day, month, year);
                        etDob.setText(formattedDate);
                        passengerViewModel.updateDob(formattedDate);
                        // Tắt báo đỏ
                        etDob.setError(null);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        //
        etIdentity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                passengerViewModel.updateIdNumber(s.toString().trim());
            }
        });
        //  Dùng TextWatcher để lưu giá trị nhập vào ViewModel
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                passengerViewModel.updateFullName(s.toString());
                if (!s.toString().trim().isEmpty()) {
                    etFullName.setError(null);
                }
            }
        });


        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString();
            String dob = etDob.getText().toString();
            String title = actvTitle.getText().toString();

            if (title.isEmpty()) {
                actvTitle.setError("Vui lòng chọn danh xưng");
                actvTitle.requestFocus();
                return;
            }

            if (fullName.isEmpty()) {
                etFullName.setError("Vui lòng nhập họ và tên");
                etFullName.requestFocus();
                return;
            }
            if (dob.isEmpty()) {
                etDob.setError("Vui lòng chọn ngày sinh");
                etDob.requestFocus();
                return;
            }


            passengerViewModel.validateAndSave();
        });
    }
}