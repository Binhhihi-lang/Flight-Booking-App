package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.ui.viewmodel.BookingInfoViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

/**
 * BookingInfoActivity – Màn hình nhập thông tin đặt vé.
 * <p>
 * NHẬN từ SearchFlightActivity:
 * - Thông tin chuyến đi / chuyến về
 * - Số lượng hành khách: adultCount, childCount, babyCount
 * - Giá vé: basePrice, taxFee
 * - Mã sơ đồ ghế: seatMapId (từ Aircraft)
 * - EXTRA_OUT_FARE_RULE_ID / EXTRA_RET_FARE_RULE_ID : ID quy định hạng vé (ID-Driven)
 * <p>
 * QUY TẮC GHÉP GHẾ:
 * - Người lớn (ADULT) : cần ghế riêng
 * - Trẻ em   (CHILD)  : cần ghế riêng
 * - Em bé    (BABY)   : ngồi cùng người lớn, KHÔNG cần ghế riêng
 * → seatsNeeded = adultCount + childCount
 */
public class BookingInfoActivity extends AppCompatActivity {

    // ══════════════════════════════════════════════════════════════════════
    // INTENT KEYS
    // Quy tắc đặt tên: EXTRA_{TRIP}_{FIELD}
    //   TRIP  = OUT (chuyến đi) | RET (chuyến về)
    //   FIELD = tên trường dữ liệu
    // ══════════════════════════════════════════════════════════════════════

    // ── Chuyến đi ─────────────────────────────────────────────────────────
    public static final String EXTRA_OUT_FLIGHT_ID    = "out_flight_id";
    public static final String EXTRA_OUT_FLIGHT_NUMBER= "out_flight_number";
    public static final String EXTRA_OUT_FARE_RULE_ID = "out_fare_rule_id";   // ← ID-Driven (đã fix)
    public static final String EXTRA_OUT_AIRLINE_LOGO = "out_airline_logo";
    public static final String EXTRA_OUT_AIRLINE_NAME = "out_airline_name";
    public static final String EXTRA_OUT_AIRCRAFT_NAME= "out_aircraft_name";
    public static final String EXTRA_OUT_SEAT_MAP_ID  = "out_seat_map_id";
    public static final String EXTRA_OUT_FROM_CITY    = "out_from_city";
    public static final String EXTRA_OUT_FROM_IATA    = "out_from_iata";
    public static final String EXTRA_OUT_TO_CITY      = "out_to_city";
    public static final String EXTRA_OUT_TO_IATA      = "out_to_iata";
    public static final String EXTRA_OUT_DEPART_TIME  = "out_depart_time";
    public static final String EXTRA_OUT_ARRIVAL_TIME = "out_arrival_time";
    public static final String EXTRA_OUT_DURATION     = "out_duration";
    public static final String EXTRA_OUT_DATE         = "out_date";
    public static final String EXTRA_OUT_BASE_PRICE   = "out_base_price";
    public static final String EXTRA_OUT_FARE_CLASS   = "out_fare_class";
    public static final String EXTRA_OUT_CHECKED_BAGGAGE = "out_checked_baggage";
    public static final String EXTRA_OUT_TAX_FEE      = "out_tax_fee";

    // ── Chuyến về (optional, chỉ khi khứ hồi) ────────────────────────────
    public static final String EXTRA_RET_FLIGHT_ID    = "ret_flight_id";
    public static final String EXTRA_RET_FLIGHT_NUMBER= "ret_flight_number";
    public static final String EXTRA_RET_FARE_RULE_ID = "ret_fare_rule_id";
    public static final String EXTRA_RET_AIRLINE_LOGO = "ret_airline_logo";
    public static final String EXTRA_RET_AIRLINE_NAME = "ret_airline_name";
    public static final String EXTRA_RET_AIRCRAFT_NAME= "ret_aircraft_name";
    public static final String EXTRA_RET_SEAT_MAP_ID  = "ret_seat_map_id";
    public static final String EXTRA_RET_FROM_CITY    = "ret_from_city";
    public static final String EXTRA_RET_FROM_IATA    = "ret_from_iata";
    public static final String EXTRA_RET_TO_CITY      = "ret_to_city";
    public static final String EXTRA_RET_TO_IATA      = "ret_to_iata";
    public static final String EXTRA_RET_DEPART_TIME  = "ret_depart_time";
    public static final String EXTRA_RET_ARRIVAL_TIME = "ret_arrival_time";
    public static final String EXTRA_RET_DURATION     = "ret_duration";
    public static final String EXTRA_RET_DATE         = "ret_date";
    public static final String EXTRA_RET_BASE_PRICE   = "ret_base_price";
    public static final String EXTRA_RET_TAX_FEE      = "ret_tax_fee";
    public static final String EXTRA_RET_FARE_CLASS   = "ret_fare_class";
    public static final String EXTRA_RET_CHECKED_BAGGAGE = "ret_checked_baggage";

    // ── Hành khách ────────────────────────────────────────────────────────
    public static final String EXTRA_ADULT_COUNT = "adult_count";
    public static final String EXTRA_CHILD_COUNT = "child_count";
    public static final String EXTRA_BABY_COUNT  = "baby_count";

    // ── Flags ─────────────────────────────────────────────────────────────
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";

    // ══════════════════════════════════════════════════════════════════════
    // Views
    // ══════════════════════════════════════════════════════════════════════

    private MaterialToolbar toolbar;

    private TextView tvOutRoute, tvOutDate;
    private TextView tvOutDepartTime, tvOutFromIata, tvOutDuration, tvOutArrivalTime, tvOutToIata;
    private TextView tvOutFlightNumber, tvOutFareClass;
    private ImageView imgOutLogo;

    private CardView cardReturnFlight;
    private CardView btnSelectSeat;
    private TextView tvRetRoute, tvRetDate;
    private TextView tvRetDepartTime, tvRetFromIata, tvRetDuration, tvRetArrivalTime, tvRetToIata;
    private TextView tvRetFlightNumber, tvRetFareClass;
    private ImageView imgRetLogo;

    private LinearLayout layoutPassengerList;
    private TextView tvSubtotalPrice, tvGrandTotalPrice;
    private MaterialButton btnBookNow;
    private TextView tvSeatSummary;

    // ══════════════════════════════════════════════════════════════════════
    // Data
    // ══════════════════════════════════════════════════════════════════════

    private BookingInfoViewModel viewModel;

    private boolean isRoundTrip;
    private int adultCount, childCount, babyCount;
    private double outBasePrice, outTaxFee;
    private double retBasePrice, retTaxFee;

    // ID để truyền sang SeatSelectionActivity (ID-Driven)
    private String outFareRuleId, retFareRuleId;

    // Dữ liệu truyền sang SeatSelectionActivity
    private String outboundFlightId, outSeatMapId, outAircraftName, outAirlineName, outFromIata, outToIata;
    private String returnFlightId,   retSeatMapId, retAircraftName, retAirlineName, retFromIata, retToIata;

    private final ArrayList<Passenger> passengerList = new ArrayList<>();
    private ArrayList<String> departSeatCodes = new ArrayList<>();
    private ArrayList<String> returnSeatCodes = new ArrayList<>();

    // registerForActivityResult PHẢI được khai báo ở field (trước onCreate)
    private final ActivityResultLauncher<Intent> seatSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ArrayList<String> dep = data.getStringArrayListExtra("selected_seats_depart");
                    ArrayList<String> ret = data.getStringArrayListExtra("selected_seats_return");
                    departSeatCodes = dep != null ? dep : new ArrayList<>();
                    returnSeatCodes = ret != null ? ret : new ArrayList<>();
                    mapSeatsToPassengers();
                }
            }
    );

    // ══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════

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
        renderFlightInfo();         // đọc Intent → gán field + render UI
        generatePassengerForms();
        calculatePrice();
        setupViewModel();
        setupClickListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Setup
    // ══════════════════════════════════════════════════════════════════════

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_booking);

        tvOutRoute      = findViewById(R.id.tv_outbound_route);
        tvOutDate       = findViewById(R.id.tv_outbound_date);
        tvOutDepartTime = findViewById(R.id.tv_outbound_depart_time);
        tvOutFromIata   = findViewById(R.id.tv_outbound_from_iata);
        tvOutDuration   = findViewById(R.id.tv_outbound_duration);
        tvOutArrivalTime= findViewById(R.id.tv_outbound_arrival_time);
        tvOutToIata     = findViewById(R.id.tv_outbound_to_iata);
        tvOutFlightNumber= findViewById(R.id.tv_outbound_flight_number);
        imgOutLogo      = findViewById(R.id.img_outbound_airline_logo);
        tvOutFareClass  = findViewById(R.id.tv_outbound_fare_class);

        cardReturnFlight= findViewById(R.id.card_return_flight);
        tvRetRoute      = findViewById(R.id.tv_return_route);
        tvRetDate       = findViewById(R.id.tv_return_date);
        tvRetDepartTime = findViewById(R.id.tv_return_depart_time);
        tvRetFromIata   = findViewById(R.id.tv_return_from_iata);
        tvRetDuration   = findViewById(R.id.tv_return_duration);
        tvRetArrivalTime= findViewById(R.id.tv_return_arrival_time);
        tvRetToIata     = findViewById(R.id.tv_return_to_iata);
        tvRetFlightNumber= findViewById(R.id.tv_return_flight_number);
        imgRetLogo      = findViewById(R.id.img_return_airline_logo);
        tvRetFareClass  = findViewById(R.id.tv_return_fare_class);

        btnSelectSeat       = findViewById(R.id.card_select_seat);
        layoutPassengerList = findViewById(R.id.layout_passenger_list);
        tvSubtotalPrice     = findViewById(R.id.tv_subtotal_price);
        tvGrandTotalPrice   = findViewById(R.id.tv_grand_total_price);
        btnBookNow          = findViewById(R.id.btn_book_now);
        tvSeatSummary       = findViewById(R.id.tv_passenger_seat);
    }


    private void renderFlightInfo() {
        Intent i = getIntent();

        isRoundTrip  = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);
        adultCount   = i.getIntExtra(EXTRA_ADULT_COUNT, 1);
        childCount   = i.getIntExtra(EXTRA_CHILD_COUNT, 0);
        babyCount    = i.getIntExtra(EXTRA_BABY_COUNT,  0);
        outBasePrice = i.getDoubleExtra(EXTRA_OUT_BASE_PRICE, 0);
        outTaxFee    = i.getDoubleExtra(EXTRA_OUT_TAX_FEE, 0);

        // ── ID-Driven: chỉ lưu ID, KHÔNG lưu Object ──────────────────────
        outFareRuleId = i.getStringExtra(EXTRA_OUT_FARE_RULE_ID);
        retFareRuleId = i.getStringExtra(EXTRA_RET_FARE_RULE_ID); // null nếu 1 chiều

        // ── Dữ liệu truyền tiếp sang SeatSelectionActivity ───────────────
        outboundFlightId = i.getStringExtra(EXTRA_OUT_FLIGHT_ID);
        outSeatMapId     = i.getStringExtra(EXTRA_OUT_SEAT_MAP_ID);
        outAircraftName  = i.getStringExtra(EXTRA_OUT_AIRCRAFT_NAME);
        outAirlineName   = i.getStringExtra(EXTRA_OUT_AIRLINE_NAME);
        outFromIata      = i.getStringExtra(EXTRA_OUT_FROM_IATA);
        outToIata        = i.getStringExtra(EXTRA_OUT_TO_IATA);

        // ── Render UI: chuyến đi ──────────────────────────────────────────
        tvOutRoute.setText(i.getStringExtra(EXTRA_OUT_FROM_CITY) + " → " + i.getStringExtra(EXTRA_OUT_TO_CITY));
        tvOutDate.setText(i.getStringExtra(EXTRA_OUT_DATE));
        tvOutDepartTime.setText(i.getStringExtra(EXTRA_OUT_DEPART_TIME));
        tvOutFromIata.setText(outFromIata);
        tvOutDuration.setText(i.getStringExtra(EXTRA_OUT_DURATION));
        tvOutArrivalTime.setText(i.getStringExtra(EXTRA_OUT_ARRIVAL_TIME));
        tvOutToIata.setText(outToIata);
        tvOutFlightNumber.setText(i.getStringExtra(EXTRA_OUT_FLIGHT_NUMBER));
        tvOutFareClass.setText(i.getStringExtra(EXTRA_OUT_FARE_CLASS));
        Glide.with(this).load(i.getStringExtra(EXTRA_OUT_AIRLINE_LOGO))
                .placeholder(R.drawable.ic_airline).into(imgOutLogo);

        // ── Render UI: chuyến về (chỉ khi khứ hồi) ───────────────────────
        if (isRoundTrip) {
            retBasePrice     = i.getDoubleExtra(EXTRA_RET_BASE_PRICE, 0);
            retTaxFee        = i.getDoubleExtra(EXTRA_RET_TAX_FEE, 0);
            returnFlightId   = i.getStringExtra(EXTRA_RET_FLIGHT_ID);
            retSeatMapId     = i.getStringExtra(EXTRA_RET_SEAT_MAP_ID);
            retAircraftName  = i.getStringExtra(EXTRA_RET_AIRCRAFT_NAME);
            retAirlineName   = i.getStringExtra(EXTRA_RET_AIRLINE_NAME);
            retFromIata      = i.getStringExtra(EXTRA_RET_FROM_IATA);
            retToIata        = i.getStringExtra(EXTRA_RET_TO_IATA);

            cardReturnFlight.setVisibility(View.VISIBLE);
            tvRetRoute.setText(i.getStringExtra(EXTRA_RET_FROM_CITY) + " → " + i.getStringExtra(EXTRA_RET_TO_CITY));
            tvRetDate.setText(i.getStringExtra(EXTRA_RET_DATE));
            tvRetDepartTime.setText(i.getStringExtra(EXTRA_RET_DEPART_TIME));
            tvRetFromIata.setText(retFromIata);
            tvRetDuration.setText(i.getStringExtra(EXTRA_RET_DURATION));
            tvRetArrivalTime.setText(i.getStringExtra(EXTRA_RET_ARRIVAL_TIME));
            tvRetToIata.setText(retToIata);
            tvRetFlightNumber.setText(i.getStringExtra(EXTRA_RET_FLIGHT_NUMBER));
            tvRetFareClass.setText(i.getStringExtra(EXTRA_RET_FARE_CLASS));
            Glide.with(this).load(i.getStringExtra(EXTRA_RET_AIRLINE_LOGO))
                    .placeholder(R.drawable.ic_airline).into(imgRetLogo);
        } else {
            cardReturnFlight.setVisibility(View.GONE);
        }
    }

    /**
     * Khởi tạo ViewModel và kích hoạt load FareRule từ Firebase.
     * Observe lỗi để hiển thị Toast nếu Firebase thất bại.
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BookingInfoViewModel.class);

        // ------------------------------------------------------------------
        // 1. QUAN SÁT TRẠNG THÁI TẢI DỮ LIỆU (LOADING / SUCCESS / ERROR)
        // ------------------------------------------------------------------
        viewModel.getLoadingLive().observe(this, state -> {
            if (state == null) return;

            // Xử lý thanh Progress Bar (Ví dụ bạn có một view là progressBar)
            // progressBar.setVisibility(state.getStatus() == AuthResult.Status.LOADING ? View.VISIBLE : View.GONE);

            // Xử lý thông báo lỗi nếu có
            if (state.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(this,
                        "Lỗi tải quy định vé: " + state.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            // Nếu SUCCESS, bạn có thể mở khóa nút "Chọn ghế" ở đây nếu trước đó bạn khóa lại để đợi data
            // if (state.getStatus() == AuthResult.Status.SUCCESS) {
            //     btnSelectSeat.setEnabled(true);
            // }
        });

        // ------------------------------------------------------------------
        // 2. QUAN SÁT QUY ĐỊNH VÉ LƯỢT ĐI
        // ------------------------------------------------------------------
        viewModel.getOutboundFareRuleLive().observe(this, outRule -> {
            if (outRule != null) {
                // (Tùy chọn) Cập nhật UI hiển thị hành lý hoặc quyền lợi lượt đi cho khách xem
                // tvOutCabinBaggage.setText(outRule.getCabinBaggage() + "kg");
            }
        });

        // 3. QUAN SÁT QUY ĐỊNH VÉ LƯỢT VỀ (NẾU LÀ VÉ KHỨ HỒI)
        if (isRoundTrip) {
            viewModel.getReturnFareRuleLive().observe(this, retRule -> {
                if (retRule != null) {
                    // (Tùy chọn) Cập nhật UI hiển thị hành lý lượt về
                }
            });
        }

        // Truyền ID lượt đi, và ID lượt về (nếu có) để ViewModel bắt đầu fetch data
        viewModel.loadFareRules(outFareRuleId, isRoundTrip ? retFareRuleId : null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Passenger forms
    // ══════════════════════════════════════════════════════════════════════

    private void generatePassengerForms() {
        layoutPassengerList.removeAllViews();
        passengerList.clear();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int idx = 0; idx < adultCount; idx++) {
            Passenger p = new Passenger("ADULT", idx, "Người lớn " + (idx + 1));
            passengerList.add(p);
            addPassengerRow(inflater, p, R.drawable.ic_nav_profile);
        }
        for (int idx = 0; idx < childCount; idx++) {
            Passenger p = new Passenger("CHILD", idx, "Trẻ em " + (idx + 1));
            passengerList.add(p);
            addPassengerRow(inflater, p, R.drawable.ic_child);
        }
        for (int idx = 0; idx < babyCount; idx++) {
            Passenger p = new Passenger("BABY", idx, "Em bé " + (idx + 1));
            passengerList.add(p);
            addPassengerRow(inflater, p, R.drawable.ic_baby);
        }
    }

    private void addPassengerRow(LayoutInflater inflater, Passenger passenger, int iconRes) {
        View row = inflater.inflate(R.layout.item_passenger_input, layoutPassengerList, false);
        ((ImageView) row.findViewById(R.id.img_passenger_icon)).setImageResource(iconRes);
        ((TextView)  row.findViewById(R.id.tv_passenger_label)).setText(passenger.getLabel());
        row.setOnClickListener(v -> openPassengerInput(passenger));
        layoutPassengerList.addView(row);
    }

    private void openPassengerInput(Passenger passenger) {
        Intent intent = new Intent(this, PassengerInputActivity.class);
        intent.putExtra("passenger", passenger);
        startActivity(intent);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Price calculation
    // ══════════════════════════════════════════════════════════════════════

    private void calculatePrice() {
        double perPerson = (outBasePrice + outTaxFee)
                + (isRoundTrip ? (retBasePrice + retTaxFee) : 0);

        double grandTotal = (perPerson * adultCount)
                + (perPerson * 0.75 * childCount)
                + (perPerson * 0.10 * babyCount);

        tvSubtotalPrice.setText(formatPrice(grandTotal));
        tvGrandTotalPrice.setText(formatPrice(grandTotal));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Click listeners
    // ══════════════════════════════════════════════════════════════════════

    private void setupClickListeners() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSelectSeat.setOnClickListener(v -> openSeatSelection());

        btnBookNow.setOnClickListener(v -> {
            for (Passenger p : passengerList) {
                if (!p.isComplete()) {
                    Toast.makeText(this,
                            "Vui lòng nhập đầy đủ thông tin hành khách!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // TODO: Chuyển sang PaymentActivity
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Seat selection
    // ══════════════════════════════════════════════════════════════════════

    private void openSeatSelection() {
        int seatsNeeded = adultCount + childCount;

        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_MAX_PASSENGERS, seatsNeeded);
        intent.putExtra(SeatSelectionActivity.EXTRA_IS_ROUND_TRIP,  isRoundTrip);

        // để nhét thẳng vào Intent truyền sang màn hình Chọn ghế.
        if (viewModel != null) {
            viewModel.buildSeatSelectionIntent(intent, isRoundTrip);
        }

        // ── Chuyến đi ─────────────────────────────────────────────────────
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_FLIGHT_ID,    outboundFlightId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_MAP_ID,  outSeatMapId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRCRAFT_NAME,outAircraftName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRLINE_NAME, outAirlineName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_FROM_IATA, outFromIata);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_TO_IATA,   outToIata);


        // ── Chuyến về (chỉ khi khứ hồi) ──────────────────────────────────
        if (isRoundTrip) {
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_FLIGHT_ID,     returnFlightId);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_MAP_ID,   retSeatMapId);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_AIRCRAFT_NAME, retAircraftName);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_AIRLINE_NAME,  retAirlineName);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_FROM_IATA,retFromIata);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_TO_IATA,  retToIata);
        }

        seatSelectionLauncher.launch(intent);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Seat → Passenger mapping
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Ánh xạ ghế đã chọn vào từng hành khách (ADULT → CHILD → bỏ qua BABY).
     */
    private void mapSeatsToPassengers() {
        int departIndex = 0;
        int returnIndex = 0;

        for (int i = 0; i < layoutPassengerList.getChildCount(); i++) {
            View row = layoutPassengerList.getChildAt(i);
            TextView tvSeat = row.findViewById(R.id.tv_passenger_seat);

            Passenger p = passengerList.get(i);

            if ("BABY".equals(p.getType())) {
                tvSeat.setText("Ngồi cùng ng.lớn");
                tvSeat.setTextColor(Color.GRAY);
                continue;
            }

            StringBuilder seatDisplayBuilder = new StringBuilder();

            if (isRoundTrip) {
                if (departSeatCodes != null && departIndex < departSeatCodes.size()) {
                    seatDisplayBuilder.append("Đi: ").append(departSeatCodes.get(departIndex));
                    p.setSeatNumber(departSeatCodes.get(departIndex));
                    departIndex++;
                } else {
                    seatDisplayBuilder.append("Đi: --");
                }
                if (returnSeatCodes != null && returnIndex < returnSeatCodes.size()) {
                    seatDisplayBuilder.append(" | Về: ").append(returnSeatCodes.get(returnIndex));
                    returnIndex++;
                } else {
                    seatDisplayBuilder.append(" | Về: --");
                }
            } else {
                if (departSeatCodes != null && departIndex < departSeatCodes.size()) {
                    seatDisplayBuilder.append(departSeatCodes.get(departIndex));
                    p.setSeatNumber(departSeatCodes.get(departIndex));
                    departIndex++;
                } else {
                    seatDisplayBuilder.append("--");
                }
            }

            tvSeat.setText(seatDisplayBuilder.toString());
            tvSeat.setTextColor(Color.parseColor("#1565C0"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}