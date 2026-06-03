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
import com.example.flight_booking_app.data.model.FlightFilterState;
import com.example.flight_booking_app.ui.view.adapter.FlightAdapter;
import com.example.flight_booking_app.ui.viewmodel.FlightFilterViewModel;
import com.example.flight_booking_app.ui.viewmodel.FlightViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchFlightActivity – Màn hình kết quả tìm kiếm chuyến bay.
 *
 * ── Phân trang ─────────────────────────────────────────────────────────────
 * RecyclerView + FlightPaginationScrollListener + FlightViewModel.
 *
 * - progressBarMain : hiển thị khi search mới / chuyển tab (load lần đầu)
 * - progressBarMore : footer ProgressBar, hiển thị khi load thêm trang
 *
 * Toàn bộ trạng thái phân trang nằm trong ViewModel
 * → xoay màn hình KHÔNG reset về trang 1, KHÔNG gọi lại Firebase.
 *
 * ── Filter ─────────────────────────────────────────────────────────────────
 * FlightFilterBottomSheet gọi viewModel.applyFilter() khi người dùng OK.
 * allFlights (danh sách gốc) vẫn được giữ trong ViewModel để reset filter.
 */
public class SearchFlightActivity extends AppCompatActivity
        implements FlightDetailBottomSheet.OnFlightActionListener {

    // ── Extra keys ────────────────────────────────────────────────────────
    public static final String EXTRA_FROM_CITY_ID  = "from_city_id";
    public static final String EXTRA_TO_CITY_ID    = "to_city_id";
    public static final String EXTRA_FROM_CITY     = "from_city";
    public static final String EXTRA_TO_CITY       = "to_city";
    public static final String EXTRA_FROM_IATA     = "from_iata";
    public static final String EXTRA_TO_IATA       = "to_iata";
    public static final String EXTRA_DEPART_DATE   = "departure_date";
    public static final String EXTRA_RETURN_DATE   = "return_date";
    public static final String EXTRA_ADULT         = "adult_count";
    public static final String EXTRA_CHILD         = "child_count";
    public static final String EXTRA_BABY          = "baby_count";
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";

    // ── Views ─────────────────────────────────────────────────────────────
    private MaterialToolbar      toolbar;
    private TextView             tvRoute, tvInfoDate;
    private TextView             tvAdultCount, tvChildCount, tvBabyCount;
    private LinearLayout         layoutTripTabs;
    private TextView             tabDepart, tabReturn;
    private RecyclerView         rvFlights;
    private LinearLayout         layoutEmptyState;
    private MaterialButton       btnSearchOtherDate;
    private ProgressBar          progressBarMain; // toàn màn hình – search mới / chuyển tab
    private ProgressBar          progressBarMore; // footer – load thêm trang
    private FloatingActionButton fabFilter;

    // ── ViewModel & Adapter ───────────────────────────────────────────────
    private FlightViewModel flightViewModel;
    private FlightFilterViewModel filterViewModel;
    private FlightAdapter   adapter;

    /**
     * Danh sách gốc đầy đủ (allFlights từ ViewModel) để FlightFilterBottomSheet
     * luôn lọc từ đầu thay vì chỉ lọc trang hiện tại.
     */
    private List<Flight> fullFlightList = new ArrayList<>();

    // ── Scroll listener – giữ tham chiếu để remove khi cần ──────────────
    private FlightPaginationScrollListener paginationScrollListener;

    // ── State từ Intent ───────────────────────────────────────────────────
    private String  fromCityId, toCityId;
    private String  fromCity, toCity;
    private String  departDate, returnDate;
    private int     adultCount, childCount, babyCount;
    private boolean isRoundTrip;

    // ══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════

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
        setupRecyclerView();       // Phải trước setupViewModel (adapter cần sẵn)
        setupViewModel();
        setupToolbar();
        setupClickListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Setup
    // ══════════════════════════════════════════════════════════════════════

    private void bindViews() {
        toolbar            = findViewById(R.id.toolbarFlights);
        tvRoute            = findViewById(R.id.tv_route);
        tvInfoDate         = findViewById(R.id.tv_info_date);
        tvAdultCount       = findViewById(R.id.tv_search_adult_count);
        tvChildCount       = findViewById(R.id.tv_search_child_count);
        tvBabyCount        = findViewById(R.id.tv_search_baby_count);
        layoutTripTabs     = findViewById(R.id.layout_trip_tabs);
        tabDepart          = findViewById(R.id.tab_depart);
        tabReturn          = findViewById(R.id.tab_return);
        rvFlights          = findViewById(R.id.rv_flights);
        layoutEmptyState   = findViewById(R.id.layout_empty_state);
        btnSearchOtherDate = findViewById(R.id.btn_search_other_date);
        progressBarMain    = findViewById(R.id.progress_bar);       // ProgressBar toàn màn hình
        progressBarMore    = findViewById(R.id.progress_bar_more);  // Footer ProgressBar (thêm vào layout)
        fabFilter          = findViewById(R.id.fab_filter);
    }

    private void receiveExtras() {
        Intent i   = getIntent();
        fromCityId = i.getStringExtra(EXTRA_FROM_CITY_ID);
        toCityId   = i.getStringExtra(EXTRA_TO_CITY_ID);
        fromCity   = i.getStringExtra(EXTRA_FROM_CITY);
        toCity     = i.getStringExtra(EXTRA_TO_CITY);
        departDate = i.getStringExtra(EXTRA_DEPART_DATE);
        returnDate = i.getStringExtra(EXTRA_RETURN_DATE);
        adultCount = i.getIntExtra(EXTRA_ADULT, 1);
        childCount = i.getIntExtra(EXTRA_CHILD, 0);
        babyCount  = i.getIntExtra(EXTRA_BABY, 0);
        isRoundTrip = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        updateToolbarRoute(fromCity, toCity, departDate);
        tvAdultCount.setText(String.valueOf(adultCount));
        tvChildCount.setText(String.valueOf(childCount));
        tvBabyCount.setText(String.valueOf(babyCount));

        if (!isRoundTrip) {
            layoutTripTabs.setVisibility(View.GONE);
            return;
        }
        layoutTripTabs.setVisibility(View.VISIBLE);
        // Khôi phục trạng thái tab sau xoay màn hình
        selectTab(flightViewModel.isSelectingReturn());
    }

    private void setupRecyclerView() {
        adapter = new FlightAdapter(this::onFlightSelected);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFlights.setLayoutManager(layoutManager);
        rvFlights.setAdapter(adapter);

        // ── Gắn Pagination Scroll Listener vào rv
        paginationScrollListener = new FlightPaginationScrollListener(layoutManager) {
            @Override
            public void loadMoreItems() {
                // ViewModel tự set isLoading = true ngay đầu loadNextPage()
                flightViewModel.loadNextPage();
            }

            @Override
            public boolean isLoading() {
                return flightViewModel.isLoading();
            }

            @Override
            public boolean isLastPage() {
                return flightViewModel.isLastPage();
            }
        };
        rvFlights.addOnScrollListener(paginationScrollListener);
    }

    private void setupViewModel() {
        flightViewModel = new ViewModelProvider(this).get(FlightViewModel.class);
        filterViewModel = new ViewModelProvider(this).get(FlightFilterViewModel.class);

        // ── Observer 1: Danh sách chuyến bay hiển thị (tích lũy theo trang) ──
        flightViewModel.getPagedFlightsLive().observe(this, flights -> {
            if (flights == null) return;

            // Cập nhật fullFlightList từ allFlights của ViewModel để filter đúng
            // (allFlights không phơi ra ngoài trực tiếp vì là internal state)
            // Tạm thời dùng flights cho filter — sẽ chuẩn hơn nếu expose getAllFlights()
            adapter.submitList(new ArrayList<>(flights));
            layoutEmptyState.setVisibility(flights.isEmpty() ? View.VISIBLE : View.GONE);
            // Lưu để FlightFilterBottomSheet dùng (dùng list đầy đủ nhất hiện có)
            fullFlightList = new ArrayList<>(flights);
        });

        // ── Observer 2: Trạng thái load lần đầu (search mới / chuyển tab) ──
        flightViewModel.getLoadState().observe(this, state -> {
            progressBarMain.setVisibility(
                    state.getStatus() == AuthResult.Status.LOADING ? View.VISIBLE : View.GONE);
            if (state.getStatus() == AuthResult.Status.ERROR) {
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // ── Observer 3: Trạng thái load thêm trang ──
        flightViewModel.getLoadingMoreLive().observe(this, isLoadingMore -> {
            if (progressBarMain != null) {
                progressBarMore.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
            }
        });

        // ── Observer 4: Đã đến trang cuối ──
        flightViewModel.getIsLastPageLive().observe(this, lastPage -> {
            // Không cần làm gì thêm — scroll listener tự check isLastPage()
            if(lastPage){
                Toast.makeText(this, "Đã đến trang cuối !!",Toast.LENGTH_SHORT).show();
            }
        });

        flightViewModel.getFilterStateLive().observe(this, state -> {
            if (state != null && adapter != null) {
                // Bạn cần truyền biến showFullPrice vào adapter của bạn
                adapter.setShowFullPrice(state.showFullPrice);
            }
        });

        //lắng nghe sự kiện từ bộ lọc gửi sang
        filterViewModel.getApplyFilterEvent().observe(this, finalState -> {
            if (finalState != null) {
                flightViewModel.applyFilterState(finalState);

            }
        });

        // ── Chỉ gọi khi ds chuyến bay nạp lên lần đầu (currentPage == 0)
        if (flightViewModel.isFirstLoad()) {
            triggerSearch();
        }
    }

    private void setupClickListeners() {
        tabDepart.setOnClickListener(v -> {
            if (flightViewModel.isSelectingReturn()) switchToOutboundTab();
        });

        tabReturn.setOnClickListener(v -> {
            if (!flightViewModel.isSelectingReturn()) {
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

        fabFilter.setOnClickListener(v -> {
            // nếu ds tìm kiếm chuyến bay rỗng thì không hiển thị
            if (flightViewModel.getAllFlights().isEmpty()) return;

            FlightFilterBottomSheet sheet = new FlightFilterBottomSheet();
            sheet.show(getSupportFragmentManager(), "filter");
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tab switching
    // ══════════════════════════════════════════════════════════════════════

    private void switchToOutboundTab() {
        flightViewModel.setSelectingReturn(false);
        selectTab(false);
        updateToolbarRoute(fromCity, toCity, departDate);
        flightViewModel.searchFlights(fromCityId, toCityId, departDate,
                adultCount, childCount, babyCount);
    }

    private void switchToReturnTab() {
        flightViewModel.setSelectingReturn(true);
        selectTab(true);
        updateToolbarRoute(toCity, fromCity, returnDate);
        flightViewModel.searchReturnFlights(fromCityId, toCityId, returnDate,
                adultCount, childCount, babyCount);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Flight selection → BottomSheet
    // ══════════════════════════════════════════════════════════════════════

    private void onFlightSelected(Flight flight) {
        flightViewModel.setCurrentlyViewingFlight(flight);
        boolean isReturnFlight = flightViewModel.isSelectingReturn();

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
                flight.getFareClassName(),
                flight.getCheckedBaggage(),
                adultCount, childCount, babyCount,
                !isReturnFlight,
                isRoundTrip
        );
            sheet.show(getSupportFragmentManager(), "flight_detail");
    }

    // ══════════════════════════════════════════════════════════════════════
    // OnFlightActionListener callbacks
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void onOutboundConfirmed() {
        Flight currentFlight = flightViewModel.getCurrentlyViewingFlight();
        flightViewModel.setSelectedOutboundFlight(currentFlight);
        flightViewModel.setSelectingReturn(true);
        switchToReturnTab();
        Snackbar.make(rvFlights,
                        "Chuyến đi đã chọn! Vui lòng chọn chuyến bay lượt về",
                        Snackbar.LENGTH_LONG)
                .setAction("OK", v -> {})
                .show();
    }

    @Override
    public void onBookingConfirmed() {
        Flight currentFlight = flightViewModel.getCurrentlyViewingFlight();
        if (isRoundTrip) {
            flightViewModel.setSelectedReturnFlight(currentFlight);
        } else {
            flightViewModel.setSelectedOutboundFlight(currentFlight);
        }

        Flight outbound      = flightViewModel.getSelectedOutboundFlight();
        Flight returnFlight  = flightViewModel.getSelectedReturnFlight();

        Intent intent = new Intent(this, BookingInfoActivity.class);

        intent.putExtra(BookingInfoActivity.EXTRA_OUT_FLIGHT_ID,      outbound.getFlightId());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_FLIGHT_NUMBER,   outbound.getFlightNumber());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_AIRLINE_LOGO,    outbound.getAirlineLogo());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_FROM_CITY,       outbound.getFrom());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_FROM_IATA,       outbound.getFromIata());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_TO_CITY,         outbound.getTo());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_TO_IATA,         outbound.getToIata());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_DEPART_TIME,     outbound.getDepartureTime());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_ARRIVAL_TIME,    outbound.getArrivalTime());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_DURATION,        outbound.getDuration());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_DATE,            departDate);
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_BASE_PRICE,      outbound.getDisplayPrice());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_FARE_CLASS,      outbound.getFareClassName());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_SEAT_MAP_ID,     outbound.getSeatMapId());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_AIRCRAFT_NAME,   outbound.getAirCraftName());
        intent.putExtra(BookingInfoActivity.EXTRA_OUT_AIRLINE_NAME,    outbound.getAirlineName());

        intent.putExtra(BookingInfoActivity.EXTRA_IS_ROUND_TRIP, isRoundTrip);
        if (isRoundTrip && returnFlight != null) {
            intent.putExtra(BookingInfoActivity.EXTRA_RET_FLIGHT_ID,     returnFlight.getFlightId());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_FLIGHT_NUMBER, returnFlight.getFlightNumber());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_AIRLINE_LOGO,  returnFlight.getAirlineLogo());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_FROM_CITY,     returnFlight.getFrom());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_FROM_IATA,     returnFlight.getFromIata());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_TO_CITY,       returnFlight.getTo());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_TO_IATA,       returnFlight.getToIata());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_DEPART_TIME,   returnFlight.getDepartureTime());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_ARRIVAL_TIME,  returnFlight.getArrivalTime());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_DURATION,      returnFlight.getDuration());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_DATE,          returnDate);
            intent.putExtra(BookingInfoActivity.EXTRA_RET_BASE_PRICE,    returnFlight.getDisplayPrice());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_FARE_CLASS,    returnFlight.getFareClassName());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_SEAT_MAP_ID,   returnFlight.getSeatMapId());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_AIRCRAFT_NAME, returnFlight.getAirCraftName());
            intent.putExtra(BookingInfoActivity.EXTRA_RET_AIRLINE_NAME,  returnFlight.getAirlineName());
        }

        intent.putExtra(BookingInfoActivity.EXTRA_ADULT_COUNT, adultCount);
        intent.putExtra(BookingInfoActivity.EXTRA_CHILD_COUNT, childCount);
        intent.putExtra(BookingInfoActivity.EXTRA_BABY_COUNT,  babyCount);

        startActivity(intent);
    }


    /** Kích hoạt search đúng tab (lần đầu vào màn hình). */
    private void triggerSearch() {
        if (flightViewModel.isSelectingReturn()) {
            flightViewModel.searchReturnFlights(fromCityId, toCityId, returnDate,
                    adultCount, childCount, babyCount);
        } else {
            flightViewModel.searchFlights(fromCityId, toCityId, departDate,
                    adultCount, childCount, babyCount);
        }
    }

    private void updateToolbarRoute(String from, String to, String date) {
        if (from != null && to != null) {
            tvRoute.setText(from.toUpperCase() + " -> " + to.toUpperCase());
        }
        if (tvInfoDate != null && date != null) {
            tvInfoDate.setText(date);
        }
    }

    private void selectTab(boolean isReturnTab) {
        tabDepart.setBackgroundResource(isReturnTab
                ? R.drawable.bg_seat_tab_unselected
                : R.drawable.bg_seat_tab_selected);
        tabDepart.setTextColor(isReturnTab
                ? getColor(R.color.text_sub_grey)
                : getColor(R.color.primary_blue));
        tabReturn.setBackgroundResource(isReturnTab
                ? R.drawable.bg_seat_tab_selected
                : R.drawable.bg_seat_tab_unselected);
        tabReturn.setTextColor(isReturnTab
                ? getColor(R.color.primary_blue)
                : getColor(R.color.text_sub_grey));
    }
}