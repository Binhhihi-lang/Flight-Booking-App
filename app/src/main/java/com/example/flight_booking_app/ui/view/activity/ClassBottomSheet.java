package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ui.viewmodel.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class ClassBottomSheet extends BottomSheetDialogFragment {

    HomeViewModel homeViewModel;
    RadioGroup rgSeatClass;
    RadioButton rbEconomy;
    RadioButton rbBusiness;
    RadioButton rbPremium;

    public ClassBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        setUpViewModel();

        setupClickListeners();
    }

    private void setupClickListeners() {

        // lấy id từng Raido button
        View.OnClickListener listener = view -> {
            String seatClassName;
            int id = view.getId();

            if (id == R.id.rb_premium_economy) seatClassName = "Phổ thông đặc biệt";
            else if (id == R.id.rb_business) seatClassName = "Thương gia";
            else seatClassName = "Phổ thông";

            homeViewModel.setSeatClass(seatClassName);
            dismiss();
        };

        // Gán cho tất cả các nút
        rbEconomy.setOnClickListener(listener);
        rbBusiness.setOnClickListener(listener);
        rbPremium.setOnClickListener(listener);
    }

    private void setUpViewModel() {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        homeViewModel.getSearchState().observe(getViewLifecycleOwner(), state ->{
            // Check radio theo hạng ghế hiện tại
            switch (state.seatClass) {
                case "Phổ thông đặc biệt":
                    rgSeatClass.check(R.id.rb_premium_economy);
                    break;
                case "Thương gia":
                    rgSeatClass.check(R.id.rb_business);
                    break;
                default:
                    rgSeatClass.check(R.id.rb_economy);
                    break;
            }
        });
    }

    private void bindView(View view) {
        rgSeatClass = view.findViewById(R.id.rg_seat_class);
        rbEconomy = view.findViewById(R.id.rb_economy);
        rbPremium = view.findViewById(R.id.rb_premium_economy);
        rbBusiness = view.findViewById(R.id.rb_business);
    }

}
