package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.ui.view.adapter.FlightAdapter;
import com.example.flight_booking_app.ui.viewmodel.FlightViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Implement OnFlightActionListener để nhận callback từ FlightDetailBottomSheet.
 * <p>
 * onOutboundConfirmed() → lưu chuyến đi, chuyển sang tab LƯỢT VỀ,
 * hiện Snackbar nhắc chọn chuyến về.
 * onBookingConfirmed()  → mở BookingActivity.
 */
public class SearchFlightActivity extends AppCompatActivity
        implements FlightDetailBottomSheet.OnFlightActionListener {

    // ── Extra keys ────────────────────────────────────────────────────────────
    public static final String EXTRA_FROM_CITY_ID = "from_city_id";
    public static final String EXTRA_TO_CITY_ID = "to_city_id";
    public static final String EXTRA_FROM_CITY = "from_city";
    public static final String EXTRA_TO_CITY = "to_city";
    public static final String EXTRA_FROM_IATA = "from_iata";
    public static final String EXTRA_TO_IATA = "to_iata";
    public static final String EXTRA_DEPART_DATE = "departure_date";
    public static final String EXTRA_RETURN_DATE = "return_date";
    public static final String EXTRA_SEAT_CLASS = "seat_class";
    public static final String EXTRA_ADULT = "adult_count";
    public static final String EXTRA_CHILD = "child_count";
    public static final String EXTRA_BABY = "baby_count";
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";

    private MaterialToolbar toolbar;
    private TextView tvRoute, tvInfoDate, tvInfoClass;
    private TextView tvAdultCount, tvChildCount, tvBabyCount;
    private LinearLayout layoutTripTabs;
    private TextView tabDepart, tabReturn;
    private RecyclerView rvFlights;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnSearchOtherDate;
    private ProgressBar progressBar;


    private FlightViewModel flightViewModel;
    private FlightAdapter adapter;

    // ── State
    private String fromCityId, toCityId;
    private String fromCity, toCity;
    private String departDate, returnDate, seatClass;
    private int adultCount, childCount, babyCount;
    private boolean isRoundTrip; // cờ tab hiển thị lượt đi , lượt về

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_flight);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        receiveExtras();
        setupRecyclerView();
        setupViewModel();
        setupToolbar();
        setupClickListeners();
    }

    private void setupClickListeners() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tabDepart.setOnClickListener(v -> {
            // Nếu ĐANG ở bên tab Về (isSelectingReturn == true) thì mới cho phép bấm quay lại tab Đi
            if (flightViewModel.isSelectingReturn()) {
                switchToOutboundTab();
            }
        });

        tabReturn.setOnClickListener(v -> {
            // Nếu ĐANG ở tab Đi (isSelectingReturn == false)
            if (!flightViewModel.isSelectingReturn()) {
                // Kiểm tra xem đã chọn chuyến đi chưa
                if (flightViewModel.getSelectedOutboundFlight() == null) {
                    Snackbar.make(rvFlights,
                            "Vui lòng chọn chuyến bay lượt đi trước",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    switchToReturnTab();
                }
            }
        });

        btnSearchOtherDate.setOnClickListener(v -> finish());
    }


    private void receiveExtras() {
        Intent i = getIntent();
        fromCityId = i.getStringExtra(EXTRA_FROM_CITY_ID);
        toCityId = i.getStringExtra(EXTRA_TO_CITY_ID);
        fromCity = i.getStringExtra(EXTRA_FROM_CITY);
        toCity = i.getStringExtra(EXTRA_TO_CITY);
        departDate = i.getStringExtra(EXTRA_DEPART_DATE);
        returnDate = i.getStringExtra(EXTRA_RETURN_DATE);
        seatClass = i.getStringExtra(EXTRA_SEAT_CLASS);
        adultCount = i.getIntExtra(EXTRA_ADULT, 1);
        childCount = i.getIntExtra(EXTRA_CHILD, 0);
        babyCount = i.getIntExtra(EXTRA_BABY, 0);
        isRoundTrip = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbarFlights);
        tvRoute = findViewById(R.id.tv_route);
        tvInfoDate = findViewById(R.id.tv_info_date);
        tvInfoClass = findViewById(R.id.tv_search_info_class);
        tvAdultCount = findViewById(R.id.tv_search_adult_count);
        tvChildCount = findViewById(R.id.tv_search_child_count);
        tvBabyCount = findViewById(R.id.tv_search_baby_count);
        layoutTripTabs = findViewById(R.id.layout_trip_tabs);
        tabDepart = findViewById(R.id.tab_depart);
        tabReturn = findViewById(R.id.tab_return);
        rvFlights = findViewById(R.id.rv_flights);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        btnSearchOtherDate = findViewById(R.id.btn_search_other_date);
        progressBar = findViewById(R.id.progress_bar);
    }


    private void setupToolbar() {
        updateToolbarRoute(fromCity, toCity, departDate);

        tvInfoClass.setText(seatClass);
        tvAdultCount.setText(String.valueOf(adultCount));
        tvChildCount.setText(String.valueOf(childCount));
        tvBabyCount.setText(String.valueOf(babyCount));

        if (!isRoundTrip) {
            layoutTripTabs.setVisibility(View.GONE);
            return;
        }

        layoutTripTabs.setVisibility(View.VISIBLE);
        selectTab(flightViewModel.isSelectingReturn());
    }

    private void updateToolbarRoute(String from, String to, String date) {
        if (from != null && to != null) {
            tvRoute.setText(from.toUpperCase() + " -> " + to.toUpperCase());

        }
        if (tvInfoDate != null && date != null) {
            tvInfoDate.setText(date);
        }
    }


    /**
     * Chuyển sang tab LƯỢT ĐI và tải lại danh sách.
     */
    private void switchToOutboundTab() {
        flightViewModel.setSelectingReturn(false);
        selectTab(false);
        updateToolbarRoute(fromCity, toCity, departDate);
        flightViewModel.searchFlights(
                fromCityId, toCityId, departDate,
                seatClass, adultCount, childCount, babyCount
        );
    }

    /**
     * Chuyển sang tab LƯỢT VỀ và tải lại danh sách.
     * Chỉ được gọi sau khi đã chọn chuyến đi.
     */
    private void switchToReturnTab() {
        flightViewModel.setSelectingReturn(true);
        selectTab(true);
        updateToolbarRoute(toCity, fromCity, returnDate);
        flightViewModel.searchReturnFlights(
                fromCityId, toCityId, returnDate,
                seatClass, adultCount, childCount, babyCount
        );
    }

    // decor
    private void selectTab(boolean isReturnTab) {
        if (isReturnTab) {
            tabReturn.setBackgroundResource(R.drawable.bg_tab_selected);
            tabReturn.setAlpha(1f);
            tabDepart.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabDepart.setAlpha(0.5f);
        } else {
            tabDepart.setBackgroundResource(R.drawable.bg_tab_selected);
            tabDepart.setAlpha(1f);
            tabReturn.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabReturn.setAlpha(0.5f);
        }
    }

    private void setupRecyclerView() {
        adapter = new FlightAdapter(this::onFlightSelected);
        rvFlights.setLayoutManager(new LinearLayoutManager(this));
        rvFlights.setAdapter(adapter);
    }

    private void setupViewModel() {
        flightViewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        flightViewModel.getFlightList().observe(this, flights -> {
            // đổ dữ liệu lên apdater
            adapter.submitList(flights);
            layoutEmptyState.setVisibility(flights.isEmpty() ? View.VISIBLE : View.GONE);
        });

        flightViewModel.getLoadState().observe(this, state -> {
            progressBar.setVisibility(
                    state.getStatus() == AuthResult.Status.LOADING ? View.VISIBLE : View.GONE);
            if (state.getStatus() == AuthResult.Status.ERROR) {
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (flightViewModel.isSelectingReturn()) {
            flightViewModel.searchReturnFlights(fromCityId, toCityId, returnDate,
                    seatClass, adultCount, childCount, babyCount);
        } else {
            // Nếu đang ở lượt đi bình thường (hoặc lần đầu vào màn hình)
            flightViewModel.searchFlights(
                    fromCityId, toCityId, departDate,
                    seatClass, adultCount, childCount, babyCount
            );
        }
    }

    // ── Chọn chuyến bay → mở BottomSheet

    private void onFlightSelected(Flight flight) {

        // Cất chuyến bay đang click vào ViewModel
        flightViewModel.setCurrentlyViewingFlight(flight);
        boolean isReturnFlight = flightViewModel.isSelectingReturn();

        // Tạo BottomSheet với đầy đủ dữ liệu qua Bundle để không bị mất khi
        FlightDetailBottomSheet sheet = FlightDetailBottomSheet.newInstance(
                flight.getFlightNumber(),
                flight.getAirlineName(),
                flight.getAirlineLogo(),
                flight.getFrom(),
                flight.getFromIata(),
                flight.getTo(),
                flight.getToIata(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getDuration(),
                isReturnFlight ? returnDate : departDate,
                flight.getDisplayPrice(),
                flight.getTaxFee(),
                seatClass,
                flight.getFareClassName(),
                flight.getCheckedBaggage(),
                adultCount, childCount, babyCount,
                !isReturnFlight,
                isRoundTrip
        );

        // Mở BottomSheet — Activity tự implement listener nên truyền vào trực tiếp
        sheet.show(getSupportFragmentManager(), "flight_detail");
    }

    // ── OnFlightActionListener callbacks

    /**
     * Callback khi người dùng xác nhận chuyến ĐI (khứ hồi).
     * → Tự động chuyển sang tab LƯỢT VỀ + hiện Snackbar nhắc nhở.
     */
    @Override
    public void onOutboundConfirmed() {
        // Lấy chuyến bay đang xem từ ViewModel ra và lưu nó vào ô "Chuyến bay đi đã chọn"
        Flight currentFlight = flightViewModel.getCurrentlyViewingFlight();
        flightViewModel.setSelectedOutboundFlight(currentFlight);

        // Đánh dấu trạng thái: Đã chuyển sang chọn chuyến về
        flightViewModel.setSelectingReturn(true);

        // Chuyển sang tab lượt về
        switchToReturnTab();

        // Snackbar nhắc chọn chuyến về — hiện ở bottom của màn hình
        Snackbar.make(
                rvFlights,
                "Chuyến đi đã chọn! Vui lòng chọn chuyến bay lượt về",
                Snackbar.LENGTH_LONG
        ).setAction("OK", v -> { /* đóng snackbar */ }).show();
    }

    @Override
    public void onBookingConfirmed() {
        Flight currentFlight = flightViewModel.getCurrentlyViewingFlight();

        if (isRoundTrip) {
            flightViewModel.setSelectedReturnFlight(currentFlight);
        } else {
            flightViewModel.setSelectedOutboundFlight(currentFlight);
        }

        Flight outbound = flightViewModel.getSelectedOutboundFlight();
        Flight returnFlight = flightViewModel.getSelectedReturnFlight();

        Intent intent = new Intent(this, BookingInfoActivity.class);

        // Thông tin lượt đi
        intent.putExtra("outbound_flight_number", outbound.getFlightNumber());
        intent.putExtra("outbound_airline_name", outbound.getAirlineName());
        intent.putExtra("outbound_airline_logo", outbound.getAirlineLogo());
        intent.putExtra("outbound_from_city", outbound.getFrom());
        intent.putExtra("outbound_from_iata", outbound.getFromIata());
        intent.putExtra("outbound_to_city", outbound.getTo());
        intent.putExtra("outbound_to_iata", outbound.getToIata());
        intent.putExtra("outbound_depart_time", outbound.getDepartureTime());
        intent.putExtra("outbound_arrival_time", outbound.getArrivalTime());
        intent.putExtra("outbound_duration", outbound.getDuration());
        intent.putExtra("outbound_date", departDate);
        intent.putExtra("outbound_price", outbound.getDisplayPrice());
        intent.putExtra("outbound_fare_class", outbound.getFareClassName());
        intent.putExtra("outbound_baggage", outbound.getCheckedBaggage());

        // Thông tin lượt về (nếu có)
        intent.putExtra("is_round_trip", isRoundTrip);
        if (isRoundTrip && returnFlight != null) {
            intent.putExtra("return_flight_number", returnFlight.getFlightNumber());
            intent.putExtra("return_from_iata", returnFlight.getFromIata());
            intent.putExtra("return_to_iata", returnFlight.getToIata());
            intent.putExtra("return_depart_time", returnFlight.getDepartureTime());
            intent.putExtra("return_arrival_time", returnFlight.getArrivalTime());
            intent.putExtra("return_date", returnDate);
            intent.putExtra("return_price", returnFlight.getDisplayPrice());
        }

        // Thông tin hành khách & hạng ghế
        intent.putExtra("seat_class", seatClass);
        intent.putExtra("adult_count", adultCount);
        intent.putExtra("child_count", childCount);
        intent.putExtra("baby_count", babyCount);

        startActivity(intent);
    }
}