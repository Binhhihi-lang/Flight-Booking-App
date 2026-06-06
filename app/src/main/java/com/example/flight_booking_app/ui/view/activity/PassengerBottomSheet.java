package com.example.flight_booking_app.ui.view.activity;

import static com.example.flight_booking_app.data.model.UiState.Status.ERROR;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ui.viewmodel.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;


public class PassengerBottomSheet extends BottomSheetDialogFragment {

    TextView tvAdult, tvChild, tvBaby;
    ImageView btnMinusAdult, btnPlusAdult, btnMinusChild, btnPlusChild, btnMinusBaby, btnPlusBaby ;
    MaterialButton btnConfirm;

    private HomeViewModel homeViewModel;

    public PassengerBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_passenger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        setUpViewModel();
        setupClickListeners();

    }

    private void setUpViewModel() {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        // String.valueOf để không bị nhầm là Id của tài nguyên
        homeViewModel.getSearchState().observe(getViewLifecycleOwner(), search ->{
            tvAdult.setText(String.valueOf(search.adultCount));
            tvChild.setText(String.valueOf(search.childCount));
            tvBaby.setText(String.valueOf(search.babyCount));
        });
        homeViewModel.getValidationError().observe(getViewLifecycleOwner(), state ->{
            if (state == null) return;

            if (state.getStatus() == ERROR){
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void bindView(View view) {
        tvAdult = view.findViewById(R.id.tv_count_adult);
        tvChild = view.findViewById(R.id.tv_count_child);
        tvBaby = view.findViewById(R.id.tv_count_baby);

        btnMinusAdult = view.findViewById(R.id.btn_minus_adult);
        btnPlusAdult = view.findViewById(R.id.btn_plus_adult);
        btnMinusChild = view.findViewById(R.id.btn_minus_child);
        btnPlusChild = view.findViewById(R.id.btn_plus_child);
        btnMinusBaby = view.findViewById(R.id.btn_minus_baby);
        btnPlusBaby = view.findViewById(R.id.btn_plus_baby);
        btnConfirm = view.findViewById(R.id.btn_confirm_passenger);
    }

    private void setupClickListeners() {
        // Nút Người lớn
        btnMinusAdult.setOnClickListener(v -> homeViewModel.updatePassengers(-1, 0, 0));
        btnPlusAdult.setOnClickListener(v -> homeViewModel.updatePassengers(1, 0, 0));

        // Nút Trẻ em
        btnMinusChild.setOnClickListener(v -> homeViewModel.updatePassengers(0, -1, 0));
        btnPlusChild.setOnClickListener(v -> homeViewModel.updatePassengers(0, 1, 0));

        // Nút Em bé
        btnMinusBaby.setOnClickListener(v -> homeViewModel.updatePassengers(0, 0, -1));
        btnPlusBaby.setOnClickListener(v -> homeViewModel.updatePassengers(0, 0, 1));

        // đóng sheet
        btnConfirm.setOnClickListener(v -> dismiss());
    }


}
