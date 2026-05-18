package com.example.flight_booking_app.ui.view.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.example.flight_booking_app.ui.view.fragment.BookingFragment;
import com.example.flight_booking_app.ui.view.fragment.HomeFragment;
import com.example.flight_booking_app.ui.view.fragment.InboxFragment;
import com.example.flight_booking_app.ui.view.fragment.OfferFragment;
import com.example.flight_booking_app.ui.view.fragment.ProfileFragment;
import com.example.flight_booking_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private final FragmentManager fm = getSupportFragmentManager();
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment bookingFragment = new BookingFragment();
    private final Fragment offerFragment = new OfferFragment();
    private final Fragment inboxFragment = new InboxFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ XML
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Sử dụng commitNow() để đảm bảo fragment được add ngay lập tức trước khi thực hiện logic khác
        fm.beginTransaction().add(R.id.fragment_container, profileFragment, "5").hide(profileFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, inboxFragment, "4").hide(inboxFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, offerFragment, "3").hide(offerFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, bookingFragment, "2").hide(bookingFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();

        // Bắt sự kiện khi click vào các nút trên thanh Nav
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_booking) {
                switchFragment(bookingFragment);
                return true;
            } else if (id == R.id.nav_inbox) {
                switchFragment(inboxFragment);
                return true;
            } else if (id == R.id.nav_offer) {
                switchFragment(offerFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;

        });

    }

    // Hàm tối ưu để chuyển đổi Fragment mà không bị đè nội dung
    private void switchFragment(Fragment targetFragment) {
        if (targetFragment == activeFragment) return; // Nếu đang ở chính nó thì không làm gì cả

        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Thêm hiệu ứng mượt mà
                .hide(activeFragment)
                .show(targetFragment)
                .commit();

        activeFragment = targetFragment;
    }

}