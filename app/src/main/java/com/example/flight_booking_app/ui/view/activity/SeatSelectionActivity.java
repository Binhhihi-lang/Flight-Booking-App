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
import com.example.flight_booking_app.data.model.BookingSessionManager;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.ui.view.adapter.SeatAdapter;
import com.example.flight_booking_app.ui.viewmodel.SeatViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SeatSelectionActivity extends AppCompatActivity {

    // ─── Intent keys (CHỈ GIỮ LẠI GHẾ ĐÃ CHỌN ĐỂ TÔ MÀU) ─────────────────
    public static final String EXTRA_OUT_SEATS_SELECTED  = "out_seats_selected";
    public static final String EXTRA_RET_SEATS_SELECTED  = "ret_seats_selected";

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

    // ─── Data từ BookingSessionManager ────────────────────────────────────
    private int maxPassengers = 1;
    private boolean isRoundTrip;

    private Flight outboundFlight;
    private FareClass outboundFare;
    private Flight returnFlight;
    private FareClass returnFare;

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
        setupViewModel();
        setupRecyclerView();
        setupToolbar();
        setupClickListeners();
    }

    private void receiveIntentData() {
        // Tích hợp BookingSessionManager để lấy dữ liệu thay vì dùng Intent lắt nhắt
        BookingSessionManager session = BookingSessionManager.getInstance();

        isRoundTrip = session.isRoundTrip();
        // Số lượng ghế cần chọn = Người lớn + Trẻ em (Bỏ qua em bé)
        maxPassengers = session.getAdultCount() + session.getChildCount();

        outboundFlight = session.getSelectedOutboundFlight();
        outboundFare = session.getSelectedOutboundFare();

        if (isRoundTrip) {
            returnFlight = session.getSelectedReturnFlight();
            returnFare = session.getSelectedReturnFare();
        }

        // Vẫn giữ lại Intent để lấy danh sách ghế đã chọn trước đó (nếu quay lại từ màn hình sau)
        preSelectedOutSeats = getIntent().getStringArrayListExtra(EXTRA_OUT_SEATS_SELECTED);
        if (preSelectedOutSeats == null) preSelectedOutSeats = new ArrayList<>();

        preSelectedRetSeats = getIntent().getStringArrayListExtra(EXTRA_RET_SEATS_SELECTED);
        if (preSelectedRetSeats == null) preSelectedRetSeats = new ArrayList<>();
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

        // Cập nhật giao diện từ Object outboundFlight
        updateInfoPanel(outboundFlight.getAirCraftName(),
                outboundFlight.getAirlineName(),
                outboundFlight.getFromIata(),
                outboundFlight.getToIata());

        if (!isRoundTrip) {
            layoutTripTabs.setVisibility(View.GONE);
        } else {
            layoutTripTabs.setVisibility(View.VISIBLE);
            selectTab(seatViewModel.isSelectingReturn());
        }
    }

    private void setupRecyclerView() {
        rvSeatMap.setLayoutManager(new GridLayoutManager(this, GRID_SPAN_COUNT));
        String currentFareClassId = seatViewModel.isSelectingReturn() ? returnFare.getFareClassId() : outboundFare.getFareClassId();
        seatAdapter = new SeatAdapter(currentFareClassId, this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
    }

    private void setupViewModel() {
        seatViewModel = new ViewModelProvider(this).get(SeatViewModel.class);
        seatViewModel.setPreSelectedSeats(preSelectedOutSeats, preSelectedRetSeats);

        seatViewModel.getGridSpanCountLive().observe(this, spanCount -> {
            if (spanCount != null) {
                rvSeatMap.setLayoutManager(new GridLayoutManager(this, spanCount));
            }
        });

        seatViewModel.getSeatMapData().observe(this, uiSeats -> {
            if (uiSeats != null) {
                seatAdapter.setSeats(uiSeats);
                updateBottomBar();
            }
        });

        seatViewModel.getIsLoading().observe(this, state -> {
            if (state == null) return;
            progressBar.setVisibility(state.getStatus() == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
            if (state.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(this, getString(R.string.error_load_seatmap_format, state.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        seatViewModel.getCurrentlyViewingSeatLive().observe(this, seat -> {
            if (seat == null) {
                tvSelectedSeatLabel.setText(getString(R.string.label_please_select));
                tvSelectedSeatPrice.setText("");
                tvSeatName.setText("");
            } else {
                tvSeatName.setText(seat.getSeatNumber());
            }
        });

        // Load dữ liệu sơ đồ ghế ban đầu
        if (seatViewModel.isSelectingReturn()) {
            seatViewModel.loadSeatMap(returnFlight.getSeatMapId(), returnFlight.getFlightId());
        } else {
            seatViewModel.loadSeatMap(outboundFlight.getSeatMapId(), outboundFlight.getFlightId());
        }
    }

    private void setupClickListeners() {
        tabDepart.setOnClickListener(v -> {
            if (seatViewModel.isSelectingReturn()) switchToOutboundTab();
        });

        tabReturn.setOnClickListener(v -> {
            int selectedCount = seatViewModel.getSelectedSeats().size();
            if (selectedCount < maxPassengers) {
                Toast.makeText(this, getString(R.string.error_select_enough_seats_format, maxPassengers), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!seatViewModel.isSelectingReturn()) switchToReturnTab();
        });

        btnSeatBack.setOnClickListener(v -> {
            if (isRoundTrip && seatViewModel.isSelectingReturn()) {
                switchToOutboundTab();
            } else {
                saveSeatsAndFinish();
            }
        });

        btnSeatContinue.setOnClickListener(v -> {
            int selectedCount = seatViewModel.getSelectedSeats().size();
            if (selectedCount < maxPassengers) {
                Toast.makeText(this, getString(R.string.error_select_enough_seats_format, maxPassengers), Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRoundTrip && !seatViewModel.isSelectingReturn()) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_outbound_seat_saved), Snackbar.LENGTH_SHORT).show();
                switchToReturnTab();
            } else {
                saveSeatsAndFinish();
            }
        });
    }

    private void saveSeatsAndFinish() {
        if (seatViewModel.isSelectingReturn()) {
            seatViewModel.setSelectedReturnSeats(seatViewModel.getSelectedSeats());
        } else {
            seatViewModel.setSelectedOutboundSeats(seatViewModel.getSelectedSeats());
        }

        ArrayList<String> departCodes = new ArrayList<>();
        ArrayList<Double> depPrices = new ArrayList<>();
        ArrayList<String> returnCodes = new ArrayList<>();
        ArrayList<Double> retPrices = new ArrayList<>();

        // (Lưu ý: Bạn hãy chắc chắn trong class FareClass đã có hàm getFreeSeatTypes() nhé)
        List<String> outFreeSeatTypes = outboundFare.getFareRule().getFreeIncludedSeatTypes();
        List<String> retFreeSeatTypes = isRoundTrip ? returnFare.getFareRule().getFreeIncludedSeatTypes() : new ArrayList<>();

        for (Seat s : seatViewModel.getSelectedOutboundSeats()) {
            departCodes.add(s.getSeatNumber());
            String seatType = s.getType() != null ? s.getType().toUpperCase() : "";
            if (outFreeSeatTypes != null && outFreeSeatTypes.contains(seatType)) {
                depPrices.add(0.0);
            } else {
                depPrices.add(s.getPrice());
            }
        }

        for (Seat s : seatViewModel.getSelectedReturnSeats()) {
            returnCodes.add(s.getSeatNumber());
            String seatType = s.getType() != null ? s.getType().toUpperCase() : "";
            if (retFreeSeatTypes != null && retFreeSeatTypes.contains(seatType)) {
                retPrices.add(0.0);
            } else {
                retPrices.add(s.getPrice());
            }
        }

        Intent result = new Intent();
        result.putStringArrayListExtra("selected_seats_depart", departCodes);
        result.putStringArrayListExtra("selected_seats_return", returnCodes);
        result.putExtra("selected_prices_depart", depPrices);
        result.putExtra("selected_prices_return", retPrices);

        setResult(RESULT_OK, result);
        finish();
    }

    private void handleSeatClick(Seat seat, int position) {
        seatViewModel.setCurrentlyViewingSeat(seat, maxPassengers);
        if (seat.isSelected()) {
            seatViewModel.deselectSeat(seat);
            tvSeatName.setText("");
            seatAdapter.notifyItemChanged(position);
        } else {
            List<Seat> selectedSeats = seatViewModel.getSelectedSeats();
            if (maxPassengers == 1) {
                if (!selectedSeats.isEmpty()) {
                    Seat oldestSeat = selectedSeats.get(0);
                    seatViewModel.deselectSeat(oldestSeat);
                    seatAdapter.notifyDataSetChanged();
                }
            } else {
                if (selectedSeats.size() >= maxPassengers) {
                    Toast.makeText(this, getString(R.string.error_max_seats_format, maxPassengers), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            seatViewModel.selectSeat(seat);
            seatAdapter.notifyItemChanged(position);
        }
        updateBottomBar();
    }

    private void updateBottomBar() {
        List<Seat> seats = seatViewModel.getSelectedSeats();
        if (seats.isEmpty()) {
            tvSelectedSeatLabel.setText(getString(R.string.label_please_select));
            tvSelectedSeatPrice.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        double totalSeatSurcharge = 0;
        boolean hasSurcharge = false;

        List<String> freeSeatTypes = seatViewModel.isSelectingReturn() ? returnFare.getFareRule().getFreeIncludedSeatTypes() : outboundFare.getFareRule().getFreeIncludedSeatTypes();

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
            tvSelectedSeatPrice.setText(getString(R.string.label_seat_free_included));
        } else {
            tvSelectedSeatPrice.setText(totalSeatSurcharge == 0
                    ? getString(R.string.label_vat_included)
                    : getString(R.string.label_seat_surcharge_format, totalSeatSurcharge));
        }
    }

    private void updateInfoPanel(String aircraft, String airline, String fromIata, String toIata) {
        if (tvAirCraftName != null) tvAirCraftName.setText(aircraft != null ? aircraft : "");
        if (tvSeatAirlineName != null) tvSeatAirlineName.setText(airline != null ? airline : "");
        if (tvSeatFromIata != null) tvSeatFromIata.setText(fromIata != null ? fromIata : "");
        if (tvSeatToIata != null) tvSeatToIata.setText(toIata != null ? toIata : "");
    }

    private void switchToOutboundTab() {
        if (!seatViewModel.getSelectedSeats().isEmpty()) seatViewModel.setSelectedReturnSeats(seatViewModel.getSelectedSeats());
        seatViewModel.setSelectingReturn(false);
        selectTab(false);
        updateInfoPanel(outboundFlight.getAirCraftName(), outboundFlight.getAirlineName(), outboundFlight.getFromIata(), outboundFlight.getToIata());

        seatViewModel.clearCurrentSelections();
        for (Seat s : seatViewModel.getSelectedOutboundSeats()) seatViewModel.selectSeat(s);

        seatAdapter = new SeatAdapter(outboundFare.getFareClassId(), this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
        seatViewModel.loadSeatMap(outboundFlight.getSeatMapId(), outboundFlight.getFlightId());
    }

    private void switchToReturnTab() {
        if (!seatViewModel.getSelectedSeats().isEmpty()) seatViewModel.setSelectedOutboundSeats(seatViewModel.getSelectedSeats());
        seatViewModel.setSelectingReturn(true);
        selectTab(true);
        updateInfoPanel(returnFlight.getAirCraftName(), returnFlight.getAirlineName(), returnFlight.getFromIata(), returnFlight.getToIata());

        seatViewModel.clearCurrentSelections();
        for (Seat s : seatViewModel.getSelectedReturnSeats()) seatViewModel.selectSeat(s);

        seatAdapter = new SeatAdapter(returnFare.getFareClassId(), this::handleSeatClick);
        rvSeatMap.setAdapter(seatAdapter);
        seatViewModel.loadSeatMap(returnFlight.getSeatMapId(), returnFlight.getFlightId());
    }

    private void selectTab(boolean isReturnTab) {
        tabDepart.setBackgroundResource(isReturnTab ? R.drawable.bg_seat_tab_unselected : R.drawable.bg_seat_tab_selected);
        tabDepart.setTextColor(isReturnTab ? getColor(R.color.text_sub_grey) : getColor(R.color.primary_blue));
        tabReturn.setBackgroundResource(isReturnTab ? R.drawable.bg_seat_tab_selected : R.drawable.bg_seat_tab_unselected);
        tabReturn.setTextColor(isReturnTab ? getColor(R.color.primary_blue) : getColor(R.color.text_sub_grey));
    }
}