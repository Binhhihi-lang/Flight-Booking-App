package com.example.flight_booking_app.ui.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.ui.view.activity.ForgotPasswordActivity;
import com.example.flight_booking_app.ui.view.activity.LoginActivity;
import com.example.flight_booking_app.ui.view.activity.UserEditProfileActivity;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvEmail, tvPhone;
    private TextView btnChangePassword, btnPolicy, btnSupport, btnLanguage;
    private ImageView imgAvatar, imgSmallAvatar;
    private MaterialToolbar toolbar;
    private MaterialButton btnLogout;
    private ImageView imgUpdateView;


    private UserViewModel userViewModel;

    // Google Sign-In
    private GoogleSignInClient gClient;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Dùng onViewCreated thay vì onCreateView để tách biệt inflate và setup.
     * Để dùng getViewLifecycleOwner() giống this
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupGoogleSignIn();
        setupViewModel();   // phải gọi trước setupClickListeners
        setupClickListeners();
    }


    private void bindViews(View view) {
        tvFullName    = view.findViewById(R.id.tvNameDisplay);
        tvEmail       = view.findViewById(R.id.tvEmailDisplay);
        tvPhone       = view.findViewById(R.id.tvPhoneDisplay);
        imgAvatar     = view.findViewById(R.id.imgAvatar);
        imgSmallAvatar = view.findViewById(R.id.imgSmallAvatar);
        toolbar       = view.findViewById(R.id.toolbarInfoProfile);
        btnLogout     = view.findViewById(R.id.btnLogout);
        imgUpdateView = view.findViewById(R.id.imgEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnPolicy = view.findViewById(R.id.btnPolicy);
        btnSupport = view.findViewById(R.id.btnSupport);
        btnLanguage = view.findViewById(R.id.btnLanguage);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        // mở hộp thoại tài khoản google
        gClient = GoogleSignIn.getClient(requireContext(), gOptions);
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe data user mỗi khi Firebase thay đổi, Fragment tự cập nhật
        // getViewLifecycleOwner() giống this
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            tvFullName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getPhoneNumber());

            String avatarUrl = user.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {

                Glide.with(requireContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgAvatar);

                Glide.with(requireContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgSmallAvatar);
            }
        });

        // Observe lỗi cập nhật người dùng
        userViewModel.getUpdateState().observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(getActivity(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Observe trạng thái logout
        userViewModel.getLogoutState().observe(getViewLifecycleOwner(), shouldLogout -> {
            if (Boolean.TRUE.equals(shouldLogout)) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                // Xóa sạch back stack không quay lại được màn hình trước
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        // lắng nghe datalistener sẽ tự được dọn trong ViewModel.onCleared()
        userViewModel.startObservingUser();
    }

    private void setupClickListeners() {
        imgUpdateView.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), UserEditProfileActivity.class)));

        // back
        toolbar.setNavigationOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_home);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(getActivity(), ForgotPasswordActivity.class)));

        btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    // đăng xuất
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // google và tk thường đăng xuất
    private void performLogout() {

        gClient.signOut().addOnCompleteListener(task -> {
            if (isAdded()) {
                Toast.makeText(getActivity(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                userViewModel.logout(); // gọi viewModel đăng xuất
            }
        });
    }
    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};

        // Lấy ngôn ngữ hiện tại của app để đánh dấu check sẵn vào Dialog cho chuyên nghiệp
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        int checkedItem = 0; // Mặc định chọn Tiếng Việt (index 0)

        // Nếu app đang cài tiếng Anh thì đổi checkItem sang 1
        if (!currentLocales.isEmpty() && currentLocales.get(0).getLanguage().equals("en")) {
            checkedItem = 1;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn ngôn ngữ / Select Language")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        setAppLanguage("vi"); // Tiếng Việt
                    } else {
                        setAppLanguage("en"); // Tiếng Anh
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setAppLanguage(String languageCode) {
        // Hàm này sẽ tự động lưu ngôn ngữ và khởi động lại Activity đang chạy để áp dụng string mới
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}