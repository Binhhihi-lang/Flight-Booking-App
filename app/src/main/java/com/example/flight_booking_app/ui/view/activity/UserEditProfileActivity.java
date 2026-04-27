package com.example.flight_booking_app.ui.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ui.viewmodel.UserViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserEditProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText edtFullName, edtEmail, edtPhone, edtGender, edtDob, edtCitizenCard;
    private ImageView imgAvatar, imgSmallAvatar, imgChangeAvatar;
    private Button btnSaveChanges;

    private UserViewModel userViewModel;

    private ProgressDialog progressDialog;
    private Uri selectedImageUri; // ảnh người dùng vừa chọn từ gallery (null = không đổi ảnh)

    public static final int MY_REQUEST_CODE = 10;

    // lấy ảnh đưa lên giao diện
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    // Glide cần Context
                    Glide.with(this)
                            .load(selectedImageUri)
                            .override(300, 300)
                            .into(imgAvatar);
                    Glide.with(this)
                            .load(selectedImageUri)
                            .override(100, 100)
                            .into(imgSmallAvatar);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_edit_profile);

        bindViews();
        initCloudinary();
        setupViewModel(); // khởi tạo ViewModel
        setupClickListeners();

        // Lấy data user lần đầu để điền vào form
        userViewModel.loadUserOnce();
    }


    private void bindViews() {
        toolbar = findViewById(R.id.toolbarEditProfile);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtGender = findViewById(R.id.edtGender);
        edtDob = findViewById(R.id.edtDob);
        edtCitizenCard = findViewById(R.id.edtCccd);
        imgAvatar = findViewById(R.id.imgEditAvatar);
        imgSmallAvatar = findViewById(R.id.imgSmallAvatar);
        imgChangeAvatar = findViewById(R.id.imgChangeAvatar);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initCloudinary() {
        try {
            HashMap<String, String> config = new HashMap<>();
            config.put("cloud_name", "dbtbj5s8w");
            config.put("api_key", "386422764289541");
            config.put("api_secret", "mPewAja1mvna9PuuhAcrTLLeYoE");
            MediaManager.init(this, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe data user điền vào form khi lấy được data user
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user == null) return;
            edtFullName.setText(user.getFullName());
            edtEmail.setText(user.getEmail());
            edtPhone.setText(user.getPhoneNumber());
            edtGender.setText(user.getGender());
            edtDob.setText(user.getDob());
            edtCitizenCard.setText(user.getCitizenCard());

            String avatarUrl = user.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this).load(avatarUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgAvatar);
                Glide.with(this).load(avatarUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgSmallAvatar);
            }
        });

        // Observe trạng thái save (loading / success / error)
        userViewModel.getUpdateState().observe(this, result -> {
            if (result == null) return;
            switch (result.getStatus()) {
                case LOADING:
                    btnSaveChanges.setEnabled(false);
                    break;

                case SUCCESS:
                    progressDialog.dismiss();
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay về ProfileFragment
                    break;

                case ERROR:
                    progressDialog.dismiss();
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupClickListeners() {
        imgChangeAvatar.setOnClickListener(v -> requestGalleryPermission());
        btnSaveChanges.setOnClickListener(v -> handleSave());

        // Date picker
        edtDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) ->
                            edtDob.setText(day + "/" + (month + 1) + "/" + year),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Gender picker
        edtGender.setOnClickListener(v -> {
            String[] genders = {"Male", "Female", "Other"};
            new AlertDialog.Builder(this)
                    .setTitle("Chọn giới tính")
                    .setItems(genders, (dialog, which) -> edtGender.setText(genders[which]))
                    .show();
        });
    }

    private void handleSave() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String gender = edtGender.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String citizenCard = edtCitizenCard.getText().toString().trim();

        if (fullName.isEmpty()) {
            edtFullName.setError("Họ tên không được để trống");
            edtFullName.requestFocus();
            return;
        }

        progressDialog.show();
        btnSaveChanges.setEnabled(false);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phoneNumber", phone);
        updates.put("gender", gender);
        updates.put("dob", dob);
        updates.put("citizenCard", citizenCard);

        if (selectedImageUri != null) {
            // Có ảnh mới upload Cloudinary trước
            uploadImageToCloudinary(selectedImageUri, updates, fullName);
        } else {
            // Không đổi ảnh lưu Firebase ngay
            userViewModel.updateProfile(null, updates, fullName);
        }
    }

    //  lưu ảnh
    private void uploadImageToCloudinary(Uri imageUri, HashMap<String, Object> updates, String fullName) {
        MediaManager.get().upload(imageUri)
                // xử lý đầu vào ảnh ko quá 10MB
                .preprocess(ImagePreprocessChain.limitDimensionsChain(1000, 1000)
                        .addStep(new DimensionsValidator(10, 10, 1000, 1000))
                        .saveWith(new BitmapEncoder(BitmapEncoder.Format.WEBP, 80)))
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Lấy link ảnh (https) trả về từ Cloudinary
                        String imageUrl = (String) resultData.get("secure_url");

                        updates.put("avatar", imageUrl);
                        Uri photoUri = Uri.parse(imageUrl);

                        // Cloudinary callback chạy trên background thread
                        runOnUiThread(() ->
                                userViewModel.updateProfile(photoUri, updates, fullName)
                        );
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            btnSaveChanges.setEnabled(true);
                            Toast.makeText(UserEditProfileActivity.this,
                                    "Lỗi tải ảnh: " + error.getDescription(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch(this);
    }


    // Yêu cầu quyền truy cập ảnh
    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MY_REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Chọn ảnh đại diện"));
    }

    // hứng kết quả nếu người dùng ko cho phép
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Cần cấp quyền để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}