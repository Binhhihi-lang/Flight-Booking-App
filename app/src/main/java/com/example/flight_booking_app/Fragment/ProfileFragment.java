package com.example.flight_booking_app.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.flight_booking_app.Activities.LoginActivity;
import com.example.flight_booking_app.Activities.UserEditProfileActivity;
import com.example.flight_booking_app.Models.User;
import com.example.flight_booking_app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    TextView tvFullName, tvEmail, tvPhone;
    ImageView imgAvatar, imgSmallAvatar, imgUpdateView;

    MaterialToolbar toolbar ;
    MaterialButton btnLogout;

    FirebaseAuth mAuth;
    private GoogleSignInClient gClient;
    private GoogleSignInOptions gOptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        imgUpdateView = view.findViewById(R.id.imgEditProfile);
        toolbar = view.findViewById(R.id.toolbarInfoProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvFullName = view.findViewById(R.id.tvNameDisplay);
        tvEmail = view.findViewById(R.id.tvEmailDisplay);
        tvPhone = view.findViewById(R.id.tvPhoneDisplay);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgSmallAvatar = view.findViewById(R.id.imgSmallAvatar);


        // sang trang chỉnh sửa thông tin
        imgUpdateView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserEditProfileActivity.class);
            startActivity(intent);

        });


        // trở về trang chủ
        toolbar.setNavigationOnClickListener(view1 -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_home);
        });

        // Cấu hình Google Sign-In , requireContext thay cho .this
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        gClient = GoogleSignIn.getClient(requireContext(), gOptions);

        // Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Hiển thị thông tin người dùng
        loadUserData();

        return view;

    }

    private void showLogoutDialog() {
        // Dùng getActivity() hoặc requireContext() làm context cho Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

        // Nút Đăng xuất
        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            performLogout();
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performLogout() {
        // Đăng xuất Firebase
        FirebaseAuth.getInstance().signOut();

        // Đăng xuất Google
        gClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (isAdded()) { // Kiểm tra xem Fragment còn gắn với Activity không để tránh crash
                    Toast.makeText(getActivity(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    // Xóa sạch Stack để không quay lại được trang Admin
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Fragment gọi finish thông qua Activity
                    requireActivity().finish();
                }
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        // tham chiếu User trong cddl
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        if (userRef == null) return;
        //  addValueEventListener cập nhật ngay
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User userProfile = snapshot.getValue(User.class);
                    if (userProfile != null) {
                        // Đổ dữ liệu lên TextView
                        tvFullName.setText(userProfile.getFullName());
                        tvEmail.setText(userProfile.getEmail());
                        tvPhone.setText(userProfile.getPhoneNumber());
                        // Kiểm tra và load ảnh bằng Glide
                        String avatarUrl = userProfile.getAvatar();
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_nav_profile)
                                    .error(R.drawable.ic_nav_profile)
                                    .into(imgAvatar);

                            // ảnh small
                            Glide.with(requireContext())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_nav_profile) // Ảnh hiện trong lúc chờ tải
                                    .error(R.drawable.ic_nav_profile)
                                    .into(imgSmallAvatar);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}