package com.example.flight_booking_app.ui.view.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.flight_booking_app.ui.view.activity.OrderDetailActivity;
import com.example.flight_booking_app.ui.view.adapter.NotificationAdapter;
import com.example.flight_booking_app.ui.viewmodel.NotificationViewModel;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NotificationFragment extends Fragment {

    private NotificationViewModel viewModel;
    private UserViewModel userViewModel;
    private MaterialToolbar toolbar;


    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private View layoutLoading;
    private View layoutEmpty;
    private TextView tvEmpty;
    private String currentUserId = "GUEST_USER"; // ID người dùng hiện tại

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
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
        toolbar       = view.findViewById(R.id.toolbar_notification_list);
        recyclerView  = view.findViewById(R.id.rv_notifications);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty   = view.findViewById(R.id.layout_empty_notification);
        tvEmpty       = view.findViewById(R.id.tv_empty_notification);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void observeViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getUserId() != null && !user.getUserId().isEmpty()) {

                // Cập nhật lại biến toàn cục để Adapter bên dưới dùng
                currentUserId = user.getUserId();

                // Khi đã chắc chắn có ID chuẩn từ DB, mới ra lệnh bắt đầu lắng nghe thông báo!
                viewModel.startListening(currentUserId);
            }
        });


        // quan sát dữ liệu thông báo
        viewModel.getNotificationListLive().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                if (tvEmpty != null) tvEmpty.setText("Bạn chưa có thông báo nào");
            } else {
                layoutEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                // Cấu hình Adapter đổ danh sách thông báo Realtime lên giao diện
                adapter = new NotificationAdapter(notifications, requireContext(),
                        (notification) -> {
                            // Xử lý sự kiện click đọc thông báo
                            if (!notification.isRead()) {
                                viewModel.markAsRead(notification.getNotificationId());
                            }

                            // Mở màn hình chi tiết đơn hàng tương ứng
                            Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
                            intent.putExtra("booking_id", notification.getBookingId());
                            startActivity(intent);
                        }
                );
                recyclerView.setAdapter(adapter);
            }
        });

        viewModel.getLoadState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    // Mẹo mượt UI: Chỉ hiện màn loading che khuất nếu danh sách đang thực sự trống rỗng
                    if (viewModel.getNotificationListLive().getValue() == null) {
                        layoutLoading.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.GONE);
                    }
                    break;

                case SUCCESS:
                    // Đã tải dữ liệu thành công
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
}