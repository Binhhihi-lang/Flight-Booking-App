package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

/**
 * SearchFlightActivity - Tìm kiếm chuyến bay với TẤT CẢ tiêu chí.
 *
 * NHẬN từ HomeFragment:
 *   - fromCityId, toCityId (để query Firebase)
 *   - fromCity, toCity, fromIata, toIata (để hiển thị UI)
 *   - departDate, returnDate (ngày bay)
 *   - seatClass (Phổ thông/Thương gia)
 *   - adultCount, childCount, babyCount (số hành khách)
 *   - isRoundTrip (một chiều / khứ hồi)
 *
 * LỌC:
 *   Repository sẽ lọc theo:
 *   1. fromCityId + toCityId + departureDate
 *   2. seatType (map từ seatClass)
 *   3. availableSeats >= totalPassengers
 *   4. Tính displayPrice từ fareOptions phù hợp
 */
public class SearchFlightActivity extends AppCompatActivity {

    // ── Extra keys
    public static final String EXTRA_FROM_CITY_ID  = "from_city_id";
    public static final String EXTRA_TO_CITY_ID    = "to_city_id";
    public static final String EXTRA_FROM_CITY     = "from_city";
    public static final String EXTRA_TO_CITY       = "to_city";
    public static final String EXTRA_FROM_IATA     = "from_iata";
    public static final String EXTRA_TO_IATA       = "to_iata";
    public static final String EXTRA_DEPART_DATE   = "departure_date";
    public static final String EXTRA_RETURN_DATE   = "return_date";
    public static final String EXTRA_SEAT_CLASS    = "seat_class";
    public static final String EXTRA_ADULT         = "adult_count";
    public static final String EXTRA_CHILD         = "child_count";
    public static final String EXTRA_BABY          = "baby_count";
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";

    // ── Views
    private MaterialToolbar toolbar;
    private TextView tvRoute, tvInfoDate, tvInfoClass;
    private TextView tvAdultCount, tvChildCount, tvBabyCount;
    private LinearLayout layoutTripTabs;
    private TextView tabDepart, tabReturn;
    private RecyclerView rvFlights;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnSearchOtherDate;
    private ProgressBar progressBar;

    // ── ViewModel & Adapter
    private FlightViewModel flightViewModel;
    private FlightAdapter adapter;

    // ── State
    private String fromCityId, toCityId;
    private String fromCity, toCity, fromIata, toIata;
    private String departDate, returnDate, seatClass;
    private int adultCount, childCount, babyCount;
    private boolean isRoundTrip;
    private boolean isShowingDepartFlight = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_flight);

        receiveExtras();
        bindViews();
        setupToolbar();
        setupTabs();
        setupEmptyState();
        setupRecyclerView();
        setupViewModel();
    }

    // ── Nhận dữ liệu từ HomeFragment

    private void receiveExtras() {
        Intent i = getIntent();
        fromCityId  = i.getStringExtra(EXTRA_FROM_CITY_ID);
        toCityId    = i.getStringExtra(EXTRA_TO_CITY_ID);
        fromCity    = i.getStringExtra(EXTRA_FROM_CITY);
        toCity      = i.getStringExtra(EXTRA_TO_CITY);
        fromIata    = i.getStringExtra(EXTRA_FROM_IATA);
        toIata      = i.getStringExtra(EXTRA_TO_IATA);
        departDate  = i.getStringExtra(EXTRA_DEPART_DATE);
        returnDate  = i.getStringExtra(EXTRA_RETURN_DATE);
        seatClass   = i.getStringExtra(EXTRA_SEAT_CLASS);
        adultCount  = i.getIntExtra(EXTRA_ADULT, 1);
        childCount  = i.getIntExtra(EXTRA_CHILD, 0);
        babyCount   = i.getIntExtra(EXTRA_BABY, 0);
        isRoundTrip = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);
    }

    private void bindViews() {
        toolbar          = findViewById(R.id.toolbarFlights);
        tvRoute          = findViewById(R.id.tv_route);
        tvInfoDate       = findViewById(R.id.tv_info_date);
        tvInfoClass      = findViewById(R.id.tv_search_info_class);
        tvAdultCount     = findViewById(R.id.tv_search_adult_count);
        tvChildCount     = findViewById(R.id.tv_search_child_count);
        tvBabyCount      = findViewById(R.id.tv_search_baby_count);
        layoutTripTabs   = findViewById(R.id.layout_trip_tabs);
        tabDepart        = findViewById(R.id.tab_depart);
        tabReturn        = findViewById(R.id.tab_return);
        rvFlights        = findViewById(R.id.rv_flights);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        btnSearchOtherDate = findViewById(R.id.btn_search_other_date);
        progressBar      = findViewById(R.id.progress_bar);
    }

    // ── Toolbar

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        updateToolbarRoute(fromCity, toCity, departDate);

        tvInfoClass.setText(seatClass);
        tvAdultCount.setText(String.valueOf(adultCount));
        tvChildCount.setText(String.valueOf(childCount));
        tvBabyCount.setText(String.valueOf(babyCount));
    }

    private void updateToolbarRoute(String from, String to, String date) {
        if (from != null && to != null) {
            tvRoute.setText(from.toUpperCase() + " → " + to.toUpperCase());
        }
        if (tvInfoDate != null && date != null) {
            tvInfoDate.setText(date);
        }
    }

    // ── Tabs (Khứ hồi)

    private void setupTabs() {
        if (!isRoundTrip) {
            layoutTripTabs.setVisibility(View.GONE);
            return;
        }

        layoutTripTabs.setVisibility(View.VISIBLE);
        selectTab(true); // Mặc định lượt đi

        tabDepart.setOnClickListener(v -> {
            if (!isShowingDepartFlight) {
                isShowingDepartFlight = true;
                selectTab(true);
                updateToolbarRoute(fromCity, toCity, departDate);

                // Tìm lại chuyến bay lượt đi với ĐẦY ĐỦ tiêu chí
                flightViewModel.searchFlights(
                        fromCityId, toCityId, departDate,
                        seatClass, adultCount, childCount, babyCount
                );
            }
        });

        tabReturn.setOnClickListener(v -> {
            if (isShowingDepartFlight) {
                isShowingDepartFlight = false;
                selectTab(false);
                updateToolbarRoute(toCity, fromCity, returnDate);

                // Tìm chuyến bay lượt về (đảo cityId, dùng returnDate)
                flightViewModel.searchReturnFlights(
                        fromCityId, toCityId, returnDate,
                        seatClass, adultCount, childCount, babyCount
                );
            }
        });
    }

    private void selectTab(boolean isDepartTab) {
        if (isDepartTab) {
            tabDepart.setBackgroundResource(R.drawable.bg_tab_selected);
            tabDepart.setAlpha(1f);
            tabReturn.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabReturn.setAlpha(0.5f);
        } else {
            tabReturn.setBackgroundResource(R.drawable.bg_tab_selected);
            tabReturn.setAlpha(1f);
            tabDepart.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabDepart.setAlpha(0.5f);
        }
    }

    // ── Empty State

    private void setupEmptyState() {
        btnSearchOtherDate.setOnClickListener(v -> finish());
    }

    // ── RecyclerView

    private void setupRecyclerView() {
        adapter = new FlightAdapter(flight -> onFlightSelected(flight));
        rvFlights.setLayoutManager(new LinearLayoutManager(this));
        rvFlights.setAdapter(adapter);
    }

    // ── ViewModel

    private void setupViewModel() {
        flightViewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // Observe danh sách chuyến bay
        flightViewModel.getFlightList().observe(this, flights -> {
            adapter.submitList(flights);
            layoutEmptyState.setVisibility(flights.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe trạng thái load
        flightViewModel.getLoadState().observe(this, state -> {
            if (state.getStatus() == AuthResult.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
            if (state.getStatus() == AuthResult.Status.ERROR) {
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // ═══════════════════════════════════════════════════════════════════════
        // TÌM KIẾM NGAY KHI VÀO — TRUYỀN ĐẦY ĐỦ TIÊU CHÍ
        // ═══════════════════════════════════════════════════════════════════════
        flightViewModel.searchFlights(
                fromCityId, toCityId, departDate,
                seatClass, adultCount, childCount, babyCount
        );
    }

    // ── Chọn chuyến bay → FlightDetailBottomSheet

    private void onFlightSelected(Flight flight) {
        Intent intent = new Intent(this, FlightDetailBottomSheet.class);

        // Truyền toàn bộ thông tin flight
        intent.putExtra("flight_id",       flight.getFlightId());
        intent.putExtra("flight_number",   flight.getFlightNumber());
        intent.putExtra("airline_name",    flight.getAirlineName());
        intent.putExtra("airline_logo",    flight.getAirlineLogo());
        intent.putExtra("from_city",       flight.getFrom());
        intent.putExtra("to_city",         flight.getTo());
        intent.putExtra("from_iata",       flight.getFromIata());
        intent.putExtra("to_iata",         flight.getToIata());
        intent.putExtra("departure_time",  flight.getDepartureTime());
        intent.putExtra("arrival_time",    flight.getArrivalTime());
        intent.putExtra("duration",        flight.getDuration());
        intent.putExtra("departure_date",  flight.getDepartureDate());
        intent.putExtra("display_price",   flight.getDisplayPrice());
        intent.putExtra("seat_class",      flight.getSelectedSeatClass());
        intent.putExtra("adult_count",     flight.getAdultCount());
        intent.putExtra("child_count",     flight.getChildCount());
        intent.putExtra("baby_count",      flight.getBabyCount());
        intent.putExtra("is_depart",       isShowingDepartFlight);
        intent.putExtra("is_round_trip",   isRoundTrip);

        startActivity(intent);
    }
}