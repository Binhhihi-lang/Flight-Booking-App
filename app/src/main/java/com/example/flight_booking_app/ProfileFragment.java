package com.example.flight_booking_app;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.flight_booking_app.Activities.UserEditProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ProfileFragment extends Fragment {

    ImageView btnUpdate;
    MaterialToolbar toolbar ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnUpdate = view.findViewById(R.id.btnEditProfile);

        btnUpdate.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), UserEditProfileActivity.class);
            startActivity(intent);

        });


        toolbar = view.findViewById(R.id.toolbarInfoProfile);

        toolbar.setNavigationOnClickListener(view1 ->{
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_home);
        });


        return view;

    }
}