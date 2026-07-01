package com.example.flight_booking_app.ui.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.City;
import com.example.flight_booking_app.data.model.SearchQueryState;
import com.example.flight_booking_app.ui.view.activity.SearchCityActivity;
import com.example.flight_booking_app.ui.view.activity.SearchFlightActivity;
import com.example.flight_booking_app.ui.view.activity.PassengerBottomSheet;
import com.example.flight_booking_app.ui.viewmodel.HomeViewModel;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.example.flight_booking_app.utils.PriceFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



public class HomeFragment extends Fragment {
    private ImageView imgAvatar;
    private CardView cardFrom, cardTo, cardDeparture, cardReturn;
    private CardView cardTraveller;
    private FloatingActionButton fabSwap;
    private Button rbOneWay, rbRoundTrip, btnSearchFlight;

    private TextView tvFromCity, tvFromCode, tvFromAirport;
    private TextView tvToCity, tvToCode, tvToAirport;
    private TextView tvDepartureDate, tvReturnDate;
    private TextView tvAdultCount, tvChildCount, tvBabyCount;


    private HomeViewModel homeViewModel;
    private UserViewModel userViewModel;

    private final ActivityResultLauncher<Intent> cityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null)
                    return;

                City city = new City(
                        result.getData().getStringExtra(SearchCityActivity.RESULT_CITY_ID),
                        result.getData().getStringExtra(SearchCityActivity.RESULT_CITY_NAME),
                        result.getData().getStringExtra(SearchCityActivity.RESULT_AIRPORT_NAME),
                        result.getData().getStringExtra(SearchCityActivity.RESULT_IATA_CODE)
                );
                String mode = result.getData().getStringExtra(SearchCityActivity.RESULT_MODE);

                // so sánh điểm điểm đi và điểm đến
                if (SearchCityActivity.MODE_FROM.equals(mode)) {
                    homeViewModel.setFromCity(city);
                } else {
                    homeViewModel.setToCity(city);
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupViewModel();
        setupClickListeners();
    }

    private void bindViews(View view) {
        cardFrom = view.findViewById(R.id.card_from);
        cardTo = view.findViewById(R.id.card_to);
        cardDeparture = view.findViewById(R.id.card_departure);
        cardReturn = view.findViewById(R.id.card_return);
        cardTraveller = view.findViewById(R.id.card_traveller);
        fabSwap = view.findViewById(R.id.fab_swap);
        rbOneWay = view.findViewById(R.id.btn_one_way);
        rbRoundTrip = view.findViewById(R.id.btn_round);
        btnSearchFlight = view.findViewById(R.id.btn_search_flights);

        tvFromCity = view.findViewById(R.id.tv_from_city);
        tvFromCode = view.findViewById(R.id.tv_from_code);
        tvFromAirport = view.findViewById(R.id.tv_from_airport);
        tvToCity = view.findViewById(R.id.tv_to_city);
        tvToCode = view.findViewById(R.id.tv_to_code);
        tvToAirport = view.findViewById(R.id.tv_to_airport);
        tvDepartureDate = view.findViewById(R.id.tv_departure_date);
        tvReturnDate = view.findViewById(R.id.tv_return_date);
        tvAdultCount = view.findViewById(R.id.tv_adult_count);
        tvChildCount = view.findViewById(R.id.tv_child_count);
        tvBabyCount = view.findViewById(R.id.tv_infant_count);

        imgAvatar = view.findViewById(R.id.imgSmallAvatar);
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        homeViewModel.getSearchState().observe(getViewLifecycleOwner(),
                search -> renderState(search));

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            String avatarUrl = user.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {

                Glide.with(requireContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgAvatar);

            }
        });
        userViewModel.startObservingUser();

        // Lỗi validate hành khách
        homeViewModel.getValidationError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && error.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Nhận SearchQueryState cập nhật toàn bộ UI.
     * Gọi lại mỗi khi bất kỳ field nào thay đổi.
     */
    private void renderState(SearchQueryState s) {
        if (s == null) return;

        // Thành phố đi
        if (s.fromCity != null) {
            if (tvFromCity != null) tvFromCity.setText(s.fromCity.getCityName());
            if (tvFromCode != null) tvFromCode.setText(s.fromCity.getIataCode());
            if (tvFromAirport != null) tvFromAirport.setText(s.fromCity.getAirportName());
        }

        // Thành phố đến
        if (s.toCity != null) {
            if (tvToCity != null) tvToCity.setText(s.toCity.getCityName());
            if (tvToCode != null) tvToCode.setText(s.toCity.getIataCode());
            if (tvToAirport != null) tvToAirport.setText(s.toCity.getAirportName());
        }

        // Ngày
        if (tvDepartureDate != null) tvDepartureDate.setText(PriceFormatter.formatDateFromMillis(s.departDateMillis));
        if (tvReturnDate != null && s.isRoundTrip) {
            tvReturnDate.setText(PriceFormatter.formatDateFromMillis(s.returnDateMillis));
        }
        else{
            tvReturnDate.setText("");
        }


        // Hành khách
        // thay vì cộng chuỗi,truyền biến trực tiếp vào hàm getString()
        if (tvAdultCount != null)
            tvAdultCount.setText(getString(R.string.passenger_adult, s.adultCount));

        if (tvChildCount != null)
            tvChildCount.setText(getString(R.string.passenger_child, s.childCount));

        if (tvBabyCount != null)
            tvBabyCount.setText(getString(R.string.passenger_infant, s.babyCount));

    }

    // ── Click listeners

    private void setupClickListeners() {

        imgAvatar.setOnClickListener(v ->{
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_profile);

        });

        // Điểm đi / điểm đến  SearchCityActivity
        cardFrom.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SearchCityActivity.class);
            intent.putExtra(SearchCityActivity.EXTRA_MODE, SearchCityActivity.MODE_FROM);
            cityLauncher.launch(intent);
        });

        cardTo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SearchCityActivity.class);
            intent.putExtra(SearchCityActivity.EXTRA_MODE, SearchCityActivity.MODE_TO);
            cityLauncher.launch(intent);
        });

        rbOneWay.setOnClickListener(v -> {
            rbRoundTrip.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            rbRoundTrip.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));

            rbOneWay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
            rbOneWay.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            cardReturn.setAlpha(0.5f);
            homeViewModel.setRoundTrip(false);
        });

        // Xử lý khi chọn "Khứ hồi" - TỰ ĐỘNG +2 NGÀY
        rbRoundTrip.setOnClickListener(v -> {
            rbOneWay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            rbOneWay.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));

            rbRoundTrip.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
            rbRoundTrip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            cardReturn.setAlpha(1.0f);
            // set trạng thái khứ hồi
            homeViewModel.setRoundTrip(true);

            // biến phải đặt trong vì khi chạy ứng dụng chạy Fagment trước
            // chạy hết các hàm rồi mới quan sát LiveData
            SearchQueryState currentFlight = homeViewModel.getSearchState().getValue();
            if (currentFlight != null) {
                // Kiểm tra biến Long xem có null không thay vì kiểm tra chuỗi rỗng
                if (currentFlight.returnDateMillis == null) {
                    long twoDaysMillis = 2 * 24 * 60 * 60 * 1000L;
                    // Lấy trực tiếp ngày đi từ State hiện tại để cộng dồn, cực kỳ an toàn
                    long suggestedReturnMillis = currentFlight.departDateMillis + twoDaysMillis;

                    // Đẩy lên ViewModel
                    homeViewModel.setReturnDate(suggestedReturnMillis);
                }
            }

        });

        // Gọi hàm chọn ngày riêng biệt
        cardDeparture.setOnClickListener(v ->  showDatePicker(true));

        cardReturn.setOnClickListener(v -> {
            rbOneWay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            rbOneWay.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));

            rbRoundTrip.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
            rbRoundTrip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            cardReturn.setAlpha(1.0f);

            // set trạng thái khứ hồi
            homeViewModel.setRoundTrip(true);

            SearchQueryState currentFlight = homeViewModel.getSearchState().getValue();
            if (currentFlight != null) {
                if (currentFlight.returnDateMillis == null) {
                    long twoDaysMillis = 2 * 24 * 60 * 60 * 1000L;
                    long suggestedReturnMillis = currentFlight.departDateMillis + twoDaysMillis;

                    homeViewModel.setReturnDate(suggestedReturnMillis);
                }
            }

            // 3. Mở Calendar để người dùng chọn ngày (false = chọn ngày về)
            showDatePicker(false);

        });

        // Hành khách gọi bottomSheet
        cardTraveller.setOnClickListener(v -> new PassengerBottomSheet().show(getChildFragmentManager(), "passenger_sheet"));


        // đổi thành phố
        fabSwap.setOnClickListener(v->{
            homeViewModel.swapCities();
            // Animation xoay FAB 180°
            if (fabSwap != null) {
                fabSwap.animate()
                        .rotationBy(180f)
                        .setDuration(300)
                        .start();
            }
        });

        btnSearchFlight.setOnClickListener(v->{
            Intent intent = new Intent(requireActivity(), SearchFlightActivity.class);
            // gọi viewModel lấy dữ liệu chuyến bay cần tìm
            homeViewModel.buildSearchIntent(intent);

            startActivity(intent);
        });

    }

    private void showDatePicker(boolean isDeparture) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(isDeparture ? "Chọn ngày đi" : "Chọn ngày về");
        // decor lịch
        builder.setTheme(R.style.MyCalendarFullscreenTheme);

        // Ràng buộc: Ngày về không được trước ngày đi
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        if (!isDeparture) {
            // Nếu chọn ngày về, chỉ cho phép chọn từ ngày đi trở đi
            constraintsBuilder.setValidator(DateValidatorPointForward.from(System.currentTimeMillis()));
        } else {
            constraintsBuilder.setValidator(DateValidatorPointForward.now());
        }
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isDeparture) {
                homeViewModel.setDepartDate(selection);

                // Lấy state hiện tại từ ViewModel để so sánh ngày về
                SearchQueryState currentState = homeViewModel.getSearchState().getValue();
                if (currentState != null && currentState.isRoundTrip && currentState.returnDateMillis != null) {
                    if (currentState.returnDateMillis < selection) {
                        // Nếu ngày về cũ trước ngày đi mới -> Tự động tăng ngày về lên +2 ngày
                        long autoReturnMillis = selection + (2 * 24 * 60 * 60 * 1000L);
                        homeViewModel.setReturnDate(autoReturnMillis);
                    }
                }
            } else {
                homeViewModel.setReturnDate(selection);
            }
        });

        picker.show(getChildFragmentManager(), "DATE_PICKER");
    }
}