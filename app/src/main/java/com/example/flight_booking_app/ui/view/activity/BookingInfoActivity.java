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

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Passenger;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

/**
 * BookingInfoActivity – Màn hình nhập thông tin đặt vé.
 *
 * NHẬN từ SearchFlightActivity:
 *   - Thông tin chuyến đi / chuyến về
 *   - Số lượng hành khách: adultCount, childCount, babyCount
 *   - Giá vé: basePrice, taxFee
 *   - Mã sơ đồ ghế: seatMapId (từ Aircraft)
 *
 * QUY TẮC GHÉP GHẾ:
 *   - Người lớn (ADULT) : cần ghế riêng
 *   - Trẻ em   (CHILD)  : cần ghế riêng
 *   - Em bé    (BABY)   : ngồi cùng người lớn, KHÔNG cần ghế riêng
 *   → seatsNeeded = adultCount + childCount
 *
 * TÍNH GIÁ VÉ:
 *   - Người lớn: 100%
 *   - Trẻ em:    75%
 *   - Em bé:     10%
 */
public class BookingInfoActivity extends AppCompatActivity {

    // ══════════════════════════════════════════════════════════════════════
    // INTENT KEYS
    // Quy tắc đặt tên: EXTRA_{TRIP}_{FIELD}
    //   TRIP  = OUT (chuyến đi) | RET (chuyến về)
    //   FIELD = tên trường dữ liệu
    // ══════════════════════════════════════════════════════════════════════

    // ── Chuyến đi ─────────────────────────────────────────────────────────
    public static final String EXTRA_OUT_FLIGHT_NUMBER   = "out_flight_number";
    public static final String EXTRA_OUT_AIRLINE_LOGO    = "out_airline_logo";
    public static final String EXTRA_OUT_AIRLINE_NAME    = "out_airline_name";   // FIX: trước = "Boeing 737"
    public static final String EXTRA_OUT_AIRCRAFT_NAME   = "out_aircraft_name";  // FIX: trước = "Airbus A320"
    public static final String EXTRA_OUT_SEAT_MAP_ID     = "out_seat_map_id";    // FIX: trước = "seat_map_id" → trùng với RET
    public static final String EXTRA_OUT_FROM_CITY       = "out_from_city";
    public static final String EXTRA_OUT_FROM_IATA       = "out_from_iata";
    public static final String EXTRA_OUT_TO_CITY         = "out_to_city";
    public static final String EXTRA_OUT_TO_IATA         = "out_to_iata";
    public static final String EXTRA_OUT_DEPART_TIME     = "out_depart_time";
    public static final String EXTRA_OUT_ARRIVAL_TIME    = "out_arrival_time";
    public static final String EXTRA_OUT_DURATION        = "out_duration";
    public static final String EXTRA_OUT_DATE            = "out_date";
    public static final String EXTRA_OUT_BASE_PRICE      = "out_base_price";
    public static final String EXTRA_OUT_FARE_CLASS      = "out_fare_class";
    public static final String EXTRA_OUT_CHECKED_BAGGAGE = "out_checked_baggage";
    public static final String EXTRA_OUT_TAX_FEE         = "out_tax_fee";

    // ── Chuyến về (optional, chỉ khi khứ hồi) ────────────────────────────
    public static final String EXTRA_RET_FLIGHT_NUMBER   = "ret_flight_number";
    public static final String EXTRA_RET_AIRLINE_LOGO    = "ret_airline_logo";
    public static final String EXTRA_RET_AIRLINE_NAME    = "ret_airline_name";   // FIX: trước = ""
    public static final String EXTRA_RET_AIRCRAFT_NAME   = "ret_aircraft_name";  // FIX: trước = ""
    public static final String EXTRA_RET_SEAT_MAP_ID     = "ret_seat_map_id";    // FIX: trước = "seat_map_id" → trùng OUT
    public static final String EXTRA_RET_FROM_CITY       = "ret_from_city";
    public static final String EXTRA_RET_FROM_IATA       = "ret_from_iata";
    public static final String EXTRA_RET_TO_CITY         = "ret_to_city";
    public static final String EXTRA_RET_TO_IATA         = "ret_to_iata";
    public static final String EXTRA_RET_DEPART_TIME     = "ret_depart_time";
    public static final String EXTRA_RET_ARRIVAL_TIME    = "ret_arrival_time";
    public static final String EXTRA_RET_DURATION        = "ret_duration";
    public static final String EXTRA_RET_DATE            = "ret_date";
    public static final String EXTRA_RET_BASE_PRICE      = "ret_base_price";
    public static final String EXTRA_RET_TAX_FEE         = "ret_tax_fee";
    public static final String EXTRA_RET_FARE_CLASS      = "ret_fare_class";

    // ── Hành khách ────────────────────────────────────────────────────────
    public static final String EXTRA_ADULT_COUNT         = "adult_count";
    public static final String EXTRA_CHILD_COUNT         = "child_count";
    public static final String EXTRA_BABY_COUNT          = "baby_count";

    // ── Flags ─────────────────────────────────────────────────────────────
    public static final String EXTRA_IS_ROUND_TRIP       = "is_round_trip";

    // ══════════════════════════════════════════════════════════════════════
    // Views
    // ══════════════════════════════════════════════════════════════════════
    private MaterialToolbar toolbar;

    private TextView  tvOutRoute, tvOutDate;
    private TextView  tvOutDepartTime, tvOutFromIata, tvOutDuration, tvOutArrivalTime, tvOutToIata;
    private TextView  tvOutFlightNumber, tvOutFareClass;
    private ImageView imgOutLogo;

    private CardView  cardReturnFlight;
    private CardView  btnSelectSeat;
    private TextView  tvRetRoute, tvRetDate;
    private TextView  tvRetDepartTime, tvRetFromIata, tvRetDuration, tvRetArrivalTime, tvRetToIata;
    private TextView  tvRetFlightNumber, tvRetFareClass;
    private ImageView imgRetLogo;

    private LinearLayout  layoutPassengerList;
    private TextView      tvSubtotalPrice, tvGrandTotalPrice;
    private MaterialButton btnBookNow;
    private TextView tvSeatSummary;

    // ══════════════════════════════════════════════════════════════════════
    // Data
    // ══════════════════════════════════════════════════════════════════════
    private boolean isRoundTrip;
    private int     adultCount, childCount, babyCount;
    private double  outBasePrice, outTaxFee;
    private double  retBasePrice, retTaxFee;

    // Dữ liệu truyền sang SeatSelectionActivity
    private String outSeatMapId, outAircraftName, outAirlineName, outFromIata, outToIata;
    private String retSeatMapId, retAircraftName, retAirlineName, retFromIata, retToIata;

    private final ArrayList<Passenger> passengerList     = new ArrayList<>();
    private       ArrayList<String>    selectedSeatCodes = new ArrayList<>();

    // registerForActivityResult PHẢI được gọi trước onCreate → khai báo trực tiếp ở field
    // 1. Tạo 2 biến danh sách lưu giữ mã ghế toàn cục tại BookingInfoActivity
    private ArrayList<String> departSeatCodes = new ArrayList<>();
    private ArrayList<String> returnSeatCodes = new ArrayList<>();

    // 2. Chỉnh sửa bộ nhận kết quả Launcher
    private final ActivityResultLauncher<Intent> seatSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ArrayList<String> dep = data.getStringArrayListExtra("selected_seats_depart");
                    ArrayList<String> ret = data.getStringArrayListExtra("selected_seats_return");
                    // Bảo vệ null: SeatSelectionActivity luôn trả về list (có thể rỗng)
                    departSeatCodes = dep != null ? dep : new ArrayList<>();
                    returnSeatCodes = ret != null ? ret : new ArrayList<>();

                    // Cập nhật giao diện: gán số ghế lên từng hàng hành khách
                    mapSeatsToPassengers();
                    // Cập nhật nút chọn ghế để phản ánh ghế đã chọn
                    updateSeatButtonLabel();
                }
            }
    );

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
        setupToolbar();
        renderFlightInfo();
        generatePassengerForms();
        calculatePrice();
        setupClickListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Setup
    // ══════════════════════════════════════════════════════════════════════

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_booking);

        tvOutRoute        = findViewById(R.id.tv_outbound_route);
        tvOutDate         = findViewById(R.id.tv_outbound_date);
        tvOutDepartTime   = findViewById(R.id.tv_outbound_depart_time);
        tvOutFromIata     = findViewById(R.id.tv_outbound_from_iata);
        tvOutDuration     = findViewById(R.id.tv_outbound_duration);
        tvOutArrivalTime  = findViewById(R.id.tv_outbound_arrival_time);
        tvOutToIata       = findViewById(R.id.tv_outbound_to_iata);
        tvOutFlightNumber = findViewById(R.id.tv_outbound_flight_number);
        imgOutLogo        = findViewById(R.id.img_outbound_airline_logo);
        tvOutFareClass    = findViewById(R.id.tv_outbound_fare_class);

        cardReturnFlight  = findViewById(R.id.card_return_flight);
        tvRetRoute        = findViewById(R.id.tv_return_route);
        tvRetDate         = findViewById(R.id.tv_return_date);
        tvRetDepartTime   = findViewById(R.id.tv_return_depart_time);
        tvRetFromIata     = findViewById(R.id.tv_return_from_iata);
        tvRetDuration     = findViewById(R.id.tv_return_duration);
        tvRetArrivalTime  = findViewById(R.id.tv_return_arrival_time);
        tvRetToIata       = findViewById(R.id.tv_return_to_iata);
        tvRetFlightNumber = findViewById(R.id.tv_return_flight_number);
        imgRetLogo        = findViewById(R.id.img_return_airline_logo);
        tvRetFareClass    = findViewById(R.id.tv_return_fare_class);

        btnSelectSeat     = findViewById(R.id.card_select_seat);
        layoutPassengerList = findViewById(R.id.layout_passenger_list);
        tvSubtotalPrice   = findViewById(R.id.tv_subtotal_price);
        tvGrandTotalPrice = findViewById(R.id.tv_grand_total_price);
        btnBookNow        = findViewById(R.id.btn_book_now);
        tvSeatSummary = findViewById(R.id.tv_passenger_seat);

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void renderFlightInfo() {
        Intent i = getIntent();

        isRoundTrip  = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);
        adultCount   = i.getIntExtra(EXTRA_ADULT_COUNT, 1);
        childCount   = i.getIntExtra(EXTRA_CHILD_COUNT, 0);
        babyCount    = i.getIntExtra(EXTRA_BABY_COUNT, 0);
        outBasePrice = i.getDoubleExtra(EXTRA_OUT_BASE_PRICE, 0);
        outTaxFee    = i.getDoubleExtra(EXTRA_OUT_TAX_FEE, 0);

        // Dữ liệu truyền sang SeatSelectionActivity
        outSeatMapId    = i.getStringExtra(EXTRA_OUT_SEAT_MAP_ID);
        outAircraftName = i.getStringExtra(EXTRA_OUT_AIRCRAFT_NAME);
        outAirlineName  = i.getStringExtra(EXTRA_OUT_AIRLINE_NAME);
        outFromIata     = i.getStringExtra(EXTRA_OUT_FROM_IATA);
        outToIata       = i.getStringExtra(EXTRA_OUT_TO_IATA);

        // ── Chuyến đi ─────────────────────────────────────────────────────
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

        // ── Chuyến về (chỉ khi khứ hồi) ──────────────────────────────────
        if (isRoundTrip) {
            retBasePrice    = i.getDoubleExtra(EXTRA_RET_BASE_PRICE, 0);
            retTaxFee       = i.getDoubleExtra(EXTRA_RET_TAX_FEE, 0);
            retSeatMapId    = i.getStringExtra(EXTRA_RET_SEAT_MAP_ID);
            retAircraftName = i.getStringExtra(EXTRA_RET_AIRCRAFT_NAME);
            retAirlineName  = i.getStringExtra(EXTRA_RET_AIRLINE_NAME);
            retFromIata     = i.getStringExtra(EXTRA_RET_FROM_IATA);
            retToIata       = i.getStringExtra(EXTRA_RET_TO_IATA);

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
            // Em bé: tạo hành khách nhưng KHÔNG cấp ghế riêng
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

    private void calculatePrice() {
        double perPerson = (outBasePrice + outTaxFee)
                + (isRoundTrip ? (retBasePrice + retTaxFee) : 0);

        double grandTotal = (perPerson * adultCount)
                + (perPerson * 0.75 * childCount)
                + (perPerson * 0.10 * babyCount);

        tvSubtotalPrice.setText(formatPrice(grandTotal));
        tvGrandTotalPrice.setText(formatPrice(grandTotal));
    }

    private void setupClickListeners() {
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

    /**
     * Mở màn hình chọn ghế.
     * seatsNeeded = adultCount + childCount (em bé BABY không cần ghế riêng).
     */
    private void openSeatSelection() {
        int seatsNeeded = adultCount + childCount;

        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_MAX_PASSENGERS,  seatsNeeded);
        intent.putExtra(SeatSelectionActivity.EXTRA_IS_ROUND_TRIP,   isRoundTrip);

        // Chuyến đi
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_MAP_ID,   outSeatMapId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRCRAFT_NAME, outAircraftName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRLINE_NAME,  outAirlineName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_FROM_IATA, outFromIata);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_TO_IATA,   outToIata);

        // Chuyến về (chỉ khi khứ hồi)
        if (isRoundTrip) {
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_MAP_ID,   retSeatMapId);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_AIRCRAFT_NAME, retAircraftName);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_AIRLINE_NAME,  retAirlineName);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_FROM_IATA, retFromIata);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_TO_IATA,   retToIata);
        }

        seatSelectionLauncher.launch(intent);
    }

    /**
     * Ánh xạ ghế đã chọn vào từng hành khách (ADULT → CHILD → bỏ qua BABY).
     */
    // 3. Viết lại hàm ánh xạ hiển thị số ghế chi tiết cho cả 2 chiều Đi - Về độc lập
    private void mapSeatsToPassengers() {
        int departIndex = 0;
        int returnIndex = 0;

        for (int i = 0; i < layoutPassengerList.getChildCount(); i++) {
            View row = layoutPassengerList.getChildAt(i);
            TextView tvSeat = row.findViewById(R.id.tv_passenger_seat);
            if (tvSeat == null) continue;

            Passenger p = passengerList.get(i);

            // Quy tắc bỏ qua cấp ghế cho em bé sơ sinh ngồi chung lòng
            if ("BABY".equals(p.getType())) {
                tvSeat.setText("Ngồi cùng ng.lớn");
                tvSeat.setTextColor(Color.GRAY);
                continue;
            }

            StringBuilder seatDisplayBuilder = new StringBuilder();

            // Xử lý thông tin hiển thị Ghế chiều đi
            if (departSeatCodes != null && departIndex < departSeatCodes.size()) {
                seatDisplayBuilder.append("Đi: ").append(departSeatCodes.get(departIndex));
                p.setSeatNumber(departSeatCodes.get(departIndex)); // Đóng gói lưu vào Object hành khách
                departIndex++;
            } else {
                seatDisplayBuilder.append("Đi: --");
            }

            // Xử lý thông tin hiển thị Ghế chiều về (chỉ render nếu chọn khứ hồi)
            if (isRoundTrip) {
                if (returnSeatCodes != null && returnIndex < returnSeatCodes.size()) {
                    seatDisplayBuilder.append(" | Về: ").append(returnSeatCodes.get(returnIndex));
                    // Bạn có thể thiết lập thêm trường returnSeatNumber trong Passenger model nếu cần quản lý nâng cao
                    returnIndex++;
                } else {
                    seatDisplayBuilder.append(" | Về: --");
                }
            }

            tvSeat.setText(seatDisplayBuilder.toString());
            tvSeat.setTextColor(Color.parseColor("#1565C0"));
        }
    }

    /**
     * Cập nhật label trên nút "Chọn ghế ngồi" sau khi nhận kết quả từ SeatSelectionActivity.
     * Hiển thị tóm tắt ghế đã chọn: "Đi: 4A, 5B | Về: 3C, 3D" hoặc "4A, 5B" nếu 1 chiều.
     */
    private void updateSeatButtonLabel() {
        if (tvSeatSummary == null) return; // View chưa có → bỏ qua

        if (departSeatCodes.isEmpty()) {
            tvSeatSummary.setText("Chọn ghế ngồi");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (isRoundTrip) {
            sb.append("Đi: ").append(String.join(", ", departSeatCodes));
            if (!returnSeatCodes.isEmpty()) {
                sb.append(" | Về: ").append(String.join(", ", returnSeatCodes));
            }
        } else {
            sb.append(String.join(", ", departSeatCodes));
        }
        tvSeatSummary.setText(sb.toString());
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}