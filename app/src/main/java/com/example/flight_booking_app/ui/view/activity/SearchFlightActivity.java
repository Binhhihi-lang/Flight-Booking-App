package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.Flight;
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


    // ── Scroll listener – giữ tham chiếu để remove khi cần ──────────────
    private FlightPaginationScrollListener paginationScrollListener;

    // ── State từ Intent ───────────────────────────────────────────────────
    private String  fromCityId, toCityId;
    private String  fromCity, toCity;
    private String  departDate, returnDate;
    private int     adultCount, childCount, babyCount;
    private boolean isRoundTrip;


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

        // Danh sách chuyến bay hiển thị
        flightViewModel.getPagedFlightsLive().observe(this, flights -> {
            if (flights == null) return;

            // đổ vào Adapter để hiển thị
            adapter.submitList(new ArrayList<>(flights));

        });

        // ── Observer 2: Trạng thái load lần đầu (search mới / chuyển tab) ──
        flightViewModel.getLoadState().observe(this, state -> {
            UiState.Status status = state.getStatus();

            // Ẩn/Hiện ProgressBar
            progressBarMain.setVisibility(status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);

            switch (status) {
                case LOADING:
                    layoutEmptyState.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    // kiểm tra xem danh sách có rỗng thật hay không
                    List<Flight> currentFlights = flightViewModel.getPagedFlightsLive().getValue();
                    boolean isEmpty = (currentFlights == null || currentFlights.isEmpty());

                    layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    break;

                    // (Mất mạng, lỗi Firebase...)
                case ERROR:
                    layoutEmptyState.setVisibility(View.GONE);

                    Toast.makeText(this, state.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });


        flightViewModel.getIsOfflineLive().observe(this, isOffline -> {
            if (isOffline) {
                // Nếu không có mạng (isOffline = true)
                Snackbar snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        "Mất kết nối mạng. Đang hiển thị dữ liệu lưu tạm.",
                        Snackbar.LENGTH_LONG
                );
                snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.primary_red));
                snackbar.show();
            }
        });


        // ── Observer 3: Trạng thái load thêm trang ──
        flightViewModel.getLoadingMoreLive().observe(this, isLoadingMore -> {
            if (progressBarMain != null) {
                progressBarMore.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
            }
        });

        // quan sát trạng thái đi đến trang cuối
        flightViewModel.getIsLastPageLive().observe(this, lastPage -> {
            // Không cần làm gì thêm — scroll listener tự check isLastPage()
            List<Flight> currentFlights = flightViewModel.getPagedFlightsLive().getValue();
            boolean isEmpty = (currentFlights == null || currentFlights.isEmpty());
            if(lastPage && !isEmpty){
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
            if (flightViewModel.getFlights().isEmpty()) return;

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
        // Lưu vào trong ViewModel
        flightViewModel.setCurrentlyViewingFlight(flight);

        // Lấy thẳng FareClass được nhồi sẵn bên trong Flight ra
        FareClass fareClass = flight.getSelectedFareClass();

        flightViewModel.setCurrentlyViewingFare(fareClass);

        boolean isReturnFlight = flightViewModel.isSelectingReturn();

        FlightDetailBottomSheet sheet = FlightDetailBottomSheet.newInstance(
                flight,
                fareClass,
                isReturnFlight ? returnDate : departDate,
                adultCount, childCount, babyCount,
                !isReturnFlight, isRoundTrip
        );
        sheet.show(getSupportFragmentManager(), "flight_detail");
    }

    // call back trả về chọn lượt về

    @Override
    public void onOutboundConfirmed() {
        // Lấy Chuyến bay và Hạng vé đang xem tạm ra
        Flight currentFlight = flightViewModel.getCurrentlyViewingFlight();
        FareClass currentFare = flightViewModel.getCurrentlyViewingFare();

        // Chốt đơn Lượt đi
        flightViewModel.setSelectedOutboundFlight(currentFlight);
        flightViewModel.setSelectedOutboundFare(currentFare);

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
        FareClass currentFare = flightViewModel.getCurrentlyViewingFare();

        // Kiểm tra xem đang chốt lượt về hay 1 chiều
        if (isRoundTrip && flightViewModel.isSelectingReturn()) {
            flightViewModel.setSelectedReturnFlight(currentFlight);
            flightViewModel.setSelectedReturnFare(currentFare);
        } else {
            flightViewModel.setSelectedOutboundFlight(currentFlight);
            flightViewModel.setSelectedOutboundFare(currentFare);
        }

        // --- LẤY TẤT CẢ DỮ LIỆU ĐÃ CHỐT ĐỂ CHUYỂN MÀN HÌNH ---
        Flight outbound = flightViewModel.getSelectedOutboundFlight();
        FareClass outboundFare = flightViewModel.getSelectedOutboundFare();

        Flight returnFlight = flightViewModel.getSelectedReturnFlight();
        FareClass returnFare = flightViewModel.getSelectedReturnFare();

        Intent intent = new Intent(this, BookingInfoActivity.class);

        // Truyền Object Lượt đi
        intent.putExtra("selected_outbound_flight", outbound);
        intent.putExtra("selected_outbound_fare", outboundFare);

        intent.putExtra(BookingInfoActivity.EXTRA_IS_ROUND_TRIP, isRoundTrip);

        // Truyền Object Lượt về (nếu có)
        if (isRoundTrip && returnFlight != null) {
            intent.putExtra("selected_return_flight", returnFlight);
            intent.putExtra("selected_return_fare", returnFare);
        }

        intent.putExtra("adult_count", adultCount);
        intent.putExtra("child_count", childCount);
        intent.putExtra("baby_count",  babyCount);

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