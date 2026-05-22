package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.PassengerInfo;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

/**
 * BookingInfoActivity - Màn hình nhập thông tin đặt vé.
 *
 * NHẬN từ SearchFlightActivity (sau khi chọn chuyến bay):
 *   - Thông tin chuyến đi (+ chuyến về nếu khứ hồi)
 *   - Số lượng hành khách: adultCount, childCount, babyCount
 *   - Giá vé: basePrice, taxFee
 *
 * NHIỆM VỤ:
 *   ① Hiển thị thông tin chuyến bay
 *   ② Tạo ĐỘNG các input field cho từng hành khách
 *   ③ Tính tổng tiền theo công thức:
 *      - Người lớn: 100%
 *      - Trẻ em: 75%
 *      - Em bé: 10%
 *   ④ Validate đủ thông tin → chuyển sang PassengerInputActivity
 *
 * TRUYỀN đến PassengerInputActivity:
 *   - ArrayList<PassengerInfo> để nhập thông tin chi tiết từng người
 */
public class BookingInfoActivity extends AppCompatActivity {

    // ══════════════════════════════════════════════════════════════════════
    // INTENT KEYS
    // ══════════════════════════════════════════════════════════════════════

    // Chuyến đi (bắt buộc)
    public static final String EXTRA_OUT_FLIGHT_NUMBER = "out_flight_number";
    public static final String EXTRA_OUT_AIRLINE_NAME = "out_airline_name";
    public static final String EXTRA_OUT_AIRLINE_LOGO = "out_airline_logo";
    public static final String EXTRA_OUT_FROM_CITY = "out_from_city";
    public static final String EXTRA_OUT_FROM_IATA = "out_from_iata";
    public static final String EXTRA_OUT_TO_CITY = "out_to_city";
    public static final String EXTRA_OUT_TO_IATA = "out_to_iata";
    public static final String EXTRA_OUT_DEPART_TIME = "out_depart_time";
    public static final String EXTRA_OUT_ARRIVAL_TIME = "out_arrival_time";
    public static final String EXTRA_OUT_DURATION = "out_duration";
    public static final String EXTRA_OUT_DATE = "out_date";
    public static final String EXTRA_OUT_BASE_PRICE = "out_base_price";
    public static final String EXTRA_OUT_TAX_FEE = "out_tax_fee";

    // Chuyến về (optional, chỉ có khi khứ hồi)
    public static final String EXTRA_RET_FLIGHT_NUMBER = "ret_flight_number";
    public static final String EXTRA_RET_AIRLINE_NAME = "ret_airline_name";
    public static final String EXTRA_RET_AIRLINE_LOGO = "ret_airline_logo";
    public static final String EXTRA_RET_FROM_CITY = "ret_from_city";
    public static final String EXTRA_RET_FROM_IATA = "ret_from_iata";
    public static final String EXTRA_RET_TO_CITY = "ret_to_city";
    public static final String EXTRA_RET_TO_IATA = "ret_to_iata";
    public static final String EXTRA_RET_DEPART_TIME = "ret_depart_time";
    public static final String EXTRA_RET_ARRIVAL_TIME = "ret_arrival_time";
    public static final String EXTRA_RET_DURATION = "ret_duration";
    public static final String EXTRA_RET_DATE = "ret_date";
    public static final String EXTRA_RET_BASE_PRICE = "ret_base_price";
    public static final String EXTRA_RET_TAX_FEE = "ret_tax_fee";

    // Hành khách
    public static final String EXTRA_ADULT_COUNT = "adult_count";
    public static final String EXTRA_CHILD_COUNT = "child_count";
    public static final String EXTRA_BABY_COUNT = "baby_count";

    // Flags
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";

    // ══════════════════════════════════════════════════════════════════════
    // VIEWS
    // ══════════════════════════════════════════════════════════════════════

    private MaterialToolbar toolbar;

    // Card chuyến đi
    private TextView tvOutRoute, tvOutDate;
    private TextView tvOutDepartTime, tvOutFromIata, tvOutDuration, tvOutArrivalTime, tvOutToIata;
    private TextView tvOutFlightNumber;
    private ImageView imgOutLogo;

    // Card chuyến về (ẩn nếu một chiều)
    private androidx.cardview.widget.CardView cardReturnFlight;
    private TextView tvRetRoute, tvRetDate;
    private TextView tvRetDepartTime, tvRetFromIata, tvRetDuration, tvRetArrivalTime, tvRetToIata;
    private TextView tvRetFlightNumber;
    private ImageView imgRetLogo;

    // Passenger input container
    private LinearLayout layoutPassengerList;

    // Price summary
    private TextView tvSubtotalPrice, tvGrandTotalPrice;

    // Button
    private MaterialButton btnBookNow;

    // DATA
    private boolean isRoundTrip;
    private int adultCount, childCount, babyCount;

    private double outBasePrice, outTaxFee;
    private double retBasePrice, retTaxFee;

    private ArrayList<PassengerInfo> passengerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        receiveExtras();
        setupToolbar();
        renderFlightInfo();
        generatePassengerForms();
        calculatePrice();
        setupClickListeners();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_booking);

        // Outbound flight
        tvOutRoute = findViewById(R.id.tv_outbound_route);
        tvOutDate = findViewById(R.id.tv_outbound_date);
        tvOutDepartTime = findViewById(R.id.tv_outbound_depart_time);
        tvOutFromIata = findViewById(R.id.tv_outbound_from_iata);
        tvOutDuration = findViewById(R.id.tv_outbound_duration);
        tvOutArrivalTime = findViewById(R.id.tv_outbound_arrival_time);
        tvOutToIata = findViewById(R.id.tv_outbound_to_iata);
        tvOutFlightNumber = findViewById(R.id.tv_outbound_flight_number);
        imgOutLogo = findViewById(R.id.img_outbound_airline_logo);

        // Return flight
        cardReturnFlight = findViewById(R.id.card_return_flight);
        tvRetRoute = findViewById(R.id.tv_return_route);
        tvRetDate = findViewById(R.id.tv_return_date);
        tvRetDepartTime = findViewById(R.id.tv_return_depart_time);
        tvRetFromIata = findViewById(R.id.tv_return_from_iata);
        tvRetDuration = findViewById(R.id.tv_return_duration);
        tvRetArrivalTime = findViewById(R.id.tv_return_arrival_time);
        tvRetToIata = findViewById(R.id.tv_return_to_iata);
        tvRetFlightNumber = findViewById(R.id.tv_return_flight_number);
        imgRetLogo = findViewById(R.id.img_return_airline_logo);

        // Passenger list
        layoutPassengerList = findViewById(R.id.layout_passenger_list);

        // Price
        tvSubtotalPrice = findViewById(R.id.tv_subtotal_price);
        tvGrandTotalPrice = findViewById(R.id.tv_grand_total_price);

        // Button
        btnBookNow = findViewById(R.id.btn_book_now);
    }

    // nhận dữ liệu từ
    private void receiveExtras() {
        Intent i = getIntent();

        isRoundTrip = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);
        adultCount = i.getIntExtra(EXTRA_ADULT_COUNT, 1);
        childCount = i.getIntExtra(EXTRA_CHILD_COUNT, 0);
        babyCount = i.getIntExtra(EXTRA_BABY_COUNT, 0);

        outBasePrice = i.getDoubleExtra(EXTRA_OUT_BASE_PRICE, 0);
        outTaxFee = i.getDoubleExtra(EXTRA_OUT_TAX_FEE, 0);

        if (isRoundTrip) {
            retBasePrice = i.getDoubleExtra(EXTRA_RET_BASE_PRICE, 0);
            retTaxFee = i.getDoubleExtra(EXTRA_RET_TAX_FEE, 0);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDER FLIGHT INFO
    // ══════════════════════════════════════════════════════════════════════

    private void renderFlightInfo() {
        Intent i = getIntent();

        // ─── Chuyến đi ───
        String outRoute = i.getStringExtra(EXTRA_OUT_FROM_CITY) + " -> " + i.getStringExtra(EXTRA_OUT_TO_CITY);
        tvOutRoute.setText(outRoute);
        tvOutDate.setText(i.getStringExtra(EXTRA_OUT_DATE));
        tvOutDepartTime.setText(i.getStringExtra(EXTRA_OUT_DEPART_TIME));
        tvOutFromIata.setText(i.getStringExtra(EXTRA_OUT_FROM_IATA));
        tvOutDuration.setText(i.getStringExtra(EXTRA_OUT_DURATION));
        tvOutArrivalTime.setText(i.getStringExtra(EXTRA_OUT_ARRIVAL_TIME));
        tvOutToIata.setText(i.getStringExtra(EXTRA_OUT_TO_IATA));
        tvOutFlightNumber.setText(i.getStringExtra(EXTRA_OUT_FLIGHT_NUMBER));

        Glide.with(this)
                .load(i.getStringExtra(EXTRA_OUT_AIRLINE_LOGO))
                .placeholder(R.drawable.ic_airline)
                .into(imgOutLogo);

        // ─── Chuyến về ───
        if (isRoundTrip) {
            cardReturnFlight.setVisibility(View.VISIBLE);

            String retRoute = i.getStringExtra(EXTRA_RET_FROM_CITY) + " -> " + i.getStringExtra(EXTRA_RET_TO_CITY);
            tvRetRoute.setText(retRoute);
            tvRetDate.setText(i.getStringExtra(EXTRA_RET_DATE));
            tvRetDepartTime.setText(i.getStringExtra(EXTRA_RET_DEPART_TIME));
            tvRetFromIata.setText(i.getStringExtra(EXTRA_RET_FROM_IATA));
            tvRetDuration.setText(i.getStringExtra(EXTRA_RET_DURATION));
            tvRetArrivalTime.setText(i.getStringExtra(EXTRA_RET_ARRIVAL_TIME));
            tvRetToIata.setText(i.getStringExtra(EXTRA_RET_TO_IATA));
            tvRetFlightNumber.setText(i.getStringExtra(EXTRA_RET_FLIGHT_NUMBER));

            Glide.with(this)
                    .load(i.getStringExtra(EXTRA_RET_AIRLINE_LOGO))
                    .placeholder(R.drawable.ic_airline)
                    .into(imgRetLogo);
        } else {
            cardReturnFlight.setVisibility(View.GONE);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERATE PASSENGER FORMS DYNAMICALLY
    // ══════════════════════════════════════════════════════════════════════
    private void generatePassengerForms() {
        layoutPassengerList.removeAllViews();
        passengerList.clear();

        LayoutInflater inflater = LayoutInflater.from(this);

        // ─── Người lớn ───
        for (int i = 0; i < adultCount; i++) {
            PassengerInfo p = new PassengerInfo("ADULT", i, "Người lớn " + (i + 1));
            passengerList.add(p);
            addPassengerRow(inflater, p, R.drawable.ic_nav_profile);
        }

        // ─── Trẻ em ───
        for (int i = 0; i < childCount; i++) {
            PassengerInfo p = new PassengerInfo("CHILD", i, "Trẻ em " + (i + 1));
            passengerList.add(p);
            addPassengerRow(inflater, p, R.drawable.ic_child);
        }

        // ─── Em bé ───
        for (int i = 0; i < babyCount; i++) {
            PassengerInfo p = new PassengerInfo("BABY", i, "Em bé " + (i + 1));
            passengerList.add(p);
            addPassengerRow(inflater, p, R.drawable.ic_baby);
        }
    }

    /**
     * Thêm 1 row passenger input vào layout.
     */
    private void addPassengerRow(LayoutInflater inflater, PassengerInfo passenger, int iconRes) {
        View row = inflater.inflate(R.layout.item_passenger_input, layoutPassengerList, false);

        ImageView imgIcon = row.findViewById(R.id.img_passenger_icon);
        TextView tvLabel = row.findViewById(R.id.tv_passenger_label);

        imgIcon.setImageResource(iconRes);
        tvLabel.setText(passenger.getLabel());

        // Click PassengerInputActivity để nhập thông tin chi tiết
        row.setOnClickListener(v -> openPassengerInput(passenger));

        layoutPassengerList.addView(row);
    }

    /**
     * Mở màn hình nhập thông tin chi tiết cho 1 hành khách.
     */
    private void openPassengerInput(PassengerInfo passenger) {
        Intent intent = new Intent(this, PassengerInputActivity.class);
        intent.putExtra("passenger", passenger);
        startActivity(intent);
    }

    private void calculatePrice() {
        double outTicketPrice = outBasePrice + outTaxFee;
        double retTicketPrice = isRoundTrip ? (retBasePrice + retTaxFee) : 0;

        // Tổng 1 chiều (hoặc 1 người nếu khứ hồi)
        double perPersonTotal = outTicketPrice + retTicketPrice;

        // Tính theo hành khách
        double adultTotal = perPersonTotal * adultCount;
        double childTotal = perPersonTotal * 0.75 * childCount;
        double babyTotal = perPersonTotal * 0.10 * babyCount;

        double grandTotal = adultTotal + childTotal + babyTotal;

        // Hiển thị
        tvSubtotalPrice.setText(formatPrice(grandTotal));
        tvGrandTotalPrice.setText(formatPrice(grandTotal));

    }

    private void setupClickListeners() {
        btnBookNow.setOnClickListener(v -> {
            // Validate: tất cả hành khách đã nhập đủ thông tin chưa
            boolean allComplete = true;
            for (PassengerInfo p : passengerList) {
                if (!p.isComplete()) {
                    allComplete = false;
                    break;
                }
            }

            if (!allComplete) {
                // TODO: Toast hoặc Dialog thông báo "Vui lòng nhập đầy đủ thông tin hành khách"
                return;
            }

            // TODO: Chuyển sang màn hình thanh toán (PaymentActivity)
        });
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}