package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.FareRule;
import com.example.flight_booking_app.data.model.Flight;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class FlightDetailBottomSheet extends BottomSheetDialogFragment {

    // ── Callback interface

    public interface OnFlightActionListener {
        /**
         * Khứ hồi + lượt đi: người dùng xác nhận chuyến đi.
         * SearchFlightActivity sẽ đóng sheet, chuyển sang tab LƯỢT VỀ,
         * hiển thị Snackbar nhắc chọn chuyến về.
         */
        void onOutboundConfirmed();

        /**
         * 1 chiều hoặc đã xác nhận lượt về.
         * SearchFlightActivity sẽ mở BookingActivity.
         */
        void onBookingConfirmed();
    }

    private OnFlightActionListener listener;


    private ImageView imgAirlineLogo;
    private TextView tvDate, tvDepartTime, tvFromIata;
    private TextView tvDuration, tvArrivalTime, tvToIata;
    private TextView tvFlightNumber, tvFlightType;
    private TextView tvBasePrice, tvTaxFee, tvFareClass, tvPassengerQuantity, tvTotalPrice;

    private LinearLayout rowCheckedBaggage, rowCarryBaggage, rowRefundPolicy, rowMealPolicy ,rowLounge, rowPriority;
    private TextView tvCabinBaggage, tvCheckedBaggage, tvRefund, tvMeal, tvLounge, tvPriority;
    private TextView tvFooterTotalPrice;
    private TextView tvFooterRouteSummary, tvFooterSubPrice;
    private MaterialButton btnBack, btnSelect;

    // ── Factory method — tạo instance với Bundle

    public static FlightDetailBottomSheet newInstance(
            Flight flight,
            FareClass fareClass,
            String travelDate,
            int adultCount, int childCount, int babyCount,
            boolean isDepart, boolean isRoundTrip) {

        Bundle args = new Bundle();

        // TRUYỀN NGUYÊN OBJECT QUA BUNDLE
        args.putSerializable("selected_flight", flight);
        args.putSerializable("selected_fare_class", fareClass);
        args.putString("travel_date", travelDate);
        args.putInt("adult_count", adultCount);
        args.putInt("child_count", childCount);
        args.putInt("baby_count", babyCount);
        args.putBoolean("is_depart", isDepart);
        args.putBoolean("is_round_trip", isRoundTrip);

        FlightDetailBottomSheet sheet = new FlightDetailBottomSheet();
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_flight_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Listener là Activity đang host sheet này
        if (getActivity() instanceof OnFlightActionListener) {
            listener = (OnFlightActionListener) getActivity();
        }

        bindViews(view);
        renderData();
        setupClickListeners();
    }

    private void bindViews(View view) {
        imgAirlineLogo = view.findViewById(R.id.img_detail_airline_logo);
        tvDate = view.findViewById(R.id.tv_detail_flight_date);
        tvDepartTime = view.findViewById(R.id.tv_detail_depart_time);
        tvFromIata = view.findViewById(R.id.tv_airport_code);
        tvDuration = view.findViewById(R.id.tv_detail_duration);
        tvArrivalTime = view.findViewById(R.id.tv_detail_arrival_time);
        tvToIata = view.findViewById(R.id.tv_To_Iata);
        tvFlightNumber = view.findViewById(R.id.tv_detail_flight_number);
        tvFlightType = view.findViewById(R.id.tv_detail_flight_type);
        tvBasePrice = view.findViewById(R.id.tv_detail_base_price);
        tvTaxFee = view.findViewById(R.id.tv_detail_tax_fee);
        tvFareClass = view.findViewById(R.id.tv_detail_fare_class);
        tvPassengerQuantity = view.findViewById(R.id.tv_footer_lbl_price_per_person);
        tvTotalPrice = view.findViewById(R.id.tv_detail_total_price);

        // Các Layout quy định dịch vụ
        rowCheckedBaggage = view.findViewById(R.id.row_carry_baggage);
        rowCarryBaggage = view.findViewById(R.id.row_carry_bag);
        rowRefundPolicy = view.findViewById(R.id.row_refund_policy);
        rowMealPolicy = view.findViewById(R.id.row_meal_policy);

         rowLounge = view.findViewById(R.id.row_lounge_policy);
         rowPriority = view.findViewById(R.id.row_priority_policy);
         

        // Các TextView hiển thị text dịch vụ
        tvCabinBaggage = view.findViewById(R.id.tv_detail_cabinBaggage);
        tvCheckedBaggage = view.findViewById(R.id.tv_detail_checkedBaggage); // Thêm dòng này để findViewById
        tvRefund = view.findViewById(R.id.tv_detail_refund_policy);
        tvMeal = view.findViewById(R.id.tv_detail_meal_policy);
        tvLounge = view.findViewById(R.id.tv_detail_lounge_policy);
        tvPriority = view.findViewById(R.id.tv_detail_priority_policy);

        // Footer
        tvFooterTotalPrice = view.findViewById(R.id.tv_footer_total_price);
        tvFooterRouteSummary = view.findViewById(R.id.tv_footer_route_summary);
        tvFooterSubPrice = view.findViewById(R.id.tv_footer_sub_price);
        btnBack = view.findViewById(R.id.btn_detail_back);
        btnSelect = view.findViewById(R.id.btn_detail_select);
    }

    private void renderData() {
        Bundle args = getArguments();
        if (args == null) return;

        // ── 1. MÓC DỮ LIỆU TỪ BUNDLE VÀ OBJECT ──
        Flight flight = (Flight) args.getSerializable("selected_flight");
        FareClass fareClass = (FareClass) args.getSerializable("selected_fare_class");

        if (flight == null || fareClass == null) return;

        String travelDate = args.getString("travel_date", "");
        int adult = args.getInt("adult_count", 1);
        int child = args.getInt("child_count", 0);
        int baby = args.getInt("baby_count", 0);
        boolean isRoundTrip = args.getBoolean("is_round_trip", false);
        boolean isDepart = args.getBoolean("is_depart", true);

        // ── 2. XỬ LÝ PHẦN DỊCH VỤ (TỪ FARE RULE) ──
        if (fareClass.getFareRule() != null) {
            FareRule rule = fareClass.getFareRule();

            // Hành lý xách tay
            if (rowCarryBaggage != null && tvCabinBaggage != null) {
                rowCarryBaggage.setVisibility(View.VISIBLE);
                tvCabinBaggage.setText("Hành lý xách tay " + rule.getCabinBaggage() + "kg");
            }

            // Hành lý ký gửi
            if (rowCheckedBaggage != null) {
                if (tvCheckedBaggage != null) {
                    if (rule.getCheckedBaggage() > 0) {
                        rowCheckedBaggage.setVisibility(View.VISIBLE);
                        tvCheckedBaggage.setText("Hành lý ký gửi " + rule.getCheckedBaggage() + "kg");
                    } else {
                        tvCheckedBaggage.setText("Không bao gồm hành lý ký gửi");
                    }
                }
            }

            // Đổi vé
            if (rowRefundPolicy != null && tvRefund != null) {
                rowRefundPolicy.setVisibility(rule.isChangeable() ? View.VISIBLE : View.GONE);
                tvRefund.setText("Được phép đổi vé");
            }

            // Suất ăn
            if (rowMealPolicy != null) {
                if (tvMeal != null) {
                    rowMealPolicy.setVisibility(rule.isHasMeal() ? View.VISIBLE : View.GONE);
                    tvMeal.setText("Bao gồm suất ăn trên máy bay");
                }
            }

            // Nếu có UI cho Phòng chờ VIP / Ưu tiên

            if (rowLounge != null) {
                rowLounge.setVisibility(rule.isHasLoungeAccess() ? View.VISIBLE : View.GONE);
                tvLounge.setText("Quyền vào phòng chờ VIP");
            }
            if (rowPriority != null) {
                rowPriority.setVisibility(rule.isHasPriority() ? View.VISIBLE : View.GONE);
                tvPriority.setText("Ưu tiên làm thủ tục & lên máy bay");
            }

        }

        // ── 3. TÍNH TOÁN GIÁ TIỀN CHUẨN ──
        int totalPassenger = adult + child + baby;
        double basePrice = fareClass.getBasePrice(); // Giá lấy từ hạng vé
        double taxFee = flight.getTaxFee();          // Thuế lấy từ chuyến bay

        // Công thức: N.Lớn(100%) + Trẻ Em(75%) + Em Bé(10%)
        double total = (basePrice + taxFee) * adult
                + (basePrice + taxFee) * 0.75 * child
                + (basePrice + taxFee) * 0.10 * baby;

        // ── 4. HIỂN THỊ THÔNG TIN CHUYẾN BAY (TỪ OBJECT FLIGHT) ──
        tvDate.setText(travelDate);
        tvDepartTime.setText(flight.getDepartureTime());
        tvFromIata.setText(flight.getFromIata());
        tvDuration.setText(flight.getDuration());
        tvArrivalTime.setText(flight.getArrivalTime());
        tvToIata.setText(flight.getToIata());
        tvFlightNumber.setText(flight.getFlightNumber());
        tvFareClass.setText(fareClass.getTitle()); // Tên hạng vé (Ví dụ: Eco Save)

        // ── 5. HIỂN THỊ CHI TIẾT GIÁ ──
        tvBasePrice.setText(formatPrice(basePrice));
        tvTaxFee.setText(formatPrice(taxFee));
        tvTotalPrice.setText(formatPrice(basePrice + taxFee)); // Giá 1 người lớn

        Glide.with(requireContext())
                .load(flight.getAirlineLogo())
                .placeholder(R.drawable.ic_airline)
                .error(R.drawable.ic_airline)
                .into(imgAirlineLogo);

        // ── 6. HIỂN THỊ FOOTER TỔNG KẾT ──
        tvPassengerQuantity.setText("Tổng số tiền " + totalPassenger + " người");
        tvFooterTotalPrice.setText(formatPrice(total));

        // Chuỗi: "Hà Nội -> TP.HCM  10:30  20/11/2026"
        String routeSummary = flight.getFrom() + " -> " + flight.getTo() + "  " + flight.getDepartureTime() + "  " + travelDate;
        tvFooterRouteSummary.setText(routeSummary);

        tvFooterSubPrice.setText(formatPrice(total));

        // ── 7. LOGIC NÚT BẤM (Đã chuẩn) ──
        if (isRoundTrip && isDepart) {
            btnSelect.setText("CHỌN LƯỢT ĐI");
        } else if (isRoundTrip) {
            btnSelect.setText("XÁC NHẬN CHUYẾN VỀ");
        } else {
            btnSelect.setText("CHỌN");
        }
    }

    private void setupClickListeners() {
        // Nút TRỞ VỀ
        btnBack.setOnClickListener(v -> dismiss());

        // Nút CHỌN xử lý theo ngữ cảnh
        btnSelect.setOnClickListener(v -> {
            if (listener == null) return;

            Bundle args = getArguments();
            if (args == null) return;

            boolean isRoundTrip = args.getBoolean("is_round_trip", false);
            boolean isDepart = args.getBoolean("is_depart", true);

            if (isRoundTrip && isDepart) {
                // Khứ hồi + lượt đi báo Activity chuyển sang tab về
                dismiss();
                listener.onOutboundConfirmed();
            } else {
                // 1 chiều hoặc đã chọn lượt về đến đặt vé
                dismiss();
                listener.onBookingConfirmed();
            }
        });
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0fđ", price);
    }
}