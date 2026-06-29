package com.example.flight_booking_app.ui.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.ui.view.adapter.BookingAdapter;
import com.example.flight_booking_app.ui.viewmodel.BookingViewModel;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class BookingFragment extends Fragment {

    private BookingViewModel bookingViewModel;
    private UserViewModel userViewModel;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private View layoutLoading;
    private View layoutEmpty;    // hiển thị khi chưa có đơn hàng nào
    private TextView tvEmpty;

    String currentUserId = "GUEST_USER";

    private final Handler tickHandler = new Handler(Looper.getMainLooper());
    private Runnable tickRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupRecyclerView();
        observeViewModel();
        toolbar.setNavigationOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_home);
        });
    }

    private void bindViews(View view) {
        toolbar       = view.findViewById(R.id.toolbar_booking_list);
        recyclerView = view.findViewById(R.id.rv_bookings);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty_booking);
        tvEmpty = view.findViewById(R.id.tv_empty_booking);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void observeViewModel() {
        // là this (chính là Fragment) hoặc getViewLifecycleOwner() (Vòng đời giao diện của Fragment).
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getUserId() != null && !user.getUserId().isEmpty()) {

                currentUserId = user.getUserId();

                // Khi đã chắc chắn có ID chuẩn từ DB, mới ra lệnh bắt đầu lắng nghe
                bookingViewModel.startListening(currentUserId);
            }

        });

        // Quan sát danh sách đơn hàng
        bookingViewModel.getBookingListLive().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings == null || bookings.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Bạn chưa có đơn hàng nào");
                stopCountdownTick(); // ← thêm
            } else {
                layoutEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                adapter = new BookingAdapter(bookings, requireContext());

                recyclerView.setAdapter(adapter);

                //  Chỉ chạy tick nếu có đơn đang chờ thanh toán
                boolean hasPending = false;
                for (Booking b : bookings) {
                    if ("RESERVATION_SUCCESS".equals(b.getStatus())) { hasPending = true; break; }
                }
                if (hasPending) startCountdownTick();
                else stopCountdownTick();
            }
        });
        //
        bookingViewModel.getLoadState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    //  che màn hình bằng Loading nếu list đang thực sự trống
                    if (bookingViewModel.getBookingListLive().getValue() == null) {
                        layoutLoading.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.GONE);
                    } else {
                        // Nếu đã có data (ví dụ vuốt để refresh), chỉ cần hiện loading nhỏ (nếu có)
                        layoutLoading.setVisibility(View.VISIBLE);
                    }
                    break;

                case SUCCESS:
                    layoutLoading.setVisibility(View.GONE);
                    break;

                case ERROR:
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Lỗi: " + state.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

    }

    private void startCountdownTick() {
        stopCountdownTick(); // tránh chạy đôi
        tickRunnable = new Runnable() {
            @Override
            public void run() {
                if (adapter != null) adapter.notifyDataSetChanged();
                tickHandler.postDelayed(this, 1000);
            }
        };
        tickHandler.postDelayed(tickRunnable, 1000);
    }

    private void stopCountdownTick() {
        if (tickRunnable != null) {
            tickHandler.removeCallbacks(tickRunnable);
            tickRunnable = null;
        }
    }

    // onPause / onResume để dừng tick khi không nhìn thấy
    @Override
    public void onPause() {
        super.onPause();
        stopCountdownTick();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Khởi động lại tick nếu đang có đơn pending
        List<Booking> list = bookingViewModel.getBookingListLive().getValue();
        if (list != null) {
            for (Booking b : list) {
                if ("RESERVATION_SUCCESS".equals(b.getStatus())) {
                    startCountdownTick();
                    break;
                }
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCountdownTick(); // tránh memory leak
    }
}