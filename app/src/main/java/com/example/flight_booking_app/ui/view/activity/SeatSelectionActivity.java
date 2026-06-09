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
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.ui.view.adapter.SeatAdapter;
import com.example.flight_booking_app.ui.viewmodel.SeatViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SeatSelectionActivity extends AppCompatActivity {

    // ─── Intent keys ──────────────────────────────────────────────────────
    public static final String EXTRA_MAX_PASSENGERS = "max_passengers";
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";

    public static final String EXTRA_OUT_CABIN_CLASS = "out_cabin_class";
    public static final String EXTRA_OUT_FREE_SEATS = "out_free_seats";

    // Chuyến đi
    public static final String EXTRA_OUT_FLIGHT_ID       = "out_flight_id";
    public static final String EXTRA_OUT_SEATS_SELECTED  = "out_seats_selected"; // FIX: was "out_flight_id"
    public static final String EXTRA_OUT_SEAT_MAP_ID     = "out_seat_map_id";
    public static final String EXTRA_OUT_AIRCRAFT_NAME   = "out_aircraft_name";
    public static final String EXTRA_OUT_AIRLINE_NAME    = "out_airline_name";
    public static final String EXTRA_OUT_SEAT_FROM_IATA  = "out_seat_from_iata";
    public static final String EXTRA_OUT_SEAT_TO_IATA    = "out_seat_to_iata";

    // Chuyến về
    public static final String EXTRA_RET_SEATS_SELECTED  = "ret_seats_selected"; // FIX: was "out_flight_id"
    public static final String EXTRA_RET_FLIGHT_ID       = "ret_flight_id";
    public static final String EXTRA_RET_SEAT_MAP_ID     = "ret_seat_map_id";

    // ĐÃ SỬA: Đổi giá trị hằng số thành KEY chuẩn
    public static final String EXTRA_RET_CABIN_CLASS = "ret_cabin_class";
    public static final String EXTRA_RET_FREE_SEATS = "ret_free_seats";

    public static final String EXTRA_RET_AIRCRAFT_NAME = "ret_aircraft_name";
    public static final String EXTRA_RET_AIRLINE_NAME = "ret_airline_name";
    public static final String EXTRA_RET_SEAT_FROM_IATA = "ret_seat_from_iata";
    public static final String EXTRA_RET_SEAT_TO_IATA = "ret_seat_to_iata";

    private static final int GRID_SPAN_COUNT = 7;

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
    private String outCabinClass, retCabinClass;
    private ArrayList<String> outFreeSeatTypes, retFreeSeatTypes;
    private String returnFlightId, retSeatMapId, retAircraftName, retAirlineName, retFromIata, retToIata;

    private ArrayList<String> preSelectedOutSeats;
    private ArrayList<String> preSelectedRetSeats;

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
        setupViewModel(); // Gọi trước setupRecyclerView để lấy trạng thái isSelectingReturn
        setupRecyclerView();
        setupToolbar();
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

        //Lấy dữ liệu hạng vé lượt đi
        outCabinClass = i.getStringExtra(EXTRA_OUT_CABIN_CLASS);
        outFreeSeatTypes = i.getStringArrayListExtra(EXTRA_OUT_FREE_SEATS);

        // lấy dữ liệu ghế đã chọn từ BookingInfo
        preSelectedOutSeats = getIntent().getStringArrayListExtra(EXTRA_OUT_SEATS_SELECTED);
        if (preSelectedOutSeats == null) {
            preSelectedOutSeats = new ArrayList<>(); // không bị NullPointerException
        }

        preSelectedRetSeats = getIntent().getStringArrayListExtra(EXTRA_RET_SEATS_SELECTED);
        if (preSelectedRetSeats == null) {
            preSelectedRetSeats = new ArrayList<>();
        }

        if (isRoundTrip) {
            returnFlightId = i.getStringExtra(EXTRA_RET_FLIGHT_ID);
            retSeatMapId = i.getStringExtra(EXTRA_RET_SEAT_MAP_ID);
            retAircraftName = i.getStringExtra(EXTRA_RET_AIRCRAFT_NAME);
            retAirlineName = i.getStringExtra(EXTRA_RET_AIRLINE_NAME);
            retFromIata = i.getStringExtra(EXTRA_RET_SEAT_FROM_IATA);
            retToIata = i.getStringExtra(EXTRA_RET_SEAT_TO_IATA);

            // Lấy dữ liệu hạng vé lượt về
            retCabinClass = i.getStringExtra(EXTRA_RET_CABIN_CLASS);
            retFreeSeatTypes = i.getStringArrayListExtra(EXTRA_RET_FREE_SEATS);
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
        toolbarSeat.setNavigationOnClickListener(v -> saveSeatsAndFinish());
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

        // Chọn đúng hạng vé (Cabin Class) tùy vào việc đang mở tab Đi hay Về
        String currentCabinClass = seatViewModel.isSelectingReturn() ? retCabinClass : outCabinClass;
        seatAdapter = new SeatAdapter(currentCabinClass, this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
    }

    private void setupViewModel() {
        seatViewModel = new ViewModelProvider(this).get(SeatViewModel.class);

        // lấy dữ liệu ghế đã chọn truyền sang và set lại trong map ghế
        seatViewModel.setPreSelectedSeats(preSelectedOutSeats, preSelectedRetSeats);

        //
        seatViewModel.getGridSpanCountLive().observe(this, spanCount -> {
            if (spanCount != null) {
                rvSeatMap.setLayoutManager(new GridLayoutManager(this, spanCount));
            }
        });

        //
        seatViewModel.getSeatMapData().observe(this, uiSeats -> {
            if (uiSeats != null) {
                seatAdapter.setSeats(uiSeats);
                // FIX: updateBottomBar() sau khi setSeats để hiển thị đúng ghế pre-selected
                updateBottomBar();
            }
        });

        seatViewModel.getIsLoading().observe(this, state -> {
            if (state == null) return;
            progressBar.setVisibility(
                    state.getStatus() == UiState.Status.LOADING ? View.VISIBLE : View.GONE
            );
            if (state.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(this, "Lỗi tải sơ đồ ghế: " + state.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        seatViewModel.getCurrentlyViewingSeatLive().observe(this, seat -> {
            if (seat == null) {
                tvSelectedSeatLabel.setText("Vui lòng chọn");
                tvSelectedSeatPrice.setText("");
                tvSeatName.setText("");
            } else {
                tvSeatName.setText(seat.getSeatNumber());
            }
        });

        if (seatViewModel.isSelectingReturn()) {
            seatViewModel.loadSeatMap(retSeatMapId, returnFlightId);
        } else {
            seatViewModel.loadSeatMap(outSeatMapId, outboundFlightId);
        }
    }

    private void setupClickListeners() {
        tabDepart.setOnClickListener(v -> {
            if (seatViewModel.isSelectingReturn()) switchToOutboundTab();
        });

        tabReturn.setOnClickListener(v -> {
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

            if (!seatViewModel.isSelectingReturn()) {
                switchToReturnTab();
            }
        });

        //  btnSeatBack khi thoát cũng lưu ghế về BookingInfo
        btnSeatBack.setOnClickListener(v -> {
            if (isRoundTrip && seatViewModel.isSelectingReturn()) {
                switchToOutboundTab();
            } else {
                saveSeatsAndFinish();
            }
        });

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
                // Chốt ghế lượt đi rồi chuyển sang tab lượt về
                // switchToReturnTab() sẽ tự gọi setSelectedOutboundSeats() + clearCurrentSelections()
                Snackbar.make(findViewById(android.R.id.content),
                        "Đã lưu ghế lượt đi. Vui lòng chọn ghế lượt về.",
                        Snackbar.LENGTH_SHORT).show();
                switchToReturnTab();
            } else {
                // Hoàn tất: trả kết quả về BookingInfo
                saveSeatsAndFinish();
            }
        });
    }

    /** Chốt ghế tab hiện tại rồi trả kết quả về BookingInfo trước khi finish. */
    private void saveSeatsAndFinish() {
        // Chốt ghế tab đang mở
        if (seatViewModel.isSelectingReturn()) {
            seatViewModel.setSelectedReturnSeats(seatViewModel.getSelectedSeats());
        } else {
            seatViewModel.setSelectedOutboundSeats(seatViewModel.getSelectedSeats());
        }

        ArrayList<String> departCodes = new ArrayList<>();
        for (Seat s : seatViewModel.getSelectedOutboundSeats()) {
            departCodes.add(s.getSeatNumber());
        }
        ArrayList<String> returnCodes = new ArrayList<>();
        for (Seat s : seatViewModel.getSelectedReturnSeats()) {
            returnCodes.add(s.getSeatNumber());
        }

        Intent result = new Intent();
        result.putStringArrayListExtra("selected_seats_depart", departCodes);
        result.putStringArrayListExtra("selected_seats_return", returnCodes);
        setResult(RESULT_OK, result);
        finish();
    }

    private void handleSeatClick(Seat seat, int position) {
        // Thông báo ViewModel ghế đang xem
        seatViewModel.setCurrentlyViewingSeat(seat, maxPassengers);

        if (seat.isSelected()) {
            // Trường hợp 1: Nếu bấm lại vào chính ghế đang chọn -> Hủy chọn nó bình thường
            seatViewModel.deselectSeat(seat);
            tvSeatName.setText("");
            seatAdapter.notifyItemChanged(position);
        } else {
            // Trường hợp 2: Bấm vào một ghế trống mới
            List<Seat> selectedSeats = seatViewModel.getSelectedSeats();

            // Kiểm tra tổng số lượng hành khách của chuyến đi
            if (maxPassengers == 1) {
                // Tự động hủy ghế cũ nếu đã có 1 ghế được chọn trước đó
                if (!selectedSeats.isEmpty()) {
                    Seat oldestSeat = selectedSeats.get(0);
                    seatViewModel.deselectSeat(oldestSeat);
                    seatAdapter.notifyDataSetChanged(); // Vẽ lại lưới để xóa màu ghế cũ
                }
            }
            else {
                if (selectedSeats.size() >= maxPassengers) {
                    Toast.makeText(this, "Bạn chỉ được chọn tối đa " + maxPassengers + " ghế!", Toast.LENGTH_SHORT).show();
                    return; // Dừng lại, không cho chọn thêm chiếc này
                }
            }

            // Tiến hành chọn chiếc ghế mới bấm (Áp dụng cho cả 2 trường hợp sau khi lọc)
            seatViewModel.selectSeat(seat);
            seatAdapter.notifyItemChanged(position);
        }

        // Cập nhật lại thanh tổng tiền và danh sách hiển thị phía dưới
        updateBottomBar();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI helpers
    // ══════════════════════════════════════════════════════════════════════

    private void updateBottomBar() {
        // FIX: Đọc đúng list theo tab đang hiển thị
        // - Tab lượt đi  → selectedSeats (đang chọn)
        // - Tab lượt về  → selectedSeats (đang chọn, đã clear ghế lượt đi khi switch tab)
        // selectedOutboundSeats chỉ dùng khi trả kết quả về BookingInfo, không dùng ở đây
        List<Seat> seats = seatViewModel.getSelectedSeats();

        if (seats.isEmpty()) {
            tvSelectedSeatLabel.setText("Vui lòng chọn");
            tvSelectedSeatPrice.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        double totalSeatSurcharge = 0;
        boolean hasSurcharge = false;

        List<String> freeSeatTypes = seatViewModel.isSelectingReturn()
                ? retFreeSeatTypes : outFreeSeatTypes;

        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            if (i > 0) sb.append(", ");
            sb.append(s.getSeatNumber());

            String seatType = s.getType() != null ? s.getType().toUpperCase() : "";
            if (freeSeatTypes == null || !freeSeatTypes.contains(seatType)) {
                totalSeatSurcharge += s.getPrice();
                hasSurcharge = true;
            }
        }

        tvSelectedSeatLabel.setText(sb.toString());

        if (!hasSurcharge) {
            tvSelectedSeatPrice.setText("Miễn phí (Bao gồm trong gói vé)");
        } else {
            tvSelectedSeatPrice.setText(totalSeatSurcharge == 0
                    ? "Đã bao gồm VAT"
                    : String.format("+%,.0fđ (Phụ thu nâng cấp ghế)", totalSeatSurcharge));
        }
    }

    private void updateInfoPanel(String aircraft, String airline, String fromIata, String toIata) {
        if (tvAirCraftName != null) tvAirCraftName.setText(aircraft != null ? aircraft : "");
        if (tvSeatAirlineName != null) tvSeatAirlineName.setText(airline != null ? airline : "");
        if (tvSeatFromIata != null) tvSeatFromIata.setText(fromIata != null ? fromIata : "");
        if (tvSeatToIata != null) tvSeatToIata.setText(toIata != null ? toIata : "");
    }

    private void switchToOutboundTab() {
        // 1. Chốt ghế lượt VỀ hiện tại trước khi rời tab
        if (!seatViewModel.getSelectedSeats().isEmpty()) {
            seatViewModel.setSelectedReturnSeats(seatViewModel.getSelectedSeats());
        }

        seatViewModel.setSelectingReturn(false);
        selectTab(false);
        updateInfoPanel(outAircraftName, outAirlineName, outFromIata, outToIata);

        // 2. Restore selectedSeats = selectedOutboundSeats để grid đánh dấu lại màu
        seatViewModel.clearCurrentSelections();
        for (Seat s : seatViewModel.getSelectedOutboundSeats()) {
            seatViewModel.selectSeat(s);
        }

        seatAdapter = new SeatAdapter(outCabinClass, this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
        seatViewModel.loadSeatMap(outSeatMapId, outboundFlightId);
    }

    private void switchToReturnTab() {
        // 1. Chốt ghế lượt ĐI hiện tại trước khi rời tab
        if (!seatViewModel.getSelectedSeats().isEmpty()) {
            seatViewModel.setSelectedOutboundSeats(seatViewModel.getSelectedSeats());
        }

        seatViewModel.setSelectingReturn(true);
        selectTab(true);
        updateInfoPanel(retAircraftName, retAirlineName, retFromIata, retToIata);

        // 2. Restore selectedSeats = selectedReturnSeats để grid đánh dấu lại màu
        seatViewModel.clearCurrentSelections();
        for (Seat s : seatViewModel.getSelectedReturnSeats()) {
            seatViewModel.selectSeat(s);
        }

        seatAdapter = new SeatAdapter(retCabinClass, this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
        seatViewModel.loadSeatMap(retSeatMapId, returnFlightId);
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