package com.example.flight_booking_app.ui.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.FareRule;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.ui.viewmodel.BookingInfoViewModel;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.example.flight_booking_app.utils.PriceFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;


public class BookingInfoActivity extends AppCompatActivity {

    // ── Chuyến đi ─────────────────────────────────────────────────────────
    private Flight outboundFlight;
    private Flight returnFlight;
    private FareClass outboundFare;
    private FareClass returnFare;


    // ── Flags ─────────────────────────────────────────────────────────────
    public static final String EXTRA_IS_ROUND_TRIP = "is_round_trip";


    private MaterialToolbar toolbar;

    private TextView tvOutRoute, tvOutDate;
    private TextView tvOutDepartTime, tvOutFromIata, tvOutDuration, tvOutArrivalTime, tvOutToIata;
    private TextView tvOutFlightNumber, tvOutFareClass;
    private ImageView imgOutLogo;

    private CardView cardReturnFlight;
    private CardView btnSelectSeat;
    private TextView tvRetRoute, tvRetDate;
    private TextView tvRetDepartTime, tvRetFromIata, tvRetDuration, tvRetArrivalTime, tvRetToIata;
    private TextView tvRetFlightNumber, tvRetFareClass;
    private ImageView imgRetLogo;

    // Passenger
    private LinearLayout layoutPassengerList;
    private TextView tvFareTotalPrice, tvSubtotalPrice, tvGrandTotalPrice;

    private TextInputEditText edtContactName, edtContactEmail, edtContactPhone;
    private MaterialButton btnBookNow;

    private BookingInfoViewModel bookingInfoViewModel;
    private UserViewModel userViewModel;
    private ProgressDialog progressDialog;

    private boolean isRoundTrip;

    private int adultCount, childCount, babyCount;

    // Dữ liệu truyền sang SeatSelectionActivity
    private String outboundFlightId, outSeatMapId, outAircraftName, outAirlineName, outFromIata, outToIata;
    private String returnFlightId, retSeatMapId, retAircraftName, retAirlineName, retFromIata, retToIata;

    private LinearLayout rowOutAdultPrice, rowOutChildPrice, rowOutBabyPrice;
    private TextView tvOutAdultCountAndPrice, tvOutChildCountAndPrice, tvOutBabyCountAndPrice, tvTotalOutboundPrice;

    private LinearLayout rowRetAdultPrice, rowRetChildPrice, rowRetBabyPrice;
    private TextView tvRetAdultCountAndPrice, tvRetChildCountAndPrice, tvRetBabyCountAndPrice, tvTotalReturnPrice;

    private ArrayList<String> depSeats;

    private ArrayList<String> retSeats;

    // Tổng tiền vé
    double currentTotalOutbound, currentTotalReturn;

    // hành khách
    private static final int ICON_ADULT = R.drawable.ic_nav_profile;
    private static final int ICON_CHILD = R.drawable.ic_child;
    private static final int ICON_BABY = R.drawable.ic_baby;
    private static final int ICON_CHECKED = R.drawable.ic_checked;

    private static final int COLOR_COMPLETE = Color.parseColor("#0175F3");

    private final ActivityResultLauncher<Intent> seatSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    depSeats = data.getStringArrayListExtra("selected_seats_depart");
                    retSeats = data.getStringArrayListExtra("selected_seats_return");

                    // Lấy giá tiền chính xác bằng getSerializableExtra
                    ArrayList<Double> depPrices = (ArrayList<Double>) data.getSerializableExtra("selected_prices_depart");
                    ArrayList<Double> retPrices = (ArrayList<Double>) data.getSerializableExtra("selected_prices_return");

                    // Cập nhật hàm updateSeats trong ViewModel để nhận thêm giá
                    bookingInfoViewModel.updateSeats(depSeats, depPrices, retSeats, retPrices, isRoundTrip);
                }
            }
    );

    private final ActivityResultLauncher<Intent> passengerInputLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Passenger updatedPassenger = (Passenger) result.getData().getSerializableExtra("updated_passenger");
                    // Nhận outboundFare và returnFare nếu có khứ hồi
                    if (updatedPassenger != null) {
                        // bookingInfoViewModel cập nhật
                        bookingInfoViewModel.updatePassenger(updatedPassenger);
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        renderFlightInfo();
        setupViewModel();
        setupClickListeners();

        setupRealtimeValidation();
    }


    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_booking);

        tvOutRoute = findViewById(R.id.tv_outbound_route);
        tvOutDate = findViewById(R.id.tv_outbound_date);
        tvOutDepartTime = findViewById(R.id.tv_outbound_depart_time);
        tvOutFromIata = findViewById(R.id.tv_outbound_from_iata);
        tvOutDuration = findViewById(R.id.tv_outbound_duration);
        tvOutArrivalTime = findViewById(R.id.tv_outbound_arrival_time);
        tvOutToIata = findViewById(R.id.tv_outbound_to_iata);
        tvOutFlightNumber = findViewById(R.id.tv_outbound_flight_number);
        imgOutLogo = findViewById(R.id.img_outbound_airline_logo);
        tvOutFareClass = findViewById(R.id.tv_outbound_fare_class);

        cardReturnFlight = findViewById(R.id.card_return_flight);
        tvRetRoute = findViewById(R.id.tv_return_route);
        tvRetDate = findViewById(R.id.tv_return_date);
        tvRetDepartTime = findViewById(R.id.tv_return_depart_time);
        tvRetFromIata = findViewById(R.id.tv_return_from_iata);
        tvRetDuration = findViewById(R.id.tv_return_duration);
        tvRetArrivalTime = findViewById(R.id.tv_return_arrival_time);
        tvRetToIata = findViewById(R.id.tv_return_to_iata);
        tvRetFlightNumber = findViewById(R.id.tv_return_flight_number);
        imgRetLogo = findViewById(R.id.img_return_airline_logo);
        tvRetFareClass = findViewById(R.id.tv_return_fare_class);

        rowOutAdultPrice = findViewById(R.id.row_outbound_adult_price);
        rowOutChildPrice = findViewById(R.id.row_outbound_child_price);
        rowOutBabyPrice = findViewById(R.id.row_outbound_baby_price);

        tvOutAdultCountAndPrice = findViewById(R.id.tv_outbound_adult_price);
        tvOutChildCountAndPrice = findViewById(R.id.tv_outbound_child_price);
        tvOutBabyCountAndPrice = findViewById(R.id.tv_outbound_baby_price);
        tvTotalOutboundPrice = findViewById(R.id.tv_outbound_total_price);

        // tổng tiền lượt về
        rowRetAdultPrice = findViewById(R.id.row_return_adult_price);
        rowRetChildPrice = findViewById(R.id.row_return_child_price);
        rowRetBabyPrice = findViewById(R.id.row_return_baby_price);


        tvRetAdultCountAndPrice = findViewById(R.id.tv_return_adult_price);
        tvRetChildCountAndPrice = findViewById(R.id.tv_return_child_price);
        tvRetBabyCountAndPrice = findViewById(R.id.tv_return_baby_price);
        tvTotalReturnPrice = findViewById(R.id.tv_return_total_price);

        btnSelectSeat = findViewById(R.id.card_select_seat);
        layoutPassengerList = findViewById(R.id.layout_passenger_list);

        // thông tin liên hệ
        edtContactName = findViewById(R.id.et_contact_name);
        edtContactEmail = findViewById(R.id.et_contact_email);
        edtContactPhone = findViewById(R.id.et_contact_phone);

        // tổng tiền
        tvFareTotalPrice = findViewById(R.id.tv_fare_price_total);
        tvSubtotalPrice = findViewById(R.id.tv_subtotal_price);
        tvGrandTotalPrice = findViewById(R.id.tv_grand_total_price);

        btnBookNow = findViewById(R.id.btn_book_now);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang giữ chỗ đặt vé... Vui lòng đợi trong giây lát!");
        progressDialog.setCancelable(false);
    }


    private void renderFlightInfo() {
        Intent intent = getIntent();

        isRoundTrip = intent.getBooleanExtra("is_round_trip", false);
        adultCount = intent.getIntExtra("adult_count", 1);
        childCount = intent.getIntExtra("child_count", 0);
        babyCount = intent.getIntExtra("baby_count", 0);

        outboundFlight = (Flight) intent.getSerializableExtra("selected_outbound_flight");
        outboundFare = (FareClass) intent.getSerializableExtra("selected_outbound_fare");

        if (isRoundTrip) {
            returnFlight = (Flight) intent.getSerializableExtra("selected_return_flight");
            returnFare = (FareClass) intent.getSerializableExtra("selected_return_fare");
        }

        // Kiểm tra an toàn dữ liệu trước khi render
        if (outboundFlight == null || outboundFare == null) return;

        // tính tổng tiền từng khách hàng
        calculateOutboundPrice();

        outboundFlightId = outboundFlight.getFlightId();
        outSeatMapId = outboundFlight.getSeatMapId();
        outAircraftName = outboundFlight.getAirCraftName();
        outAirlineName = outboundFlight.getAirlineName();
        outFromIata = outboundFlight.getFromIata();
        outToIata = outboundFlight.getToIata();

        // CHUYẾN ĐI
        tvOutRoute.setText(outboundFlight.getFrom() + " -> " + outboundFlight.getTo());
        tvOutDate.setText(PriceFormatter.formatDateOnly(outboundFlight.getDepartureTime()));
        tvOutDepartTime.setText(PriceFormatter.formatTimeOnly(outboundFlight.getDepartureTime()));
        tvOutFromIata.setText(outFromIata);
        tvOutDuration.setText(outboundFlight.getDuration());
        tvOutArrivalTime.setText(PriceFormatter.formatTimeOnly(outboundFlight.getArrivalTime()));
        tvOutToIata.setText(outToIata);
        tvOutFlightNumber.setText(outboundFlight.getFlightNumber());
        tvOutFareClass.setText(outboundFare.getTitle()); // Tên hạng vé

        Glide.with(this)
                .load(outboundFlight.getAirlineLogo())
                .placeholder(R.drawable.ic_airline)
                .into(imgOutLogo);

        // Chỉ khi khứ hồi
        if (isRoundTrip && returnFlight != null && returnFare != null) {
            cardReturnFlight.setVisibility(View.VISIBLE);

            calculateReturnPrice();
            returnFlightId = returnFlight.getFlightId();
            retSeatMapId = returnFlight.getSeatMapId();
            retAircraftName = returnFlight.getAirCraftName();
            retAirlineName = returnFlight.getAirlineName();
            retFromIata = returnFlight.getFromIata();
            retToIata = returnFlight.getToIata();

            // Render UI
            tvRetRoute.setText(returnFlight.getFrom() + " -> " + returnFlight.getTo());
            tvRetDate.setText(PriceFormatter.formatDateOnly(returnFlight.getDepartureTime()));
            tvRetDepartTime.setText(PriceFormatter.formatTimeOnly(returnFlight.getDepartureTime()));
            tvRetFromIata.setText(retFromIata);
            tvRetDuration.setText(returnFlight.getDuration());
            tvRetArrivalTime.setText(PriceFormatter.formatTimeOnly(returnFlight.getArrivalTime()));
            tvRetToIata.setText(retToIata);
            tvRetFlightNumber.setText(returnFlight.getFlightNumber());
            tvRetFareClass.setText(returnFare.getTitle());

            Glide.with(this)
                    .load(returnFlight.getAirlineLogo())
                    .placeholder(R.drawable.ic_airline)
                    .into(imgRetLogo);
        } else {
            cardReturnFlight.setVisibility(View.GONE);
        }
    }

    private void calculateOutboundPrice() {
        // 1. Lấy giá và thuế từ Object
        double basePrice = outboundFare.getBasePrice();
        double taxFee = outboundFlight.getTaxFee();

        // 2. Tính giá đơn vị (Người lớn 100%, Trẻ em 75%, Em bé 10%)
        double adultSinglePrice = basePrice + taxFee;
        double childSinglePrice = adultSinglePrice * 0.75;
        double babySinglePrice = adultSinglePrice * 0.10;

        // 3. Hiển thị UI bằng cách gọi lớp Utils
        // --- NGƯỜI LỚN ---
        if (adultCount > 0) {
            rowOutAdultPrice.setVisibility(View.VISIBLE);
            // Gọi Utils: Kết quả trả về dạng "2 x 1.500.000 đ"
            tvOutAdultCountAndPrice.setText(PriceFormatter.formatCountAndPrice(adultCount, adultSinglePrice));
        } else {
            rowOutAdultPrice.setVisibility(View.GONE);
        }

        // --- TRẺ EM ---
        if (childCount > 0) {
            rowOutChildPrice.setVisibility(View.VISIBLE);
            tvOutChildCountAndPrice.setText(PriceFormatter.formatCountAndPrice(childCount, childSinglePrice));
        } else {
            rowOutChildPrice.setVisibility(View.GONE);
        }

        // --- EM BÉ ---
        if (babyCount > 0) {
            rowOutBabyPrice.setVisibility(View.VISIBLE);
            tvOutBabyCountAndPrice.setText(PriceFormatter.formatCountAndPrice(babyCount, babySinglePrice));
        } else {
            rowOutBabyPrice.setVisibility(View.GONE);
        }

        // 4. Tính tổng tiền Lượt đi
        currentTotalOutbound = (adultSinglePrice * adultCount)
                + (childSinglePrice * childCount)
                + (babySinglePrice * babyCount);

        tvTotalOutboundPrice.setText(PriceFormatter.formatPrice(currentTotalOutbound));
    }

    private void calculateReturnPrice() {
        // 1. Lấy giá và thuế từ Object chiều về
        double basePrice = returnFare.getBasePrice();
        double taxFee = returnFlight.getTaxFee();

        // 2. Tính giá đơn vị chiều về (Người lớn 100%, Trẻ em 75%, Em bé 10%)
        double adultSinglePrice = basePrice + taxFee;
        double childSinglePrice = adultSinglePrice * 0.75;
        double babySinglePrice = adultSinglePrice * 0.10;


        // 3. Hiển thị UI bằng cách gọi PriceFormatter từ thư mục utils
        // --- NGƯỜI LỚN ---
        if (adultCount > 0) {
            rowRetAdultPrice.setVisibility(View.VISIBLE);
            // Trả về chuỗi dạng: "2 x 1.500.000 đ"
            tvRetAdultCountAndPrice.setText(PriceFormatter.formatCountAndPrice(adultCount, adultSinglePrice));
        } else {
            rowRetAdultPrice.setVisibility(View.GONE);
        }

        // --- TRÈ EM ---
        if (childCount > 0) {
            rowRetChildPrice.setVisibility(View.VISIBLE);
            // Trả về chuỗi dạng: "1 x 1.125.000 đ"
            tvRetChildCountAndPrice.setText(PriceFormatter.formatCountAndPrice(childCount, childSinglePrice));
        } else {
            rowRetChildPrice.setVisibility(View.GONE);
        }

        // --- EM BÉ ---
        if (babyCount > 0) {
            rowRetBabyPrice.setVisibility(View.VISIBLE);
            // Trả về chuỗi dạng: "1 x 150.000 đ"
            tvRetBabyCountAndPrice.setText(PriceFormatter.formatCountAndPrice(babyCount, babySinglePrice));
        } else {
            rowRetBabyPrice.setVisibility(View.GONE);
        }

        // 4. Tính tổng tiền Lượt về (Chưa cộng tiền hành lý mua thêm)
        currentTotalReturn = (adultSinglePrice * adultCount)
                + (childSinglePrice * childCount)
                + (babySinglePrice * babyCount);

        // Trả về chuỗi dạng: "4.275.000 đ"
        tvTotalReturnPrice.setText(PriceFormatter.formatPrice(currentTotalReturn));
    }

    private void setupViewModel() {
        bookingInfoViewModel = new ViewModelProvider(this).get(BookingInfoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // khởi tạo lần đầu danh sách hành khách động (trống thông tin)
        bookingInfoViewModel.initPassengersIfNeeded(adultCount, childCount, babyCount);

        // quan sát hiển thị thông tin liên hệ
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user == null) return;

            edtContactName.setText(user.getFullName());
            edtContactEmail.setText(user.getEmail());
            edtContactPhone.setText(user.getPhoneNumber());
        });
        bookingInfoViewModel.getUiState().observe(this, result -> {
            if (result == null) return;
            switch (result.getStatus()) {
                case LOADING:
                    btnBookNow.setEnabled(false);
                    break;

                case SUCCESS:
                    progressDialog.dismiss();
                    Toast.makeText(this, "Đã giữ chỗ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay về ProfileFragment
                    break;

                case ERROR:
                    progressDialog.dismiss();
                    btnBookNow.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });


        // quan sát thông tin hành khách
        bookingInfoViewModel.getPassengerListLive().observe(this, list -> {
            if (list != null) {
                //
                layoutPassengerList.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);

                for (Passenger p : list) {
                    View container = inflater.inflate(R.layout.item_passenger_input, layoutPassengerList, false);

                    // Xác định icon đại diện theo loại khách
                    ImageView imgIcon = container.findViewById(R.id.img_passenger_icon);
                    int iconRes = p.getType().equals("ADULT") ? ICON_ADULT :
                            p.getType().equals("CHILD") ? ICON_CHILD : ICON_BABY;
                    imgIcon.setImageResource(iconRes);

                    // Xử lý Trạng thái Check hoàn thành nhập liệu
                    TextView tvMandatory = container.findViewById(R.id.tv_passenger_mandatory);
                    ImageView imgCheck = container.findViewById(R.id.img_passenger_check);

                    // hành lý ký gửi
                    LinearLayout rowInfoAdd = container.findViewById(R.id.row_InfoAdd_container);
                    TextView tvBaggageAdd = container.findViewById(R.id.tv_baggage_add);
                    TextView tvDobAdd = container.findViewById(R.id.tv_dob_add);
                    TextView tvIdNumberAdd = container.findViewById(R.id.tv_idNumber_add);

                    // Kiểm tra hoàn thành
                    boolean hasId = p.getIdNumber() != null && !p.getIdNumber().isEmpty();
                    boolean hasDob = p.getDateOfBirth() != null && !p.getDateOfBirth().isEmpty();
                    boolean hasTitle = p.getTitle() != null && !p.getTitle().isEmpty();
                    boolean hasOutBaggage = p.getOutboundBaggageId() != null && !p.getOutboundBaggageId().isEmpty();
                    boolean hasRetBaggage = p.getReturnBaggageId() != null && !p.getReturnBaggageId().isEmpty();

                    if (p.isComplete()) {
                        imgCheck.setImageResource(ICON_CHECKED);
                        imgCheck.setColorFilter(COLOR_COMPLETE, PorterDuff.Mode.SRC_IN);
                        tvMandatory.setVisibility(View.GONE);
                        imgCheck.setVisibility(View.VISIBLE);

                        // Ẩn rowInfoAdd nếu tất cả đều trống
                        if (!hasId && !hasDob && !hasTitle && !hasOutBaggage && !hasRetBaggage) {
                            rowInfoAdd.setVisibility(View.GONE);
                        } else {
                            rowInfoAdd.setVisibility(View.VISIBLE);
                        }
                    }

                    // Hiển thị Nhãn và Tên
                    TextView tvLabel = container.findViewById(R.id.tv_passenger_label);
                    if (p.getFullName() != null && !p.getFullName().trim().isEmpty()) {
                        tvLabel.setText(p.getTitle() + ": " + p.getFullName().toUpperCase());
                    } else {
                        tvLabel.setText(p.getLabel());
                    }

                    // ngày sinh
                    if (hasDob) {
                        tvDobAdd.setVisibility(View.VISIBLE);
                        tvDobAdd.setText("- Ngày sinh: " + p.getDateOfBirth());
                    } else {
                        tvDobAdd.setVisibility(View.GONE);
                    }

                    // số giấy tờ
                    if (hasId) {
                        tvIdNumberAdd.setVisibility(View.VISIBLE);
                        tvIdNumberAdd.setText("- Số giấy tờ: " + p.getIdNumber());
                    } else {
                        tvIdNumberAdd.setVisibility(View.GONE);
                    }

                    // Hiển thị hành lý (Kiểm tra cả hai trước)
                    if (hasOutBaggage && hasRetBaggage) {
                        tvBaggageAdd.setVisibility(View.VISIBLE);
                        tvBaggageAdd.setText("- Hành lý lượt đi " + p.getOutboundBaggageWeight() + "kg & lượt về " + p.getReturnBaggageWeight() + "kg");
                    } else if (hasOutBaggage) {
                        tvBaggageAdd.setVisibility(View.VISIBLE);
                        tvBaggageAdd.setText("- Hành lý lượt đi " + p.getOutboundBaggageWeight() + "kg");
                    } else if (hasRetBaggage) {
                        tvBaggageAdd.setVisibility(View.VISIBLE);
                        tvBaggageAdd.setText("- Hành lý lượt về " + p.getReturnBaggageWeight() + "kg");
                    } else {
                        tvBaggageAdd.setVisibility(View.GONE);
                    }

                    // Hiển thị Ghế
                    TextView tvSeat = container.findViewById(R.id.tv_passenger_seat);
                    if (p.getSeatNumber() != null && !p.getSeatNumber().isEmpty()) {
                        tvSeat.setText(p.getSeatNumber());
                        tvSeat.setTextColor(p.getType().equals("BABY") ? Color.GRAY : Color.parseColor("#1565C0"));
                    }
                    container.setOnClickListener(v -> openPassengerInput(p));
                    layoutPassengerList.addView(container);
                }
            }
        });

        bookingInfoViewModel.updateSubTotalPrice();

        // quan sát tiền cộng
        bookingInfoViewModel.getSubTotalPriceLive().observe(this, price -> {
            if (price != null) {
                tvFareTotalPrice.setText(PriceFormatter.formatPrice(currentTotalOutbound + currentTotalReturn));

                tvSubtotalPrice.setText(PriceFormatter.formatPrice(price));

                tvGrandTotalPrice.setText(PriceFormatter.formatPrice(price + currentTotalOutbound + currentTotalReturn));
            }
        });

        // Lấy data user lần đầu để điền vào form
        userViewModel.loadUserOnce();
    }

    private void openPassengerInput(Passenger passenger) {
        Intent intent = new Intent(this, PassengerInputActivity.class);
        intent.putExtra("passenger", passenger);
        intent.putExtra("is_round_trip", isRoundTrip);

        if (outboundFare != null && outboundFare.getBaggageOptions() != null) {
            intent.putExtra("outbound_baggage_options",
                    new ArrayList<>(outboundFare.getBaggageOptions()));
        }

        if (isRoundTrip && returnFare != null && returnFare.getBaggageOptions() != null) {
            intent.putExtra("return_baggage_options",
                    new ArrayList<>(returnFare.getBaggageOptions()));
        }

        passengerInputLauncher.launch(intent);
    }


    private void setupClickListeners() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSelectSeat.setOnClickListener(v -> openSeatSelection());

        btnBookNow.setOnClickListener(v -> {
            String fullName = edtContactName.getText().toString().trim();
            String email = edtContactEmail.getText().toString().trim();
            String phone = edtContactPhone.getText().toString().trim();

            if (fullName.isEmpty()) {
                edtContactName.setError("Vui lòng nhập họ và tên");
                edtContactName.requestFocus();
            } else if (email.isEmpty()) {
                edtContactEmail.setError("Vui lòng nhập email liên hệ");
                edtContactEmail.requestFocus();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtContactEmail.setError("Định dạng email không hợp lệ");
                edtContactEmail.requestFocus();
            } else if (phone.isEmpty()) {
                edtContactPhone.setError("Vui lòng nhập số điện thoại");
                edtContactPhone.requestFocus();
            } else if (!phone.startsWith("0") || phone.length() != 10) {
                edtContactPhone.setError("Số điện thoại phải bắt đầu bằng 0 và đủ 10 số");
                edtContactPhone.requestFocus();
            } else {
                boolean isValid = bookingInfoViewModel.validateInfo(fullName, email, phone);
                if (isValid) {
                    // TODO: Gửi thông tin lên server
                    Intent intent = new Intent(this,OrderDetailActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    // Seat selection
    private void openSeatSelection() {
        int seatsNeeded = adultCount + childCount;

        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_MAX_PASSENGERS, seatsNeeded);
        intent.putExtra(SeatSelectionActivity.EXTRA_IS_ROUND_TRIP, isRoundTrip);

        FareRule outRule = outboundFare.getFareRule();
        if (outRule != null) {
            intent.putExtra(SeatSelectionActivity.EXTRA_OUT_FARE_CLASS, outRule.getFareClassName());
            if (outRule.getFreeIncludedSeatTypes() != null) {
                intent.putStringArrayListExtra(SeatSelectionActivity.EXTRA_OUT_FREE_SEATS, new ArrayList<>(outRule.getFreeIncludedSeatTypes()));
            }
        }

        if (isRoundTrip) {
            FareRule retRule = returnFare.getFareRule();
            if (retRule != null) {
                intent.putExtra(SeatSelectionActivity.EXTRA_RET_FARE_CLASS, retRule.getFareClassName());
                if (retRule.getFreeIncludedSeatTypes() != null) {
                    intent.putStringArrayListExtra(SeatSelectionActivity.EXTRA_RET_FREE_SEATS, new ArrayList<>(retRule.getFreeIncludedSeatTypes()));
                }
            }
        }

        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEATS_SELECTED, depSeats);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_FLIGHT_ID, outboundFlightId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_MAP_ID, outSeatMapId);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRCRAFT_NAME, outAircraftName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_AIRLINE_NAME, outAirlineName);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_FROM_IATA, outFromIata);
        intent.putExtra(SeatSelectionActivity.EXTRA_OUT_SEAT_TO_IATA, outToIata);

        if (isRoundTrip) {
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEATS_SELECTED, retSeats);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_FLIGHT_ID, returnFlightId);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_MAP_ID, retSeatMapId);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_AIRCRAFT_NAME, retAircraftName);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_AIRLINE_NAME, retAirlineName);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_FROM_IATA, retFromIata);
            intent.putExtra(SeatSelectionActivity.EXTRA_RET_SEAT_TO_IATA, retToIata);
        }

        seatSelectionLauncher.launch(intent);
    }

    private void setupRealtimeValidation() {
        edtContactName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                if (name.isEmpty()) edtContactName.setError("Họ tên không được để trống");
                else edtContactName.setError(null);
            }
        });

        edtContactPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                if (phone.isEmpty()) edtContactPhone.setError("Số điện thoại không được để trống");
                else if (!phone.startsWith("0")) edtContactPhone.setError("Số điện thoại phải bắt đầu bằng 0");
                else if (phone.length() != 10) edtContactPhone.setError("Số điện thoại không hợp lệ");
                else edtContactPhone.setError(null);
            }
        });

        edtContactEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (email.isEmpty()) edtContactEmail.setError("Email không được để trống");
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) edtContactEmail.setError("Email không đúng định dạng hợp lệ");
                else edtContactEmail.setError(null);
            }
        });
    }
}
