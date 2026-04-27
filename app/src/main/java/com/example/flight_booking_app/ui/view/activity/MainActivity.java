package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.flight_booking_app.ui.view.fragment.BookingFragment;
import com.example.flight_booking_app.ui.view.fragment.HomeFragment;
import com.example.flight_booking_app.ui.view.fragment.InboxFragment;
import com.example.flight_booking_app.ui.view.fragment.OfferFragment;
import com.example.flight_booking_app.ui.view.fragment.ProfileFragment;
import com.example.flight_booking_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ XML
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load trang mặc định khi vừa mở App
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
        }


        // Bắt sự kiện khi click vào các nút trên thanh Nav
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Lấy ID của item người dùng vừa bấm (ID này nằm trong file bottom_nav_menu.xml)
            int itemId = item.getItemId();

            //
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_booking) {
                selectedFragment = new BookingFragment();
            } else if (itemId == R.id.nav_offer) {
                selectedFragment = new OfferFragment();
            }
            else if (itemId == R.id.nav_inbox) {
                selectedFragment = new InboxFragment();
            }
            else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            // Thực hiện việc tráo đổi màn hình
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();

                return true;
            }

            return false;
        });
    }
}