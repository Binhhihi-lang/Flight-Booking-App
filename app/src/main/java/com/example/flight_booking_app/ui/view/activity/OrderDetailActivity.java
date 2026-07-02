package com.example.flight_booking_app.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.Api.CreateOrder;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.ui.viewmodel.OrderDetailViewModel;
import com.example.flight_booking_app.utils.PriceFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvToolbarOrderCode;
    private TextView tvOrderStatusBanner;

    // ── Views: Card chuyến bay ─────────────────────────────────────────────
    private View cardReturnFlight;

    // Lượt đi
    private TextView tvOutboundRoute, tvOutboundDate;
    private TextView tvOutboundDepartTime, tvOutboundFromIata;
    private TextView tvOutboundArrivalTime, tvOutboundToIata;
    private TextView tvOutboundDuration, tvOutboundFlightNumber, tvOutboundFareClass;
    private ImageView imgOutboundLogo;
    private TextView tvOutboundTotalPrice;
    private TextView tvOutboundAdultPrice, tvOutboundChildPrice, tvOutboundBabyPrice;
    private View rowOutAdult, rowOutChild, rowOutBaby;

    // Lượt về
    private TextView tvReturnRoute, tvReturnDate;
    private TextView tvReturnDepartTime, tvReturnFromIata;
    private TextView tvReturnArrivalTime, tvReturnToIata;
    private TextView tvReturnDuration, tvReturnFlightNumber, tvReturnFareClass;
    private ImageView imgReturnLogo;
    private TextView tvReturnTotalPrice;
    private TextView tvReturnAdultPrice, tvReturnChildPrice, tvReturnBabyPrice;
    private View rowRetAdult, rowRetChild, rowRetBaby;

    // Hành khách constants
    private static final int ICON_ADULT = R.drawable.ic_nav_profile;
    private static final int ICON_CHILD = R.drawable.ic_child;
    private static final int ICON_BABY = R.drawable.ic_baby;
    private static final int ICON_CHECKED = R.drawable.ic_checked;
    private static final int COLOR_COMPLETE = Color.parseColor("#0175F3");

    // ── Views: Card thông tin đơn hàng ─────────────────────────────────────
    private View groupContactDetailed, groupContactCompact;
    private TextView tvContactNameDetail, tvContactEmailDetail, tvContactPhoneDetail;
    private TextView tvContactNameCompact, tvContactEmailCompact;
    private TextView tvOrderCode, tvOrderStatus;
    private View rowPaymentDeadline;
    private TextView tvPaymentDeadline;
    private TextView tvFarePrice, tvSubtotalPrice, tvGrandTotal;
    private View layoutBookingCodeReturn;
    private TextView tvBookingCodeOutbound, tvBookingCodeReturn;

    // ── Views: Card hành khách ─────────────────────────────────────────────
    private LinearLayout layoutPassengerList;

    private MaterialToolbar toolbar;

    // Views: Bottom bar
    private View layoutBottomFailed, layoutBottomPayment, layoutBottomPaymentExpire;
    private View layoutLoading;

    private MaterialButton btnRebookOrder;
    private MaterialButton btnContactSupport;
    private MaterialButton btnPayContactExpire;
    private MaterialButton btnPayNow;
    private CountDownTimer countdownTimer;

    // ── ViewModel ──────────────────────────────────────────────────────────
    private OrderDetailViewModel viewModel;
    private String bookingId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        observeViewModel();
        setupClickListeners();

    }

    private void bindViews() {
        tvToolbarOrderCode = findViewById(R.id.tv_toolbar_order_code);
        tvOrderStatusBanner = findViewById(R.id.tv_order_status_banner);

        // Lượt đi
        tvOutboundRoute = findViewById(R.id.tv_outbound_route_order);
        tvOutboundDate = findViewById(R.id.tv_outbound_date_order);
        tvOutboundDepartTime = findViewById(R.id.tv_outbound_depart_time_order);
        tvOutboundFromIata = findViewById(R.id.tv_outbound_from_iata_order);
        tvOutboundArrivalTime = findViewById(R.id.tv_outbound_arrival_time_order);
        tvOutboundToIata = findViewById(R.id.tv_outbound_to_iata_order);
        tvOutboundDuration = findViewById(R.id.tv_outbound_duration_order);
        tvOutboundFlightNumber = findViewById(R.id.tv_outbound_flight_number_order);
        tvOutboundFareClass = findViewById(R.id.tv_outbound_fare_class_order);
        imgOutboundLogo = findViewById(R.id.img_outbound_airline_logo_order);
        tvOutboundTotalPrice = findViewById(R.id.tv_outbound_total_price_order);
        tvOutboundAdultPrice = findViewById(R.id.tv_outbound_adult_price_order);
        tvOutboundChildPrice = findViewById(R.id.tv_outbound_child_price_order);
        tvOutboundBabyPrice = findViewById(R.id.tv_outbound_baby_price_order);
        rowOutAdult = findViewById(R.id.row_outbound_adult_price_order);
        rowOutChild = findViewById(R.id.row_outbound_child_price_order);
        rowOutBaby = findViewById(R.id.row_outbound_baby_price_order);

        // Lượt về
        cardReturnFlight = findViewById(R.id.card_return_flight_order_order);
        tvReturnRoute = findViewById(R.id.tv_return_route_order);
        tvReturnDate = findViewById(R.id.tv_return_date_order);
        tvReturnDepartTime = findViewById(R.id.tv_return_depart_time_order);
        tvReturnFromIata = findViewById(R.id.tv_return_from_iata_order);
        tvReturnArrivalTime = findViewById(R.id.tv_return_arrival_time_order);
        tvReturnToIata = findViewById(R.id.tv_return_to_iata_order);
        tvReturnDuration = findViewById(R.id.tv_return_duration_order);
        tvReturnFlightNumber = findViewById(R.id.tv_return_flight_number_order);
        tvReturnFareClass = findViewById(R.id.tv_return_fare_class_order);
        imgReturnLogo = findViewById(R.id.img_return_airline_logo_order);
        tvReturnTotalPrice = findViewById(R.id.tv_return_total_price_order);
        tvReturnAdultPrice = findViewById(R.id.tv_return_adult_price_order);
        tvReturnChildPrice = findViewById(R.id.tv_return_child_price_order);
        tvReturnBabyPrice = findViewById(R.id.tv_return_baby_price_order);
        rowRetAdult = findViewById(R.id.row_return_adult_price_order);
        rowRetChild = findViewById(R.id.row_return_child_price_order);
        rowRetBaby = findViewById(R.id.row_return_baby_price_order);

        // Đơn hàng
        groupContactDetailed = findViewById(R.id.group_contact_detailed);
        groupContactCompact = findViewById(R.id.group_contact_compact);
        tvContactNameDetail = findViewById(R.id.tv_contact_name_detail);
        tvContactEmailDetail = findViewById(R.id.tv_contact_email_detail);
        tvContactPhoneDetail = findViewById(R.id.tv_contact_phone_detail);
        tvContactNameCompact = findViewById(R.id.tv_contact_name_compact);
        tvContactEmailCompact = findViewById(R.id.tv_contact_email_compact);
        tvOrderCode = findViewById(R.id.tv_order_code);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        rowPaymentDeadline = findViewById(R.id.row_payment_deadline);
        tvPaymentDeadline = findViewById(R.id.tv_payment_deadline);
        tvFarePrice = findViewById(R.id.tv_fare_price);
        tvSubtotalPrice = findViewById(R.id.tv_subtotal_price);
        tvGrandTotal = findViewById(R.id.tv_grand_total);
        layoutBookingCodeReturn = findViewById(R.id.layout_booking_code_return);
        tvBookingCodeOutbound = findViewById(R.id.tv_booking_code_outbound);
        tvBookingCodeReturn = findViewById(R.id.tv_booking_code_return);

        layoutPassengerList = findViewById(R.id.layout_order_passenger_list);
        layoutBottomFailed = findViewById(R.id.layout_bottom_failed);
        layoutBottomPayment = findViewById(R.id.layout_bottom_payment);
        layoutBottomPaymentExpire = findViewById(R.id.layout_bottom_payment_expire);
        layoutLoading = findViewById(R.id.layout_loading);

        btnRebookOrder = findViewById(R.id.btn_rebook_order);
        btnContactSupport = findViewById(R.id.btn_contact_support);
        btnPayContactExpire = findViewById(R.id.btn_pay_contact);
        btnPayNow = findViewById(R.id.btn_pay_now);

        toolbar = findViewById(R.id.toolbar_order_detail);

        // Thanh toán Zalopay
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);


    }

    private void observeViewModel() {
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        bookingId = getIntent().getStringExtra("booking_id");

        // quan sát trạng thái loading
        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    layoutLoading.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    layoutLoading.setVisibility(View.GONE);
                    break;

                case ERROR:
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + state.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // quan sát dữ liệu đơn hàng
        viewModel.getBooking().observe(this, booking -> {
            if (booking != null) populateAll(booking);
        });

        // Trong observeViewModel() — OrderDetailActivity.java
        viewModel.getCountdownLive().observe(this, remainingMs -> {
            if (remainingMs == null || remainingMs <= 0) return;

            if (countdownTimer != null) countdownTimer.cancel();

            rowPaymentDeadline.setVisibility(View.VISIBLE);

            countdownTimer = new CountDownTimer(remainingMs, 1000) {
                @Override
                public void onTick(long ms) {
                    long minutes = (ms / 1000) / 60;
                    long seconds = (ms / 1000) % 60;
                    tvPaymentDeadline.setText(
                            String.format(Locale.getDefault(), "Còn lại %02d:%02d", minutes, seconds));
                    tvPaymentDeadline.setTextColor(Color.parseColor("#E65100"));
                }

                @Override
                public void onFinish() {
                    // ViewModel.Handler tự gọi expireBooking() → snapshot bắn lại → UI tự đúng
                    tvPaymentDeadline.setText(getString(R.string.status_payment_expired));
                    tvPaymentDeadline.setTextColor(Color.RED);
                }
            }.start();
        });

        viewModel.startObservingBooking(bookingId);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    private void populateAll(Booking b) {
        tvToolbarOrderCode.setText(getString(R.string.label_order_code_format, b.getOrderCode()));
        populateStatusBanner(b.getStatus());
        populateOutboundCard(b);
        populateReturnCard(b);
        populateOrderInfo(b);
        populatePassengers(b.getPassengers());
        populateBottomBar(b);
    }

    private void populateStatusBanner(String status) {
        String text, bgColor;
        switch (status != null ? status : "") {
            case "RESERVATION_SUCCESS":
                text = "Đã giữ chỗ — Chờ thanh toán ⏳";
                bgColor = "#F59E0B";
                break;
            case "PAYMENT_SUCCESS":
                text = "Thanh toán thành công ✅";
                bgColor = "#16A34A";
                break;
            case "PAYMENT_EXPIRED":
                text = "Đã hết hạn thanh toán ⌛";
                bgColor = "#6B7280";
                break;
            default:
                text = "Đặt chỗ không thành công ❌";
                bgColor = "#B1B1B1";
                break;
        }
        tvOrderStatusBanner.setBackgroundColor(Color.parseColor(bgColor));
        tvOrderStatusBanner.setText(text);
    }

    private void populateOutboundCard(Booking b) {
        Flight f = b.getOutboundFlight();
        if (f == null) return;

        tvOutboundRoute.setText(f.getFrom() + " → " + f.getTo());
        tvOutboundDate.setText(PriceFormatter.formatDateOnly(f.getDepartureTime()));
        tvOutboundDepartTime.setText(PriceFormatter.formatTimeOnly(f.getDepartureTime()));
        tvOutboundArrivalTime.setText(PriceFormatter.formatTimeOnly(f.getArrivalTime()));
        tvOutboundFromIata.setText(f.getFromIata());
        tvOutboundToIata.setText(f.getToIata());
        tvOutboundDuration.setText(f.getDuration());
        tvOutboundFlightNumber.setText(f.getFlightNumber());
        tvOutboundFareClass.setText(b.getOutboundFare() != null ? b.getOutboundFare().getTitle() : "");

        Glide.with(this).load(f.getAirlineLogo()).into(imgOutboundLogo);

        // Tính giá chi tiết
        int adult = 0, child = 0, baby = 0;
        for (Passenger p : b.getPassengers()) {
            if ("ADULT".equals(p.getType())) adult++;
            else if ("CHILD".equals(p.getType())) child++;
            else if ("BABY".equals(p.getType())) baby++;
        }

        double base = b.getOutboundFare() != null ? b.getOutboundFare().getBasePrice() : 0;
        double tax = f.getTaxFee();
        double adultPrice = base + tax;
        double childPrice = adultPrice * 0.75;
        double babyPrice = adultPrice * 0.10;

        rowOutAdult.setVisibility(adult > 0 ? View.VISIBLE : View.GONE);
        tvOutboundAdultPrice.setText(PriceFormatter.formatCountAndPrice(adult, adultPrice));
        rowOutChild.setVisibility(child > 0 ? View.VISIBLE : View.GONE);
        tvOutboundChildPrice.setText(PriceFormatter.formatCountAndPrice(child, childPrice));
        rowOutBaby.setVisibility(baby > 0 ? View.VISIBLE : View.GONE);
        tvOutboundBabyPrice.setText(PriceFormatter.formatCountAndPrice(baby, babyPrice));

        tvOutboundTotalPrice.setText(PriceFormatter.formatPrice(adult * adultPrice + child * childPrice + baby * babyPrice));
    }

    private void populateReturnCard(Booking b) {
        if (!b.isRoundTrip() || b.getReturnFlight() == null) {
            cardReturnFlight.setVisibility(View.GONE);
            layoutBookingCodeReturn.setVisibility(View.GONE);
            return;
        }
        cardReturnFlight.setVisibility(View.VISIBLE);
        layoutBookingCodeReturn.setVisibility(View.VISIBLE);

        Flight f = b.getReturnFlight();
        tvReturnRoute.setText(f.getFrom() + " → " + f.getTo());
        tvReturnDate.setText(PriceFormatter.formatDateOnly(f.getDepartureTime()));
        tvReturnDepartTime.setText(PriceFormatter.formatTimeOnly(f.getDepartureTime()));
        tvReturnArrivalTime.setText(PriceFormatter.formatTimeOnly(f.getArrivalTime()));
        tvReturnFromIata.setText(f.getFromIata());
        tvReturnToIata.setText(f.getToIata());
        tvReturnDuration.setText(f.getDuration());
        tvReturnFlightNumber.setText(f.getFlightNumber());
        tvReturnFareClass.setText(b.getReturnFare() != null ? b.getReturnFare().getTitle() : "");

        Glide.with(this).load(f.getAirlineLogo()).into(imgReturnLogo);


        int adult = 0, child = 0, baby = 0;
        for (Passenger p : b.getPassengers()) {
            if ("ADULT".equals(p.getType())) adult++;
            else if ("CHILD".equals(p.getType())) child++;
            else if ("BABY".equals(p.getType())) baby++;
        }

        double base = b.getReturnFare() != null ? b.getReturnFare().getBasePrice() : 0;
        double tax = f.getTaxFee();
        double adultPrice = base + tax;
        double childPrice = adultPrice * 0.75;
        double babyPrice = adultPrice * 0.10;

        rowRetAdult.setVisibility(adult > 0 ? View.VISIBLE : View.GONE);
        tvReturnAdultPrice.setText(PriceFormatter.formatCountAndPrice(adult, adultPrice));
        rowRetChild.setVisibility(child > 0 ? View.VISIBLE : View.GONE);
        tvReturnChildPrice.setText(PriceFormatter.formatCountAndPrice(child, childPrice));
        rowRetBaby.setVisibility(baby > 0 ? View.VISIBLE : View.GONE);
        tvReturnBabyPrice.setText(PriceFormatter.formatCountAndPrice(baby, babyPrice));

        tvReturnTotalPrice.setText(PriceFormatter.formatPrice(adult * adultPrice + child * childPrice + baby * babyPrice));
    }

    private void populateOrderInfo(Booking b) {
        String status = b.getStatus() != null ? b.getStatus() : "";
        boolean isPending = "RESERVATION_SUCCESS".equals(status);
        boolean isExpired = "PAYMENT_EXPIRED".equals(status);
        boolean isSuccess = "PAYMENT_SUCCESS".equals(status);
        boolean isFailed = "RESERVATION_FAILED".equals(status);


        // Nhóm liên hệ: compact khi chờ thanh toán / thành công, detailed khi thất bại / hết hạn
        boolean showCompact = isPending || isSuccess;
        groupContactDetailed.setVisibility(showCompact ? View.GONE : View.VISIBLE);
        groupContactCompact.setVisibility(showCompact ? View.VISIBLE : View.GONE);

        tvContactNameDetail.setText(b.getContactName());
        tvContactEmailDetail.setText(b.getContactEmail());
        tvContactPhoneDetail.setText(b.getContactPhone());
        tvContactNameCompact.setText(b.getContactName());
        tvContactEmailCompact.setText(b.getContactPhone() + " - " + b.getContactEmail());

        tvOrderCode.setText("#" + b.getOrderCode());
        // Dùng getString(R.string...) để chuyển ID thành nội dung văn bản
        tvBookingCodeOutbound.setText(b.getBookingCode() != null
                ? b.getBookingCode()
                : getResources().getString(R.string.status_booking_pending));
        tvBookingCodeReturn.setText(b.getBookingCode() != null
                ? b.getBookingCode()
                : getResources().getString(R.string.status_booking_pending));

        tvOrderStatus.setText(statusLabel(status));
        tvOrderStatus.setTextColor(Color.parseColor(statusColor(status)));

        // Tính toán phụ phí (ghế + hành lý)
        double totalAddons = 0;
        double totalBaseTickets;
        if (b.getPassengers() != null) {
            for (Passenger p : b.getPassengers()) {
                totalAddons += p.getTotalPriceWithServices();
            }
        }
        totalBaseTickets = b.getTotalAmount() - totalAddons;

        tvFarePrice.setText(PriceFormatter.formatPrice(totalBaseTickets));
        tvSubtotalPrice.setText(PriceFormatter.formatPrice(totalAddons));
        tvGrandTotal.setText(PriceFormatter.formatPrice(b.getTotalAmount()));
    }

    private void populatePassengers(List<Passenger> passengers) {
        layoutPassengerList.removeAllViews();
        if (passengers == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Passenger p : passengers) {
            View v = inflater.inflate(R.layout.item_passenger_input, layoutPassengerList, false);

            // Icon
            ((ImageView) v.findViewById(R.id.img_passenger_icon)).setImageResource(
                    "ADULT".equals(p.getType()) ? ICON_ADULT : "CHILD".equals(p.getType()) ? ICON_CHILD : ICON_BABY);

            // Trạng thái hoàn thành
            if (p.isComplete()) {
                ImageView check = v.findViewById(R.id.img_passenger_check);
                check.setImageResource(ICON_CHECKED);
                check.setColorFilter(COLOR_COMPLETE, PorterDuff.Mode.SRC_IN);
                check.setVisibility(View.VISIBLE);
                v.findViewById(R.id.tv_passenger_mandatory).setVisibility(View.GONE);
            }

            // Tên hành khách
            ((TextView) v.findViewById(R.id.tv_passenger_label)).setText(
                    (p.getFullName() != null && !p.getFullName().isEmpty()) ? p.getTitle() + ": " + p.getFullName().toUpperCase() : p.getLabel());

            // Thông tin bổ sung
            v.findViewById(R.id.row_InfoAdd_container).setVisibility(View.VISIBLE);

            TextView dob = v.findViewById(R.id.tv_dob_add);
            dob.setVisibility(p.getDateOfBirth() != null ? View.VISIBLE : View.GONE);
            if (p.getDateOfBirth() != null) dob.setText(getString(R.string.label_passenger_dob, p.getDateOfBirth()));

            TextView id = v.findViewById(R.id.tv_idNumber_add);
            id.setVisibility(p.getIdNumber() != null ? View.VISIBLE : View.GONE);
            if (p.getIdNumber() != null) id.setText(getString(R.string.label_passenger_id, p.getIdNumber()));

            TextView baggage = v.findViewById(R.id.tv_baggage_add);
            boolean hasOut = p.getOutboundBaggageId() != null && !p.getOutboundBaggageId().isEmpty();
            boolean hasRet = p.getReturnBaggageId() != null && !p.getReturnBaggageId().isEmpty();

            // Lấy context từ chính textview baggage hoặc itemView của ViewHolder
            Context context = baggage.getContext();

            if (hasOut && hasRet) {
                baggage.setText(context.getString(R.string.label_baggage_both, p.getOutboundBaggageWeight(), p.getReturnBaggageWeight()));
            } else if (hasOut) {
                baggage.setText(context.getString(R.string.label_baggage_outbound_orderDetail, p.getOutboundBaggageWeight()));
            } else if (hasRet) {
                baggage.setText(context.getString(R.string.label_baggage_return_orderDetail, p.getReturnBaggageWeight()));
            } else {
                baggage.setVisibility(View.GONE);
            }

            // Hiển thị ghế
            TextView seat = v.findViewById(R.id.tv_passenger_seat);
            if ("BABY".equals(p.getType())) {
                seat.setText(R.string.label_baby_seat);
                seat.setTextColor(Color.GRAY);
            } else {
                String outSeat = p.getOutboundSeat();
                String retSeat = p.getReturnSeat();
                boolean hasOutSeat = outSeat != null && !outSeat.trim().isEmpty();
                boolean hasRetSeat = retSeat != null && !retSeat.trim().isEmpty();

                if (hasOutSeat || hasRetSeat) {
                    if (hasOutSeat && hasRetSeat) {
                        seat.setText(getString(R.string.label_seat_depart_return, outSeat, retSeat));
                    } else {
                        seat.setText(hasOutSeat ? outSeat : retSeat);
                    }
                    seat.setTextColor(Color.parseColor("#1565C0"));
                } else {
                    seat.setText("");
                }
            }
            layoutPassengerList.addView(v);
        }
    }

    private void populateBottomBar(Booking b) {
        String s = b.getStatus() != null ? b.getStatus() : "";
        layoutBottomFailed.setVisibility("RESERVATION_FAILED".equals(s) ? View.VISIBLE : View.GONE);
        layoutBottomPayment.setVisibility("RESERVATION_SUCCESS".equals(s) ? View.VISIBLE : View.GONE);
        layoutBottomPaymentExpire.setVisibility("PAYMENT_EXPIRED".equals(s) ? View.VISIBLE : View.GONE);
    }

    private String statusLabel(String s) {
        switch (s != null ? s : "") {
            case "RESERVATION_SUCCESS":
                return "Đợi thanh toán";
            case "PAYMENT_SUCCESS":
                return "Đã thanh toán";
            case "PAYMENT_EXPIRED":
                return "Hết hạn thanh toán";
            default:
                return "Giữ chỗ không thành công";
        }
    }

    private String statusColor(String s) {
        switch (s != null ? s : "") {
            case "RESERVATION_SUCCESS":
                return "#F59E0B";
            case "PAYMENT_SUCCESS":
                return "#16A34A";
            case "PAYMENT_EXPIRED":
                return "#6B7280";
            default:
                return "#DC2626";
        }
    }

    private void setupClickListeners() {
        // toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Đặt lại
        btnRebookOrder.setOnClickListener(v -> {
            finish();
        });

        View.OnClickListener showSupportDialog = v -> {
            Booking current = viewModel.getBooking().getValue();
            String name = current != null ? current.getContactName() : "";
            String status = current != null ? statusLabel(current.getStatus()) : "";
            String msg = "Chào " + name + " 👋\n"
                    + "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.\n\n"
                    + "Trạng thái đặt chỗ của bạn là: " + status + "\n\n"
                    + "Vui lòng nhấn GỌI ĐIỆN THOẠI hoặc GỬI SMS để được hỗ trợ nhanh nhất.\n\n"
                    + "Xin cảm ơn !";
            ContactSupportDialog.newInstance(msg)
                    .show(getSupportFragmentManager(), "contact_support");
        };

        btnContactSupport.setOnClickListener(showSupportDialog);

        btnPayContactExpire.setOnClickListener(showSupportDialog);

        btnPayNow.setOnClickListener(v -> {
            CreateOrder orderApi = new CreateOrder();
            String rawTotal = tvGrandTotal.getText().toString();

            // 2. Dùng Regex loại bỏ sạch sành sanh những gì KHÔNG PHẢI LÀ SỐ [^0-9]
            // Kết quả thu được sẽ là chuỗi thuần số: "1000000"
            String totalString = rawTotal.replaceAll("[^0-9]", "");

            try {
                // Lấy tổng tiền thanh toán
                JSONObject data = orderApi.createOrder(totalString);
                String code = data.getString("return_code");

                if (code.equals("1")) {
                    // khi ấn thanh toán sẽ tạo ra chuỗi token để thanh toán
                    String token = data.getString("zp_trans_token");
                    ZaloPaySDK.getInstance().payOrder(OrderDetailActivity.this, token, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String s, String s1, String s2) {
                            Booking current = viewModel.getBooking().getValue();
                            if (current != null) {
                                viewModel.confirmPaymentSuccess(current);
                            }
                            Intent intent = new Intent(OrderDetailActivity.this,
                                    PaymentNotificationActivity.class);
                            intent.putExtra("result", "Success Payment");
                            intent.putExtra("booking_id", current != null ? current.getBookingId() : "");
                            if (current != null) {
                                intent.putExtra("amount", PriceFormatter.formatPrice(current.getTotalAmount()));
                                intent.putExtra("order_code", current.getOrderCode());
                            }
                            startActivity(intent);
                        }

                        @Override
                        public void onPaymentCanceled(String s, String s1) {
                            Booking current = viewModel.getBooking().getValue();
                            Intent intent = new Intent(OrderDetailActivity.this, PaymentNotificationActivity.class);
                            intent.putExtra("result", "Cancel Payment");
                            intent.putExtra("booking_id", current != null ? current.getBookingId() : "");
                            if (current != null) {
                                intent.putExtra("amount", PriceFormatter.formatPrice(current.getTotalAmount()));
                                intent.putExtra("order_code", current.getOrderCode());
                            }
                            startActivity(intent);
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                            Booking current = viewModel.getBooking().getValue();
                            Intent intent = new Intent(OrderDetailActivity.this, PaymentNotificationActivity.class);
                            intent.putExtra("result", "Error Payment");
                            intent.putExtra("booking_id", current != null ? current.getBookingId() : "");
                            if (current != null) {
                                intent.putExtra("amount", PriceFormatter.formatPrice(current.getTotalAmount()));
                                intent.putExtra("order_code", current.getOrderCode());
                            }
                            startActivity(intent);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
    // màn hình thanh toán zalopay
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }



}
