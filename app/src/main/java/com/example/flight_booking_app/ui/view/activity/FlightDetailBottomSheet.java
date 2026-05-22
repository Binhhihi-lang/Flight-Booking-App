package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * BottomSheet hiển thị chi tiết chuyến bay sau khi người dùng bấm chọn.
 * <p>
 * NHẬN dữ liệu qua Bundle (không phải Intent — Fragment không dùng Intent).
 * Dùng static factory newInstance() để tạo với Bundle đúng cách.
 * <p>
 * CALLBACK về SearchFlightActivity qua interface OnFlightActionListener:
 * onOutboundConfirmed() → khứ hồi, lượt đi: đóng sheet, chuyển tab về
 * onBookingConfirmed()  → 1 chiều hoặc lượt về: tiến đến đặt vé
 */
public class FlightDetailBottomSheet extends BottomSheetDialogFragment {

    // ── Bundle keys
    public static final String KEY_FLIGHT_NUMBER = "flight_number";
    public static final String KEY_AIRLINE_NAME = "airline_name";
    public static final String KEY_AIRLINE_LOGO = "airline_logo";
    public static final String KEY_FROM_CITY = "from_city";
    public static final String KEY_FROM_IATA = "from_iata";
    public static final String KEY_TO_CITY = "to_city";
    public static final String KEY_TO_IATA = "to_iata";
    public static final String KEY_DEPART_TIME = "departure_time";
    public static final String KEY_ARRIVAL_TIME = "arrival_time";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_DEPART_DATE = "departure_date";
    public static final String KEY_BASE_PRICE = "display_price";
    public static final String KEY_TAX_FEE = "tax_fee";
    public static final String KEY_SEAT_CLASS = "seat_class";
    public static final String KEY_FARE_CLASS_NAME = "fare_class_name";
    public static final String KEY_BAGGAGE = "checked_baggage";
    public static final String KEY_ADULT_COUNT = "adult_count";
    public static final String KEY_CHILD_COUNT = "child_count";
    public static final String KEY_BABY_COUNT = "baby_count";
    public static final String KEY_IS_DEPART = "is_depart";
    public static final String KEY_IS_ROUND_TRIP = "is_round_trip";

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
    private TextView tvBasePrice, tvTaxFee, tvFareClass, tvTotalPrice;
    private TextView tvBaggage, tvRefundPolicy;
    private TextView tvFooterTotalPrice;
    private TextView tvFooterRouteSummary, tvFooterSubPrice;
    private MaterialButton btnBack, btnSelect;

    // ── Factory method — tạo instance với Bundle

    public static FlightDetailBottomSheet newInstance(
            String flightNumber, String airlineName, String airlineLogo,
            String fromCity, String fromIata, String toCity, String toIata,
            String departTime, String arrivalTime, String duration, String departDate,
            double basePrice, double taxFee,
            String seatClass, String fareClassName, int baggage,
            int adultCount, int childCount, int babyCount,
            boolean isDepart, boolean isRoundTrip) {

        Bundle args = new Bundle();
        args.putString(KEY_FLIGHT_NUMBER, flightNumber);
        args.putString(KEY_AIRLINE_NAME, airlineName);
        args.putString(KEY_AIRLINE_LOGO, airlineLogo);
        args.putString(KEY_FROM_CITY, fromCity);
        args.putString(KEY_FROM_IATA, fromIata);
        args.putString(KEY_TO_CITY, toCity);
        args.putString(KEY_TO_IATA, toIata);
        args.putString(KEY_DEPART_TIME, departTime);
        args.putString(KEY_ARRIVAL_TIME, arrivalTime);
        args.putString(KEY_DURATION, duration);
        args.putString(KEY_DEPART_DATE, departDate);
        args.putDouble(KEY_BASE_PRICE, basePrice);
        args.putDouble(KEY_TAX_FEE, taxFee);
        args.putString(KEY_SEAT_CLASS, seatClass);
        args.putString(KEY_FARE_CLASS_NAME, fareClassName != null ? fareClassName : seatClass);
        args.putInt(KEY_BAGGAGE, baggage);
        args.putInt(KEY_ADULT_COUNT, adultCount);
        args.putInt(KEY_CHILD_COUNT, childCount);
        args.putInt(KEY_BABY_COUNT, babyCount);
        args.putBoolean(KEY_IS_DEPART, isDepart);
        args.putBoolean(KEY_IS_ROUND_TRIP, isRoundTrip);

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
        tvTotalPrice = view.findViewById(R.id.tv_detail_total_price);
        tvBaggage = view.findViewById(R.id.tv_detail_baggage);
        tvRefundPolicy = view.findViewById(R.id.tv_detail_refund_policy);
        tvFooterTotalPrice = view.findViewById(R.id.tv_footer_total_price);
        tvFooterRouteSummary = view.findViewById(R.id.tv_footer_route_summary);
        tvFooterSubPrice = view.findViewById(R.id.tv_footer_sub_price);
        btnBack = view.findViewById(R.id.btn_detail_back);
        btnSelect = view.findViewById(R.id.btn_detail_select);
    }

    private void renderData() {
        Bundle args = getArguments();
        if (args == null) return;

        // Đọc từ Bundle
        String flightNumber = args.getString(KEY_FLIGHT_NUMBER, "");
        String airlineLogo = args.getString(KEY_AIRLINE_LOGO, "");
        String fromCity = args.getString(KEY_FROM_CITY, "");
        String fromIata = args.getString(KEY_FROM_IATA, "");
        String toCity = args.getString(KEY_TO_CITY, "");
        String toIata = args.getString(KEY_TO_IATA, "");
        String departTime = args.getString(KEY_DEPART_TIME, "");
        String arrivalTime = args.getString(KEY_ARRIVAL_TIME, "");
        String duration = args.getString(KEY_DURATION, "");
        String departDate = args.getString(KEY_DEPART_DATE, "");
        double basePrice = args.getDouble(KEY_BASE_PRICE, 0);
        double taxFee = args.getDouble(KEY_TAX_FEE, 0);
        String fareClassName = args.getString(KEY_FARE_CLASS_NAME, "");
        int baggage = args.getInt(KEY_BAGGAGE, 0);
        int adult = args.getInt(KEY_ADULT_COUNT, 1);
        int child = args.getInt(KEY_CHILD_COUNT, 0);
        int baby = args.getInt(KEY_BABY_COUNT, 0);
        boolean isRoundTrip = args.getBoolean(KEY_IS_ROUND_TRIP, false);
        boolean isDepart = args.getBoolean(KEY_IS_DEPART, true);

        // Tính tổng tiền = (basePrice + taxFee) * adult + 75% * child + 10% * baby
        double total = (basePrice + taxFee) * adult
                + (basePrice + taxFee) * 0.75 * child
                + (basePrice + taxFee) * 0.10 * baby;

        // Hiển thị thông tin chuyến bay

        tvDate.setText(departDate);
        tvDepartTime.setText(departTime);
        tvFromIata.setText(fromIata);
        tvDuration.setText(duration);
        tvArrivalTime.setText(arrivalTime);
        tvToIata.setText(toIata);
        tvFlightNumber.setText(flightNumber);
        tvFareClass.setText(fareClassName);

        //  Giá

        tvBasePrice.setText(formatPrice(basePrice));
        tvTaxFee.setText(formatPrice(taxFee));
        tvTotalPrice.setText(formatPrice(basePrice + taxFee));

        // Hành lý

        if (baggage > 0) {
            tvBaggage.setText("Hành lý ký gửi " + baggage + "kg");
        } else {
            tvBaggage.setText("Không bao gồm hành lý ký gửi");
        }


        Glide.with(requireContext())
                .load(airlineLogo)
                .placeholder(R.drawable.ic_airline)
                .error(R.drawable.ic_airline)
                .into(imgAirlineLogo);


        tvFooterTotalPrice.setText(formatPrice(total));
        tvFooterRouteSummary.setText(fromCity + " -> " + toCity + "  " + departTime + "  " + departDate);
        tvFooterSubPrice.setText(formatPrice(total));

        //   Khứ hồi + đang ở lượt đi → "CHỌN LƯỢT ĐI"
        //   Khứ hồi + đang ở lượt về → "XÁC NHẬN CHUYẾN VỀ"
        //   1 chiều                   → "CHỌN"

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

            boolean isRoundTrip = args.getBoolean(KEY_IS_ROUND_TRIP, false);
            boolean isDepart = args.getBoolean(KEY_IS_DEPART, true);

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