package com.example.flight_booking_app.ui.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.ui.view.adapter.SeatAdapter;
import com.example.flight_booking_app.ui.viewmodel.SeatViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * SeatSelectionActivity – Màn hình chọn ghế ngồi.
 * <p>
 * NHẬN từ BookingInfoActivity (qua Intent):
 * - EXTRA_MAX_PASSENGERS    (int)    : số ghế tối đa = adultCount + childCount
 * - EXTRA_IS_ROUND_TRIP     (bool)   : hiển thị tab Đi/Về hay không
 * - EXTRA_OUT_SEAT_MAP_ID   (String) : seatMapId chuyến đi
 * - EXTRA_OUT_AIRCRAFT_NAME (String) : tên máy bay chuyến đi
 * - EXTRA_OUT_AIRLINE_NAME  (String) : tên hãng chuyến đi
 * - EXTRA_OUT_SEAT_FROM_IATA(String) : IATA điểm đi
 * - EXTRA_OUT_SEAT_TO_IATA  (String) : IATA điểm đến
 * (Tương tự EXTRA_RET_* cho chuyến về)
 * <p>
 * TRẢ VỀ BookingInfoActivity qua "selected_seats_depart" và "selected_seats_return".
 */
public class SeatSelectionActivity extends AppCompatActivity {

    // ─── Intent keys ──────────────────────────────────────────────────────
    public static final String EXTRA_MAX_PASSENGERS = "max_passengers";
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";
    public static final String RESULT_SELECTED_SEATS = "selected_seats"; // legacy, không dùng nữa

    // Chuyến đi
    public static final String EXTRA_OUT_FLIGHT_ID = "out_flight_id";
    public static final String EXTRA_OUT_SEAT_MAP_ID = "out_seat_map_id";
    public static final String EXTRA_OUT_AIRCRAFT_NAME = "out_aircraft_name";
    public static final String EXTRA_OUT_AIRLINE_NAME = "out_airline_name";
    public static final String EXTRA_OUT_SEAT_FROM_IATA = "out_seat_from_iata";
    public static final String EXTRA_OUT_SEAT_TO_IATA = "out_seat_to_iata";

    // Chuyến về
    public static final String EXTRA_RET_FLIGHT_ID = "ret_flight_id";
    public static final String EXTRA_RET_SEAT_MAP_ID = "ret_seat_map_id";
    public static final String EXTRA_RET_AIRCRAFT_NAME = "ret_aircraft_name";
    public static final String EXTRA_RET_AIRLINE_NAME = "ret_airline_name";
    public static final String EXTRA_RET_SEAT_FROM_IATA = "ret_seat_from_iata";
    public static final String EXTRA_RET_SEAT_TO_IATA = "ret_seat_to_iata";

    private static final int GRID_SPAN_COUNT = 7; // 3 ghế | lối đi | 3 ghế

    // ─── Views ────────────────────────────────────────────────────────────
    private RecyclerView rvSeatMap;
    private MaterialToolbar toolbarSeat;
    private LinearLayout layoutTripTabs;
    private SeatAdapter seatAdapter;
    private TextView tvSelectedSeatLabel, tvSelectedSeatPrice;
    private Button btnSeatBack, btnSeatContinue;
    private ProgressBar progressBar;
    private TextView tabDepart, tabReturn;
    private TextView tvAirCraftName, tvSeatAirlineName;
    private TextView tvSeatFromIata, tvSeatToIata, tvSeatName;

    // ─── Data từ Intent ───────────────────────────────────────────────────
    private int maxPassengers = 1;
    private boolean isRoundTrip;

    private String outboundFlightId, outSeatMapId, outAircraftName, outAirlineName, outFromIata, outToIata;
    private String returnFlightId, retSeatMapId, retAircraftName, retAirlineName, retFromIata, retToIata;

    private SeatViewModel seatViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        receiveIntentData();
        bindViews();
        // ViewModel phải khởi tạo trước setupToolbar() vì setupToolbar dùng seatViewModel
        seatViewModel = new ViewModelProvider(this).get(SeatViewModel.class);
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
    }

    private void receiveIntentData() {
        Intent i = getIntent();
        maxPassengers = i.getIntExtra(EXTRA_MAX_PASSENGERS, 1);
        isRoundTrip = i.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false);

        outboundFlightId = i.getStringExtra(EXTRA_OUT_FLIGHT_ID);
        outSeatMapId = i.getStringExtra(EXTRA_OUT_SEAT_MAP_ID);
        outAircraftName = i.getStringExtra(EXTRA_OUT_AIRCRAFT_NAME);
        outAirlineName = i.getStringExtra(EXTRA_OUT_AIRLINE_NAME);
        outFromIata = i.getStringExtra(EXTRA_OUT_SEAT_FROM_IATA);
        outToIata = i.getStringExtra(EXTRA_OUT_SEAT_TO_IATA);

        if (isRoundTrip) {
            returnFlightId = i.getStringExtra(EXTRA_RET_FLIGHT_ID);
            retSeatMapId = i.getStringExtra(EXTRA_RET_SEAT_MAP_ID);
            retAircraftName = i.getStringExtra(EXTRA_RET_AIRCRAFT_NAME);
            retAirlineName = i.getStringExtra(EXTRA_RET_AIRLINE_NAME);
            retFromIata = i.getStringExtra(EXTRA_RET_SEAT_FROM_IATA);
            retToIata = i.getStringExtra(EXTRA_RET_SEAT_TO_IATA);
        }
    }

    private void bindViews() {
        toolbarSeat = findViewById(R.id.toolbar_seat);
        rvSeatMap = findViewById(R.id.rv_seat_map);
        tvSelectedSeatLabel = findViewById(R.id.tv_selected_seat_label);
        tvSelectedSeatPrice = findViewById(R.id.tv_selected_seat_price);
        btnSeatBack = findViewById(R.id.btn_seat_back);
        btnSeatContinue = findViewById(R.id.btn_seat_continue);
        progressBar = findViewById(R.id.progress_bar);
        tabDepart = findViewById(R.id.tab_seat_depart);
        tabReturn = findViewById(R.id.tab_seat_return);
        tvAirCraftName = findViewById(R.id.tv_aircraft_model);
        tvSeatAirlineName = findViewById(R.id.tv_seat_airline_name);
        tvSeatName = findViewById(R.id.tv_seat_name);
        tvSeatFromIata = findViewById(R.id.tv_seat_from_iata);
        tvSeatToIata = findViewById(R.id.tv_seat_to_iata);
        layoutTripTabs = findViewById(R.id.layout_seat_tab);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarSeat);
        toolbarSeat.setNavigationOnClickListener(v -> finish());
        updateInfoPanel(outAircraftName, outAirlineName, outFromIata, outToIata);

        if (!isRoundTrip) {
            layoutTripTabs.setVisibility(View.GONE);
        } else {
            layoutTripTabs.setVisibility(View.VISIBLE);
            selectTab(seatViewModel.isSelectingReturn());
        }
    }

    private void setupRecyclerView() {
        rvSeatMap.setLayoutManager(new GridLayoutManager(this, GRID_SPAN_COUNT));
        seatAdapter = new SeatAdapter(this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
    }

    private void setupViewModel() {
        // Quan sát lưới ghế

        seatViewModel.getSeatMapData().observe(this, uiSeats -> {
            if (uiSeats != null) {
                seatAdapter.setSeats(uiSeats);
                updateBottomBar();
            }
        });

        // Quan sát trạng thái loading / error
        seatViewModel.getIsLoading().observe(this, state -> {
            if (state == null) return;
            progressBar.setVisibility(
                    state.getStatus() == AuthResult.Status.LOADING ? View.VISIBLE : View.GONE
            );
            if (state.getStatus() == AuthResult.Status.ERROR) {
                Toast.makeText(this, "Lỗi tải sơ đồ ghế: " + state.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát ghế đang xem (click) → chỉ phát khi chưa đủ ghế
        // Khi null (vừa clearCurrentSelections) → reset về trạng thái ban đầu
        seatViewModel.getCurrentlyViewingSeatLive().observe(this, seat -> {
            if (seat == null) {
                // Tab vừa được reset (chuyển từ lượt đi sang lượt về)
                tvSelectedSeatLabel.setText("Vui lòng chọn");
                tvSelectedSeatPrice.setText("");
                tvSeatName.setText("");
            } else {
                // Còn chỗ chọn hiển thị ghế đang xem
                tvSeatName.setText(seat.getSeatNumber());

            }
        });

        // Load sơ đồ ghế đúng theo tab hiện tại
        if (seatViewModel.isSelectingReturn()) {
            seatViewModel.loadSeatMap(retSeatMapId,returnFlightId);
        } else {
            seatViewModel.loadSeatMap(outSeatMapId,outboundFlightId);
        }
    }

    private void setupClickListeners() {
        // Tab LƯỢT ĐI
        tabDepart.setOnClickListener(v -> {
            if (seatViewModel.isSelectingReturn()) switchToOutboundTab();
        });

        // Tab LƯỢT VỀ — chỉ cho phép nếu đã chốt ghế lượt đi
        tabReturn.setOnClickListener(v -> {
            if (!seatViewModel.isSelectingReturn()) {
                if (seatViewModel.getSelectedOutboundSeats().isEmpty()) {
                    Snackbar.make(rvSeatMap, "Vui lòng hoàn thành chọn ghế lượt đi trước",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    switchToReturnTab();
                }
            }
        });

        // TRỞ VỀ: nếu đang ở tab lượt về → quay về tab lượt đi, ngược lại → đóng màn hình
        btnSeatBack.setOnClickListener(v -> {
            if (isRoundTrip && seatViewModel.isSelectingReturn()) {
                switchToOutboundTab();
            } else {
                finish();
            }
        });

        // TIẾP TỤC
        btnSeatContinue.setOnClickListener(v -> {
            int selectedCount = seatViewModel.getSelectedSeats().size();

            if (selectedCount == 0) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCount < maxPassengers) {
                Toast.makeText(this,
                        "Vui lòng chọn đủ " + maxPassengers + " ghế cho hành khách!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRoundTrip && !seatViewModel.isSelectingReturn()) {
                // ── Khứ hồi, vừa xong lượt đi: snapshot → chuyển tab lượt về ──
                seatViewModel.setSelectedOutboundSeats(seatViewModel.getSelectedSeats());
                seatViewModel.clearCurrentSelections();
                switchToReturnTab();
                Snackbar.make(rvSeatMap,
                        "Đã lưu ghế lượt đi. Vui lòng chọn ghế lượt về.",
                        Snackbar.LENGTH_SHORT).show();

            } else {
                // ── 1 chiều hoặc đã xong lượt về: trả kết quả ──
                ArrayList<String> departCodes = new ArrayList<>();
                ArrayList<String> returnCodes = new ArrayList<>();

                if (isRoundTrip) {
                    for (Seat s : seatViewModel.getSelectedOutboundSeats()) {
                        departCodes.add(s.getSeatNumber());
                    }
                    for (Seat s : seatViewModel.getSelectedSeats()) {
                        returnCodes.add(s.getSeatNumber());
                    }
                } else {
                    for (Seat s : seatViewModel.getSelectedSeats()) {
                        departCodes.add(s.getSeatNumber());
                    }
                }

                Intent result = new Intent();
                result.putStringArrayListExtra("selected_seats_depart", departCodes);
                result.putStringArrayListExtra("selected_seats_return", returnCodes);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Seat click
    // ══════════════════════════════════════════════════════════════════════

    private void handleSeatClick(Seat seat, int position) {
        // Thông báo ViewModel ghế đang xem — ViewModel tự quyết định có phát LiveData không
        seatViewModel.setCurrentlyViewingSeat(seat, maxPassengers);

        if (seat.isSelected()) {
            // Bỏ chọn ghế đang chọn
            seatViewModel.deselectSeat(seat);
            tvSeatName.setText("");
        } else {
            // Kiểm tra đã đủ ghế chưa
            if (seatViewModel.getSelectedSeats().size() >= maxPassengers) {
                Toast.makeText(this,
                        "Bạn chỉ được chọn tối đa " + maxPassengers + " ghế!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            seatViewModel.selectSeat(seat);
        }

        seatAdapter.notifyItemChanged(position);
        updateBottomBar();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật bottom bar theo danh sách ghế đang chọn.
     */
    private void updateBottomBar() {
        List<Seat> seats = seatViewModel.getSelectedSeats();
        if (seats.isEmpty()) {
            tvSelectedSeatLabel.setText("Vui lòng chọn");
            tvSelectedSeatPrice.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            if (i > 0) sb.append(", ");
            sb.append(s.getSeatNumber());
            total += s.getPrice();
        }
        tvSelectedSeatLabel.setText(sb.toString());
        // Chỉ hiển thị giá nếu có phụ thu (> 0)
        tvSelectedSeatPrice.setText(total > 0 ? String.format("%,.0fđ", total) : "Đã bao gồm VAT");
    }

    private void updateInfoPanel(String aircraft, String airline, String fromIata, String toIata) {
        if (tvAirCraftName != null) tvAirCraftName.setText(aircraft != null ? aircraft : "");
        if (tvSeatAirlineName != null) tvSeatAirlineName.setText(airline != null ? airline : "");
        if (tvSeatFromIata != null) tvSeatFromIata.setText(fromIata != null ? fromIata : "");
        if (tvSeatToIata != null) tvSeatToIata.setText(toIata != null ? toIata : "");
    }

    private void switchToOutboundTab() {
        seatViewModel.setSelectingReturn(false);
        selectTab(false);
        updateInfoPanel(outAircraftName, outAirlineName, outFromIata, outToIata);

        //  Mã Sơ đồ mẫu + Mã Chuyến bay Lượt Đi
        seatViewModel.loadSeatMap(outSeatMapId, outboundFlightId);
    }

    private void switchToReturnTab() {
        seatViewModel.setSelectingReturn(true);
        selectTab(true);
        updateInfoPanel(retAircraftName, retAirlineName, retFromIata, retToIata);

        // Mã Sơ đồ mẫu + Mã Chuyến bay Lượt Về
        seatViewModel.loadSeatMap(retSeatMapId, returnFlightId);
    }
    private void selectTab(boolean isReturnTab) {
        // LƯỢT ĐI
        tabDepart.setBackgroundResource(isReturnTab
                ? R.drawable.bg_seat_tab_unselected
                : R.drawable.bg_seat_tab_selected);
        tabDepart.setTextColor(isReturnTab
                ? getColor(R.color.text_sub_grey)
                : getColor(R.color.primary_blue));

        // LƯỢT VỀ
        tabReturn.setBackgroundResource(isReturnTab
                ? R.drawable.bg_seat_tab_selected
                : R.drawable.bg_seat_tab_unselected);
        tabReturn.setTextColor(isReturnTab
                ? getColor(R.color.primary_blue)
                : getColor(R.color.text_sub_grey));
    }
}