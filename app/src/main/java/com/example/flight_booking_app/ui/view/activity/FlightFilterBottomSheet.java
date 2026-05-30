package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Flight;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * FlightFilterBottomSheet
 * ══════════════════════════════════════════════════════════════════
 * BottomSheet lọc + sắp xếp danh sách chuyến bay.
 *
 * NHẬN: danh sách Flight gốc (fullList) từ SearchFlightActivity.
 * TRẢ VỀ: danh sách đã lọc qua interface OnFilterApplied.
 *
 * LOGIC:
 *  1. Hiển thị giá   : Đầy đủ (có taxFee) / NET (chưa có taxFee)
 *  2. Sắp xếp        : Giá thấp/cao, Giờ đi sớm/muộn, Bay ngắn nhất
 *  3. Khoảng giá     : RangeSlider min–max tính từ danh sách thực tế
 *  4. Hãng bay       : Tự động detect từ danh sách, CheckBox mỗi hãng
 *                       kèm giá rẻ nhất của hãng đó
 *  5. Thời gian đi   : Đêm(00-06) / Sáng(06-12) / Chiều(12-18) / Tối(18-24)
 *  6. Hạng ghế       : Tất cả / Phổ thông / Phổ thông đặc biệt / Thương gia
 *
 * CÁCH DÙNG trong SearchFlightActivity:
 *   FlightFilterBottomSheet sheet = FlightFilterBottomSheet.newInstance(fullList);
 *   sheet.setOnFilterApplied(filteredList -> adapter.submitList(filteredList));
 *   sheet.show(getSupportFragmentManager(), "filter");
 * ══════════════════════════════════════════════════════════════════
 */
public class FlightFilterBottomSheet extends BottomSheetDialogFragment {

    // ── Hằng số khoảng thời gian ──────────────────────────────────────────
    private static final int TIME_MORNING_START   = 0;
    private static final int TIME_MORNING_END     = 6;
    private static final int TIME_NOON_START      = 6;
    private static final int TIME_NOON_END        = 12;
    private static final int TIME_AFTERNOON_START = 12;
    private static final int TIME_AFTERNOON_END   = 18;
    private static final int TIME_NIGHT_START     = 18;
    private static final int TIME_NIGHT_END       = 24;

    // ── Callback về Activity ──────────────────────────────────────────────
    public interface OnFilterApplied {
        void onApplied(List<Flight> filteredList);
    }

    private OnFilterApplied filterCallback;

    public void setOnFilterApplied(OnFilterApplied cb) {
        this.filterCallback = cb;
    }

    // ── Dữ liệu gốc ──────────────────────────────────────────────────────
    /** Danh sách chuyến bay gốc — không bao giờ thay đổi */
    private List<Flight> fullFlightList = new ArrayList<>();

    // ── Trạng thái filter hiện tại ────────────────────────────────────────
    private boolean showFullPrice  = true;       // true=đầy đủ, false=NET
    private String  sortMode       = "PRICE_ASC";
    /** Set tên hãng đang được chọn (null = tất cả) */
    private final List<String> selectedAirlines  = new ArrayList<>();
    private final List<Boolean> timeSlotSelected = new ArrayList<>(
            java.util.Arrays.asList(false, false, false, false)); // morning/noon/afternoon/night
    private String  seatClassFilter = "ALL";

    // ── Adapter hãng bay ──────────────────────────────────────────────────
    private AirlineFilterAdapter airlineAdapter;

    // ── Views ─────────────────────────────────────────────────────────────
    private LinearLayout  layoutPriceFull, layoutPriceNet;
    private RadioGroup    rgSortBy, rgSeatClass;
    private RecyclerView  rvFilterAirlines;
    private LinearLayout  chipMorning, chipAfternoon, chipNight;
    private MaterialButton btnApply;
    private TextView      tvBtnReset;
    private MaterialToolbar toolBarBack;

    // ══════════════════════════════════════════════════════════════════════
    // Factory
    // ══════════════════════════════════════════════════════════════════════

    public static FlightFilterBottomSheet newInstance(List<Flight> flights) {
        FlightFilterBottomSheet sheet = new FlightFilterBottomSheet();
        sheet.fullFlightList = new ArrayList<>(flights);
        return sheet;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mở rộng BottomSheet lên full height ngay khi mở
//        View bottomSheet = view.getRootView().findViewById();
//        if (bottomSheet != null) {
//            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            behavior.setSkipCollapsed(true);
//        }

        bindViews(view);
        setupPriceDisplayToggle();
        setupSortBy();
        setupAirlineRecyclerView();
        setupTimeChips();
        setupSeatClass();
        setupApplyButton();
        setupResetButton();
        setupClickListeners();
    }

    private void setupClickListeners() {
        toolBarBack.setNavigationOnClickListener(v -> dismiss());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Bind views
    // ══════════════════════════════════════════════════════════════════════

    private void bindViews(View v) {
        layoutPriceFull        = v.findViewById(R.id.layout_price_full);
        layoutPriceNet         = v.findViewById(R.id.layout_price_net);
        rgSortBy               = v.findViewById(R.id.rg_sort_by);
        rvFilterAirlines       = v.findViewById(R.id.rv_filter_airlines);
        chipMorning            = v.findViewById(R.id.chip_time_morning);
        chipAfternoon          = v.findViewById(R.id.chip_time_afternoon);
        chipNight              = v.findViewById(R.id.chip_time_night);
        rgSeatClass            = v.findViewById(R.id.rg_seat_class);
        btnApply               = v.findViewById(R.id.btn_apply_filter);
        tvBtnReset             = v.findViewById(R.id.tv_btn_reset);
        toolBarBack = v.findViewById(R.id.tool_bar_back);

    }


    // ══════════════════════════════════════════════════════════════════════
    // 1. Hiển thị giá
    // ══════════════════════════════════════════════════════════════════════

    private void setupPriceDisplayToggle() {
        updatePriceChipUI(true); // mặc định Giá đầy đủ

        layoutPriceFull.setOnClickListener(v -> {
            showFullPrice = true;
            updatePriceChipUI(true);
        });
        layoutPriceNet.setOnClickListener(v -> {
            showFullPrice = false;
            updatePriceChipUI(false);
        });
    }

    private void updatePriceChipUI(boolean isFullSelected) {
        layoutPriceFull.setBackgroundResource(isFullSelected
                ? R.drawable.bg_filter_chip_selected : R.drawable.bg_filter_chip_unselected);
        layoutPriceNet.setBackgroundResource(isFullSelected
                ? R.drawable.bg_filter_chip_unselected : R.drawable.bg_filter_chip_selected);

        // Cập nhật màu text
        TextView tvFullTitle = layoutPriceFull.findViewWithTag(null);
        // Đơn giản hơn: dùng getChildAt
        setChipTextColor(layoutPriceFull, isFullSelected);
        setChipTextColor(layoutPriceNet, !isFullSelected);
    }

    /** Đổi màu text các TextView con trong chip theo trạng thái active/inactive. */
    private void setChipTextColor(LinearLayout chip, boolean active) {
        int colorActive   = 0xFF0175F3;
        int colorInactive = 0xFF757575;
        for (int i = 0; i < chip.getChildCount(); i++) {
            View child = chip.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                // Chỉ đổi màu cho title (textStyle bold), không đổi subtitle
                tv.setTextColor(active ? colorActive : colorInactive);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 2. Sắp xếp
    // ══════════════════════════════════════════════════════════════════════

    private void setupSortBy() {
        rgSortBy.setOnCheckedChangeListener((group, checkedId) -> {
            if      (checkedId == R.id.rb_sort_price_asc)   sortMode = "PRICE_ASC";
            else if (checkedId == R.id.rb_sort_depart_early) sortMode = "DEPART_EARLY";
            else if (checkedId == R.id.rb_sort_duration)     sortMode = "DURATION";
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // 4. Hãng bay — detect động từ danh sách + giá rẻ nhất mỗi hãng
    // ══════════════════════════════════════════════════════════════════════

    private void setupAirlineRecyclerView() {
        // Gom nhóm: airlineName → {logo, minPrice}
        // Dùng LinkedHashMap để giữ thứ tự xuất hiện
        Map<String, AirlineFilterItem> airlineMap = new LinkedHashMap<>();

        for (Flight f : fullFlightList) {
            String name = f.getAirlineName();
            if (name == null || name.isEmpty()) continue;

            if (!airlineMap.containsKey(name)) {
                airlineMap.put(name, new AirlineFilterItem(
                        name, f.getAirlineLogo(), f.getDisplayPrice()));
            } else {
                AirlineFilterItem existing = airlineMap.get(name);
                if (f.getDisplayPrice() < existing.minPrice) {
                    existing.minPrice = f.getDisplayPrice();
                }
            }
        }

        List<AirlineFilterItem> airlineList = new ArrayList<>(airlineMap.values());

        // Mặc định tất cả được chọn
        selectedAirlines.clear();
        for (AirlineFilterItem item : airlineList) {
            selectedAirlines.add(item.name);
        }


        rvFilterAirlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFilterAirlines.setAdapter(airlineAdapter);
    }

    // ══════════════════════════════════════════════════════════════════════
    // 5. Thời gian khởi hành
    // ══════════════════════════════════════════════════════════════════════

    private void setupTimeChips() {
        setupSingleTimeChip(chipMorning,   0);
        setupSingleTimeChip(chipAfternoon, 1);
        setupSingleTimeChip(chipNight,     2);
    }

    private void setupSingleTimeChip(LinearLayout chip, int index) {
        chip.setOnClickListener(v -> {
            boolean nowSelected = !timeSlotSelected.get(index);
            timeSlotSelected.set(index, nowSelected);
            chip.setBackgroundResource(nowSelected
                    ? R.drawable.bg_filter_chip_selected
                    : R.drawable.bg_filter_chip_unselected);
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // 6. Hạng ghế
    // ══════════════════════════════════════════════════════════════════════

    private void setupSeatClass() {
        rgSeatClass.setOnCheckedChangeListener((group, checkedId) -> {
            if      (checkedId == R.id.rb_class_economy)         seatClassFilter = "ECONOMY";
            else if (checkedId == R.id.rb_class_premium_economy) seatClassFilter = "PREMIUM_ECONOMY";
            else if (checkedId == R.id.rb_class_business)        seatClassFilter = "BUSINESS";
            else                                                  seatClassFilter = "ALL";
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Áp dụng filter
    // ══════════════════════════════════════════════════════════════════════

    private void setupApplyButton() {
        btnApply.setOnClickListener(v -> {
            List<Flight> filtered = applyAllFilters(fullFlightList);
            if (filterCallback != null) {
                filterCallback.onApplied(filtered);
            }
            dismiss();
        });
    }

    /**
     * Áp dụng toàn bộ tiêu chí lọc theo thứ tự:
     *   1. Khoảng giá
     *   2. Hãng bay
     *   3. Thời gian khởi hành
     *   4. Hạng ghế
     * Sau đó sắp xếp theo sortMode.
     */
    private List<Flight> applyAllFilters(List<Flight> source) {
        List<Flight> result = new ArrayList<>();

        for (Flight f : source) {

            // ── Lọc hãng bay ──
            if (!selectedAirlines.isEmpty()
                    && !selectedAirlines.contains(f.getAirlineName())) continue;

            // ── Lọc thời gian khởi hành ──
            boolean anyTimeSelected = false;
            for (boolean b : timeSlotSelected) { if (b) { anyTimeSelected = true; break; } }

            if (anyTimeSelected) {
                int hour = parseDepartureHour(f.getDepartureTime());
                boolean matchesTime = false;
                if (timeSlotSelected.get(0) && hour >= TIME_MORNING_START   && hour < TIME_MORNING_END)   matchesTime = true;
                if (timeSlotSelected.get(1) && hour >= TIME_AFTERNOON_START && hour < TIME_AFTERNOON_END) matchesTime = true;
                if (timeSlotSelected.get(2) && hour >= TIME_NIGHT_START     && hour < TIME_NIGHT_END)     matchesTime = true;
                if (!matchesTime) continue;
            }

            // ── Lọc hạng ghế ──
            if (!"ALL".equals(seatClassFilter)) {
                String fareClass = f.getFareClassName();
                if (fareClass == null) continue;
                boolean matches = false;
                switch (seatClassFilter) {
                    case "ECONOMY":
                        matches = fareClass.toLowerCase().contains("phổ thông")
                                || fareClass.toLowerCase().contains("eco");
                        break;
                    case "PREMIUM_ECONOMY":
                        matches = fareClass.toLowerCase().contains("đặc biệt")
                                || fareClass.toLowerCase().contains("premium");
                        break;
                    case "BUSINESS":
                        matches = fareClass.toLowerCase().contains("thương gia")
                                || fareClass.toLowerCase().contains("business");
                        break;
                }
                if (!matches) continue;
            }

            result.add(f);
        }

        // ── Sắp xếp ──
        switch (sortMode) {
            case "PRICE_ASC":
                Collections.sort(result, (a, b) -> Double.compare(
                        showFullPrice ? a.getDisplayPrice() + a.getTaxFee() : a.getDisplayPrice(),
                        showFullPrice ? b.getDisplayPrice() + b.getTaxFee() : b.getDisplayPrice()));
                break;
            case "PRICE_DESC":
                Collections.sort(result, (a, b) -> Double.compare(
                        showFullPrice ? b.getDisplayPrice() + b.getTaxFee() : b.getDisplayPrice(),
                        showFullPrice ? a.getDisplayPrice() + a.getTaxFee() : a.getDisplayPrice()));
                break;
            case "DEPART_EARLY":
                Collections.sort(result, (a, b) ->
                        compareTime(a.getDepartureTime(), b.getDepartureTime()));
                break;
            case "DEPART_LATE":
                Collections.sort(result, (a, b) ->
                        compareTime(b.getDepartureTime(), a.getDepartureTime()));
                break;
            case "DURATION":
                Collections.sort(result, (a, b) ->
                        parseDurationMinutes(a.getDuration()) - parseDurationMinutes(b.getDuration()));
                break;
        }

        return result;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Reset
    // ══════════════════════════════════════════════════════════════════════

    private void setupResetButton() {
        tvBtnReset.setOnClickListener(v -> resetAllFilters());
    }

    private void resetAllFilters() {
        // Giá hiển thị
        showFullPrice = true;
        updatePriceChipUI(true);

        // Sắp xếp
        sortMode = "PRICE_ASC";
        rgSortBy.check(R.id.rb_sort_price_asc);

        // Hãng bay: chọn lại tất cả
        if (airlineAdapter != null) {
            airlineAdapter.selectAll();
        }

        // Thời gian: bỏ chọn tất cả
        for (int i = 0; i < timeSlotSelected.size(); i++) {
            timeSlotSelected.set(i, false);
        }
        resetChipUI(chipMorning);
        resetChipUI(chipAfternoon);
        resetChipUI(chipNight);

        // Hạng ghế
        seatClassFilter = "ALL";
        rgSeatClass.check(R.id.rb_class_all);
    }

    private void resetChipUI(LinearLayout chip) {
        chip.setBackgroundResource(R.drawable.bg_filter_chip_unselected);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    /** Parse "HH:mm" → giờ (int). Trả về -1 nếu lỗi. */
    private int parseDepartureHour(String time) {
        if (time == null || !time.contains(":")) return -1;
        try { return Integer.parseInt(time.split(":")[0]); }
        catch (NumberFormatException e) { return -1; }
    }

    /** So sánh 2 chuỗi "HH:mm" để sắp xếp. */
    private int compareTime(String t1, String t2) {
        if (t1 == null) return 1;
        if (t2 == null) return -1;
        return t1.compareTo(t2); // "HH:mm" so sánh lexicographic là đúng
    }

    /**
     * Parse thời gian bay "2h10p" hoặc "1h 30m" sang phút.
     * Trả về 0 nếu không parse được.
     */
    private int parseDurationMinutes(String duration) {
        if (duration == null) return 0;
        int total = 0;
        try {
            // Chuẩn hoá: "2h10p" / "2h 10m" / "2h10m"
            String d = duration.toLowerCase()
                    .replace("p", "m").replace(" ", "");
            int hIdx = d.indexOf('h');
            int mIdx = d.indexOf('m');
            if (hIdx >= 0) {
                total += Integer.parseInt(d.substring(0, hIdx)) * 60;
            }
            if (mIdx > hIdx + 1) {
                String mPart = d.substring(hIdx + 1, mIdx);
                if (!mPart.isEmpty()) total += Integer.parseInt(mPart);
            }
        } catch (Exception ignored) { }
        return total;
    }


    // ══════════════════════════════════════════════════════════════════════
    // Inner class: AirlineFilterItem (dữ liệu cho mỗi hàng hãng bay)
    // ══════════════════════════════════════════════════════════════════════

    public static class AirlineFilterItem {
        public final String name;
        public final String logoUrl;
        public double minPrice;

        public AirlineFilterItem(String name, String logoUrl, double minPrice) {
            this.name     = name;
            this.logoUrl  = logoUrl;
            this.minPrice = minPrice;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Inner class: AirlineFilterAdapter
    // ══════════════════════════════════════════════════════════════════════

    public static class AirlineFilterAdapter
            extends RecyclerView.Adapter<AirlineFilterAdapter.VH> {

        private final List<AirlineFilterItem> items;
        private final List<String>            selectedNames;
        private final Runnable                onChanged;

        public AirlineFilterAdapter(List<AirlineFilterItem> items,
                                    List<String> selectedNames,
                                    Runnable onChanged) {
            this.items         = items;
            this.selectedNames = selectedNames;
            this.onChanged     = onChanged;
        }

        public void selectAll() {
            selectedNames.clear();
            for (AirlineFilterItem item : items) selectedNames.add(item.name);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_filter_airline, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AirlineFilterItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvMinPrice.setText(String.format(Locale.getDefault(),
                    "từ %,.0fđ", item.minPrice));
            holder.cbCheck.setChecked(selectedNames.contains(item.name));

            Glide.with(holder.itemView.getContext())
                    .load(item.logoUrl)
                    .placeholder(R.drawable.ic_airline)
                    .error(R.drawable.ic_airline)
                    .into(holder.imgLogo);

            // Thêm/xoá hãng khi click cả dòng
            View.OnClickListener toggleClick = v -> {
                if (selectedNames.contains(item.name)) {
                    selectedNames.remove(item.name);
                } else {
                    selectedNames.add(item.name);
                }
                holder.cbCheck.setChecked(selectedNames.contains(item.name));
                if (onChanged != null) onChanged.run();
            };

            holder.itemView.setOnClickListener(toggleClick);
            holder.cbCheck.setOnClickListener(toggleClick);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            android.widget.CheckBox cbCheck;
            android.widget.ImageView imgLogo;
            TextView tvName, tvMinPrice;

            VH(@NonNull View v) {
                super(v);
                cbCheck    = v.findViewById(R.id.cb_airline_check);
                imgLogo    = v.findViewById(R.id.img_airline_logo);
                tvName     = v.findViewById(R.id.tv_airline_name);}
        }
    }
}
