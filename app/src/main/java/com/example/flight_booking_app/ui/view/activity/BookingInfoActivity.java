package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.Flight;
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

    // ── Chuyến đi ─────────────────────────────────────────────────────────
    private Flight outboundFlight;
    private Flight returnFlight;
    private FareClass outboundFare;
    private FareClass returnFare;
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
    public static final String EXTRA_OUT_FARE_CLASS_ID   = "out_fare_class_id";
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
    public static final String EXTRA_RET_FARE_CLASS_ID   = "ret_fare_class_id";
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

    private ArrayList<String> depSeats;

    private ArrayList<String> retSeats;

    //
    private static final int ICON_ADULT = R.drawable.ic_nav_profile;
    private static final int ICON_CHILD = R.drawable.ic_child;
    private static final int ICON_BABY = R.drawable.ic_baby;
    private static final int ICON_CHECKED = R.drawable.ic_checked;

    private static final int COLOR_COMPLETE = Color.parseColor("#0175F3");

    private final ActivityResultLauncher<Intent> seatSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    depSeats = data.getStringArrayListExtra("selected_seats_depart");
                    retSeats = data.getStringArrayListExtra("selected_seats_return");

                    // cho ViewModel
                    viewModel.updateSeats(depSeats, retSeats, isRoundTrip);
                }
            }
    );

    private final ActivityResultLauncher<Intent> passengerInputLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Passenger updatedPassenger = (Passenger) result.getData().getSerializableExtra("updated_passenger");
                    if (updatedPassenger != null) {
                        // ViewModel cập nhật
                        viewModel.updatePassenger(updatedPassenger);
                    }
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
        Intent intent = getIntent();

        // 1. NHẬN CÁC OBJECT NGUYÊN KHỐI VÀ ĐẾM SỐ LƯỢNG KHÁCH
        isRoundTrip  = intent.getBooleanExtra("is_round_trip", false);
        adultCount   = intent.getIntExtra("adult_count", 1);
        childCount   = intent.getIntExtra("child_count", 0);
        babyCount    = intent.getIntExtra("baby_count",  0);

        outboundFlight = (Flight) intent.getSerializableExtra("selected_outbound_flight");
        outboundFare   = (FareClass) intent.getSerializableExtra("selected_outbound_fare");

        if (isRoundTrip) {
            returnFlight = (Flight) intent.getSerializableExtra("selected_return_flight");
            returnFare   = (FareClass) intent.getSerializableExtra("selected_return_fare");
        }

        // Kiểm tra an toàn dữ liệu trước khi render
        if (outboundFlight == null || outboundFare == null) return;

        // 2. GÁN CÁC BIẾN GLOBAL PHỤC VỤ TRUYỀN SANG SEAT_SELECTION & TÍNH TIỀN
        outBasePrice     = outboundFare.getBasePrice(); // Lấy giá từ Hạng vé
        outTaxFee        = outboundFlight.getTaxFee(); // Thuế phí từ Chuyến bay

        outFareRuleId    = outboundFare.getFareRuleId();
        outboundFlightId = outboundFlight.getFlightId();
        outSeatMapId     = outboundFlight.getSeatMapId();
        outAircraftName  = outboundFlight.getAirCraftName();
        outAirlineName   = outboundFlight.getAirlineName();
        outFromIata      = outboundFlight.getFromIata();
        outToIata        = outboundFlight.getToIata();

        // 3. RENDER UI: CHUYẾN ĐI
        tvOutRoute.setText(outboundFlight.getFrom() + " → " + outboundFlight.getTo());
        tvOutDate.setText(outboundFlight.getDepartureDate());
        tvOutDepartTime.setText(outboundFlight.getDepartureTime());
        tvOutFromIata.setText(outFromIata);
        tvOutDuration.setText(outboundFlight.getDuration());
        tvOutArrivalTime.setText(outboundFlight.getArrivalTime());
        tvOutToIata.setText(outToIata);
        tvOutFlightNumber.setText(outboundFlight.getFlightNumber());
        tvOutFareClass.setText(outboundFare.getTitle()); // Tên hạng vé

        Glide.with(this)
                .load(outboundFlight.getAirlineLogo())
                .placeholder(R.drawable.ic_airline)
                .into(imgOutLogo);

        // 4. RENDER UI: CHUYẾN VỀ (Chỉ khi khứ hồi)
        if (isRoundTrip && returnFlight != null && returnFare != null) {
            cardReturnFlight.setVisibility(View.VISIBLE);

            // Gán biến Global chuyến về
            retBasePrice     = returnFare.getBasePrice();
            retTaxFee        = returnFlight.getTaxFee();

            retFareRuleId    = returnFare.getFareRuleId();
            returnFlightId   = returnFlight.getFlightId();
            retSeatMapId     = returnFlight.getSeatMapId();
            retAircraftName  = returnFlight.getAirCraftName();
            retAirlineName   = returnFlight.getAirlineName();
            retFromIata      = returnFlight.getFromIata();
            retToIata        = returnFlight.getToIata();

            // Render UI
            tvRetRoute.setText(returnFlight.getFrom() + " → " + returnFlight.getTo());
            tvRetDate.setText(returnFlight.getDepartureDate());
            tvRetDepartTime.setText(returnFlight.getDepartureTime());
            tvRetFromIata.setText(retFromIata);
            tvRetDuration.setText(returnFlight.getDuration());
            tvRetArrivalTime.setText(returnFlight.getArrivalTime());
            tvRetToIata.setText(retToIata);
            tvRetFlightNumber.setText(returnFlight.getFlightNumber());
            tvRetFareClass.setText(returnFare.getTitle());

            Glide.with(this)
                    .load(returnFlight.getAirlineLogo())
                    .placeholder(R.drawable.ic_airline)
                    .into(imgRetLogo);
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

            // Xử lý thông báo lỗi nếu có
            if (state.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(this,
                        "Lỗi tải quy định vé: " + state.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
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

        // Truyền ID lượt đi, và ID lượt về (nếu có) để ViewModel bắt đầu fetch data FareRule
        viewModel.loadFareRules(outFareRuleId, isRoundTrip ? retFareRuleId : null);

        // khởi tạo lần đầu danh sách hành khách động (trống thông tin)
        viewModel.initPassengersIfNeeded(adultCount, childCount, babyCount);

        // quan sát thông tin hành khách
        viewModel.getPassengerListLive().observe(this, list -> {
            if (list != null) {
                layoutPassengerList.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);

                for (Passenger p : list) {
                    View row = inflater.inflate(R.layout.item_passenger_input, layoutPassengerList, false);

                    // Xác định icon đại diện theo loại khách
                    ImageView imgIcon = row.findViewById(R.id.img_passenger_icon);
                    int iconRes = p.getType().equals("ADULT") ? ICON_ADULT :
                            p.getType().equals("CHILD") ? ICON_CHILD : ICON_BABY;
                    imgIcon.setImageResource(iconRes);

                    // 2. Xử lý Trạng thái Check hoàn thành nhập liệu
                    TextView tvMandatory = row.findViewById(R.id.tv_passenger_mandatory);
                    ImageView imgCheck = row.findViewById(R.id.img_passenger_check);

                    if (p.isComplete()) {
                        // Nếu đã nhập đầy đủ (Họ tên, Ngày sinh, Danh xưng)
                        imgCheck.setImageResource(ICON_CHECKED);

                        imgCheck.setColorFilter(COLOR_COMPLETE, PorterDuff.Mode.SRC_IN);

                        tvMandatory.setVisibility(View.GONE);
                        // Đảm bảo icon được hiển thị
                        imgCheck.setVisibility(View.VISIBLE);
                    }

                    // Hiển thị Nhãn và Tên
                    TextView tvLabel = row.findViewById(R.id.tv_passenger_label);
                    if (p.getFullName() != null && !p.getFullName().trim().isEmpty()) {
                        // set Tên
                        tvLabel.setText(p.getTitle() + ": " + p.getFullName().toUpperCase());

                    } else {
                        tvLabel.setText(p.getLabel());
                    }

                    // Hiển thị Ghế
                    TextView tvSeat = row.findViewById(R.id.tv_passenger_seat);
                    if (p.getSeatNumber() != null && !p.getSeatNumber().isEmpty()) {
                        tvSeat.setText(p.getSeatNumber());
                        tvSeat.setTextColor(p.getType().equals("BABY") ? Color.GRAY : Color.parseColor("#1565C0"));
                    }
                    // Gắn sự kiện click mở màn hình nhập liệu
                    row.setOnClickListener(v -> openPassengerInput(p));
                    layoutPassengerList.addView(row);
                }
            }
        });

        // quan sát ghế ngồi đã chọn
        viewModel.getDepartSeatCodesLive().observe(this,seats ->{

        });
    }

    private void openPassengerInput(Passenger passenger) {
        Intent intent = new Intent(this, PassengerInputActivity.class);
        intent.putExtra("passenger", passenger);
        passengerInputLauncher.launch(intent);
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

    private void setupClickListeners() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSelectSeat.setOnClickListener(v -> openSeatSelection());

        btnBookNow.setOnClickListener(v -> {
            ArrayList<Passenger> passengerList = viewModel.getPassengerListLive().getValue();
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


    // Seat selection

    private void openSeatSelection() {
        int seatsNeeded = adultCount + childCount;

        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_MAX_PASSENGERS, seatsNeeded);
        intent.putExtra(SeatSelectionActivity.EXTRA_IS_ROUND_TRIP,  isRoundTrip);

        // gọi ViewModel truyền dữ liệu FareRule sang SeatSelection
        if (viewModel != null) {
            viewModel.buildSeatSelectionIntent(intent, isRoundTrip);
        }

        // ── Chuyến đi ─────────────────────────────────────────────────────
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEATS_SELECTED, depSeats);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_FLIGHT_ID,    outboundFlightId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_MAP_ID,  outSeatMapId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRCRAFT_NAME,outAircraftName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRLINE_NAME, outAirlineName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_FROM_IATA, outFromIata);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_TO_IATA,   outToIata);


        // ── Chuyến về (chỉ khi khứ hồi) ──────────────────────────────────
        if (isRoundTrip) {
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEATS_SELECTED, retSeats);
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
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}