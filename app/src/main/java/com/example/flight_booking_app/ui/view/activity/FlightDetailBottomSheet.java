package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.flight_booking_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FlightDetailBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_flight_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        setUpViewModel();
        setupClickListeners();

    }

    private void setupClickListeners() {
    }

    private void setUpViewModel() {

    }

    private void bindView(View view) {

    }


}