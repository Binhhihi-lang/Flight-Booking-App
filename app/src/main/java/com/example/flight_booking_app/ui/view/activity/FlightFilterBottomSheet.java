package com.example.flight_booking_app.ui.view.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.AirlineFilterItem;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.FlightFilterState;
import com.example.flight_booking_app.ui.view.adapter.AirlineFilterAdapter;
import com.example.flight_booking_app.ui.viewmodel.FlightFilterViewModel;
import com.example.flight_booking_app.ui.viewmodel.FlightViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightFilterBottomSheet extends BottomSheetDialogFragment {

    private FlightFilterViewModel filterViewModel;
    private FlightViewModel flightViewModel; // Dùng chung để lấy danh sách chuyến bay gốc
    private AirlineFilterAdapter airlineAdapter;

    private LinearLayout layoutPriceFull, layoutPriceNet;
    private RadioGroup rgSortBy, rgSeatClass;
    private RecyclerView rvFilterAirlines;
    private LinearLayout chipMorning, chipAfternoon, chipNight;
    private MaterialButton btnApply;
    private TextView tvBtnReset;
    private MaterialToolbar toolBarBack;

    private boolean isBindingState = false; // Cờ chặn vòng lặp vô tận khi cập nhật UI từ LiveData

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            // Lấy cái layout gốc của BottomSheet
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // ép full chiều cao
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                // Ép trạng thái mở rộng tối đa
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupViewModel();

        setupClickListeners();
    }


    private void setupViewModel() {
        filterViewModel = new ViewModelProvider(requireActivity()).get(FlightFilterViewModel.class);
        flightViewModel = new ViewModelProvider(requireActivity()).get(FlightViewModel.class);

        setupDynamicAirlines();

        // quan sát trạng thái thay đổi
        filterViewModel.getFilterState().observe(getViewLifecycleOwner(), state -> renderUIFromState(state));
    }

    private void bindViews(View v) {
        layoutPriceFull = v.findViewById(R.id.layout_price_full);
        layoutPriceNet = v.findViewById(R.id.layout_price_net);
        rgSortBy = v.findViewById(R.id.rg_sort_by);
        rvFilterAirlines = v.findViewById(R.id.rv_filter_airlines);
        chipMorning = v.findViewById(R.id.chip_time_morning);
        chipAfternoon = v.findViewById(R.id.chip_time_afternoon);
        chipNight = v.findViewById(R.id.chip_time_night);
        rgSeatClass = v.findViewById(R.id.rg_seat_class);
        btnApply = v.findViewById(R.id.btn_apply_filter);
        tvBtnReset = v.findViewById(R.id.tv_btn_reset);
        toolBarBack = v.findViewById(R.id.tool_bar_back);
    }

    private void setupDynamicAirlines() {
        List<Flight> fullFlightList = flightViewModel.getAllFlights();
        Map<String, AirlineFilterItem> airlineMap = new LinkedHashMap<>();

        // Put hãng may vào Map
        for (Flight f : fullFlightList) {
            String name = f.getAirlineName();
            if (name == null || name.isEmpty()) continue;

            if (!airlineMap.containsKey(name)) {
                airlineMap.put(name, new AirlineFilterItem(name, f.getAirlineLogo()));
            }
        }

        List<AirlineFilterItem> airlineList = new ArrayList<>(airlineMap.values());

        // trạng thái bộ lọc
        FlightFilterState currentState = filterViewModel.getFilterState().getValue();

        // Lưu tên hãng máy bay từ trạng thái
        List<String> currentSelected = new ArrayList<>(currentState.selectedAirlines);

        // đổ vào Adapter
        airlineAdapter = new AirlineFilterAdapter(airlineList, currentSelected, () -> {
            if (!isBindingState) {
                filterViewModel.setSelectedAirlines(new ArrayList<>(currentSelected));
            }
        });

        rvFilterAirlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFilterAirlines.setAdapter(airlineAdapter);
    }

    private void renderUIFromState(FlightFilterState state) {
        if (state == null) return;
        isBindingState = true; // Bật cờ bọc dữ liệu

        // Cập nhật Hiển thị giá
        updatePriceChipUI(state.showFullPrice);

        // Cập nhật Sắp xếp
        if ("PRICE_ASC".equals(state.sortMode)) rgSortBy.check(R.id.rb_sort_price_asc);
        else if ("DEPART_EARLY".equals(state.sortMode)) rgSortBy.check(R.id.rb_sort_depart_early);
        else if ("DURATION".equals(state.sortMode)) rgSortBy.check(R.id.rb_sort_duration);

        // Cập nhật Chip khung giờ
        if (state.timeSlots.size() >= 3) {
            chipMorning.setBackgroundResource(state.timeSlots.get(0) ? R.drawable.bg_filter_chip_selected : R.drawable.bg_filter_chip_unselected);
            chipAfternoon.setBackgroundResource(state.timeSlots.get(1) ? R.drawable.bg_filter_chip_selected : R.drawable.bg_filter_chip_unselected);
            chipNight.setBackgroundResource(state.timeSlots.get(2) ? R.drawable.bg_filter_chip_selected : R.drawable.bg_filter_chip_unselected);
        }

        // Cập nhật Hạng ghế
        if ("ECONOMY".equals(state.seatClass)) rgSeatClass.check(R.id.rb_class_economy);
        else if ("PREMIUM".equals(state.seatClass)) rgSeatClass.check(R.id.rb_class_premium_economy);
        else if ("BUSINESS".equals(state.seatClass)) rgSeatClass.check(R.id.rb_class_business);
        else rgSeatClass.check(R.id.rb_class_all);

        isBindingState = false; // Tắt cờ sau khi hoàn thành đổ UI
    }

    private void updatePriceChipUI(boolean isFullSelected) {
        layoutPriceFull.setBackgroundResource(isFullSelected ? R.drawable.bg_filter_chip_selected : R.drawable.bg_filter_chip_unselected);
        layoutPriceNet.setBackgroundResource(isFullSelected ? R.drawable.bg_filter_chip_unselected : R.drawable.bg_filter_chip_selected);
        setChipTextColor(layoutPriceFull, isFullSelected);
        setChipTextColor(layoutPriceNet, !isFullSelected);
    }

    private void setChipTextColor(LinearLayout chip, boolean active) {
        int colorActive = 0xFF0175F3;
        int colorInactive = 0xFF757575;
        for (int i = 0; i < chip.getChildCount(); i++) {
            View child = chip.getChildAt(i);
            if (child instanceof TextView) ((TextView) child).setTextColor(active ? colorActive : colorInactive);
        }
    }

    private void setupClickListeners() {
        layoutPriceFull.setOnClickListener(v -> filterViewModel.setShowFullPrice(true));
        layoutPriceNet.setOnClickListener(v -> filterViewModel.setShowFullPrice(false));

        rgSortBy.setOnCheckedChangeListener((group, checkedId) -> {
            if (isBindingState) return;
            if (checkedId == R.id.rb_sort_price_asc) filterViewModel.setSortMode("PRICE_ASC");
            else if (checkedId == R.id.rb_sort_depart_early) filterViewModel.setSortMode("DEPART_EARLY");
            else if (checkedId == R.id.rb_sort_duration) filterViewModel.setSortMode("DURATION");
        });

        chipMorning.setOnClickListener(v -> {
            FlightFilterState s = filterViewModel.getFilterState().getValue();
            if (s != null) filterViewModel.setTimeSlotAtIndex(0, !s.timeSlots.get(0));
        });
        chipAfternoon.setOnClickListener(v -> {
            FlightFilterState s = filterViewModel.getFilterState().getValue();
            if (s != null) filterViewModel.setTimeSlotAtIndex(1, !s.timeSlots.get(1));
        });
        chipNight.setOnClickListener(v -> {
            FlightFilterState s = filterViewModel.getFilterState().getValue();
            if (s != null) filterViewModel.setTimeSlotAtIndex(2, !s.timeSlots.get(2));
        });

        rgSeatClass.setOnCheckedChangeListener((group, checkedId) -> {
            if (isBindingState) return;
            if (checkedId == R.id.rb_class_economy) filterViewModel.setSeatClass("ECONOMY");
            else if (checkedId == R.id.rb_class_premium_economy) filterViewModel.setSeatClass("PREMIUM");
            else if (checkedId == R.id.rb_class_business) filterViewModel.setSeatClass("BUSINESS");
            else filterViewModel.setSeatClass("ALL");
        });

        tvBtnReset.setOnClickListener(v -> {
            filterViewModel.resetToDefault();
            if (airlineAdapter != null) airlineAdapter.clearSelection();
        });

        btnApply.setOnClickListener(v -> {
            filterViewModel.applyFilter(); // Gửi tín hiệu đã chốt sang Activity chủ quản
            dismiss();
        });

        toolBarBack.setNavigationOnClickListener(v -> dismiss());
    }
}