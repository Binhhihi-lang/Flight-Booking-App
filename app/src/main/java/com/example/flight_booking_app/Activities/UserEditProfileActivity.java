package com.example.flight_booking_app.Activities;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.example.flight_booking_app.Models.User;
import com.example.flight_booking_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserEditProfileActivity extends AppCompatActivity {

    MaterialToolbar toolbar ;
    EditText edtFullName, etdEmail, edtPhone, edtGender, edtDob, edtCccd;
    ImageView imgAvatar, imgSmallAvatar, imgChangeAvatar;
    Button btnSaveChanges;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    ProgressDialog progressDialog;
    private Uri muri;

    public void setMuri(Uri muri) {
        this.muri = muri;
    }

    // biến đánh dấu xin quyền mở ảnh
    public static final int MY_REQUEST_CODE = 10;
    //chọn ảnh từ gallery
    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    muri = result.getData().getData();

                    Glide.with(this)
                            .load(muri)
                            .override(300, 300)
                            .into(imgAvatar);

                    Glide.with(this)
                            .load(muri)
                            .override(100, 100)
                            .into(imgSmallAvatar);
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_edit_profile);

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbarEditProfile);
        edtFullName = findViewById(R.id.edtFullName);
        etdEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtGender = findViewById(R.id.edtGender);
        edtDob = findViewById(R.id.edtDob);
        edtCccd = findViewById(R.id.edtCccd);
        imgAvatar = findViewById(R.id.imgEditAvatar);
        imgSmallAvatar = findViewById(R.id.imgSmallAvatar);
        imgChangeAvatar = findViewById(R.id.imgChangeAvatar);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressDialog = new ProgressDialog(this);

        initCloudinary();

        // nút back lại
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        // date
        edtDob.setOnClickListener(v -> {
            // Lấy ngày tháng năm hiện tại để hiển thị mặc định trên Lịch
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Bật hộp thoại Lịch
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UserEditProfileActivity.this,
                    (view, yearSelected, monthOfYear, dayOfMonth) -> {
                        // Khi người dùng chọn xong, format lại chuỗi ngày và gán vào EditText
                        // Cộng 1 vào monthOfYear vì tháng trong Java bắt đầu từ số 0
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + yearSelected;
                        edtDob.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // giới tính
        edtGender.setOnClickListener(v -> {
            // Tạo mảng các lựa chọn
            String[] genders = {"Male", "Female", "Other"};

            // Bật hộp thoại chọn
            AlertDialog.Builder builder = new AlertDialog.Builder(UserEditProfileActivity.this);
            builder.setTitle("Select Gender");
            builder.setItems(genders, (dialog, which) -> {
                // which là vị trí index mà người dùng đã click (0, 1 hoặc 2)
                edtGender.setText(genders[which]);
            });
            builder.show();
        });

        // lấy thông tin người dùng show lên
        setUserInfomation();

        initListener();
    }

    // khởi tạo Cloudinary
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

    // hiển thị thông tin người dùng
    public void setUserInfomation(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            return;
        }

        // Lấy UID của user đang đăng nhập
        String uid = currentUser.getUid();

        // Truy cập vào nhánh "Users"
        mDatabase.child("Users").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Ép kiểu dữ liệu lấy về thành class User
                User userProfile = task.getResult().getValue(User.class);

                if (userProfile != null) {
                    // Đổ dữ liệu từ model User lên EditText
                    edtFullName.setText(userProfile.getFullName());
                    etdEmail.setText(userProfile.getEmail());
                    edtPhone.setText(userProfile.getPhoneNumber());
                    edtGender.setText(userProfile.getGender());
                    edtDob.setText(userProfile.getDob());
                    edtCccd.setText(userProfile.getCitizenCard());
                    // Kiểm tra và load ảnh bằng Glide
                    String avatarUrl = userProfile.getAvatar();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(userProfile.getAvatar())
                                .placeholder(R.drawable.ic_nav_profile) // Ảnh hiện trong lúc chờ tải
                                .error(R.drawable.ic_nav_profile)         // Ảnh hiện nếu bị lỗi
                                .into(imgAvatar);

                        // ảnh small
                        Glide.with(this)
                                .load(userProfile.getAvatar())
                                .placeholder(R.drawable.ic_nav_profile) // Ảnh hiện trong lúc chờ tải
                                .error(R.drawable.ic_nav_profile)         // Ảnh hiện nếu bị lỗi
                                .into(imgSmallAvatar);
                    }
                }
            } else {
                // Xử lý lỗi nếu không tìm thấy dữ liệu
                Toast.makeText(this, "Không thể tải dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    //
    private void initListener(){
        // sự kiện lưu ảnh
        imgChangeAvatar.setOnClickListener(v -> {
            onClickRequestPermission();
        });

        // sk lưu thông tin
        btnSaveChanges.setOnClickListener(view -> {
            onClickUpdateProfile();
        });

    }

    // Hàm kiểm tra và xin quyền mở nơi chứa ảnh
    private void onClickRequestPermission() {
        // Nếu chạy trên Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES};
                requestPermissions(permissions, MY_REQUEST_CODE);
            }
        }
        // Nếu chạy trên Android 6.0 đến Android 12
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, MY_REQUEST_CODE);
            }
        }
        // Android dưới 6.0
        else {
            openGallery();
        }
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    // nhận kết quả khi người dùng từ chối mở quyền kho ảnh
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Người dùng vừa bấm "Cho phép"
                openGallery();
            } else {
                // Người dùng bấm "Từ chối"
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // upload lên Cloudinary
    private void uploadImageToCloudinary(Uri imageUri, HashMap<String, Object> updates,
                                         DatabaseReference mDatabase, FirebaseUser currentUser, String newName) {

        MediaManager.get().upload(imageUri)
                .preprocess(ImagePreprocessChain.limitDimensionsChain(1000, 1000)
                        .addStep(new DimensionsValidator(10, 10, 1000, 1000))
                        .saveWith(new BitmapEncoder(BitmapEncoder.Format.WEBP, 80)))
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Bắt đầu upload
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Lấy link ảnh (https) trả về từ Cloudinary
                        String imageUrl = (String) resultData.get("secure_url");

                        updates.put("avatar", imageUrl);

                        // Cập nhật Firebase
                        Uri photoUri = Uri.parse(imageUrl);
                        updateDataToDatabase(currentUser, mDatabase, updates, newName, photoUri);
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(UserEditProfileActivity.this, "Lỗi tải ảnh lên: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                    }
                }).dispatch(this);
    }

    private void updateDataToDatabase(FirebaseUser currentUser,
                                      DatabaseReference dbRef, HashMap<String, Object> updates, String newName, Uri newPhotoUri) {

        // Cập nhật vào Realtime Database
        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                //  Cập nhật UserProfile của Firebase Auth
                UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName);
                if (newPhotoUri != null) {
                    profileUpdatesBuilder.setPhotoUri(newPhotoUri);
                }
                UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();

                currentUser.updateProfile(profileUpdates).addOnCompleteListener(authTask -> {
                    progressDialog.dismiss();
                    Toast.makeText(UserEditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình Edit, quay lại màn hình Profile
                });

            } else {
                progressDialog.dismiss();
                Toast.makeText(UserEditProfileActivity.this, "Cập nhật Database thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // update profile trên trang firebase
    public void onClickUpdateProfile() {
        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String strFullName = edtFullName.getText().toString().trim();
        String strPhone = edtPhone.getText().toString().trim();
        String strGender = edtGender.getText().toString().trim();
        String strDob = edtDob.getText().toString().trim();
        String strCccd = edtCccd.getText().toString().trim();

        progressDialog.setMessage("Đang Xử lý");
        progressDialog.show();

        String uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("fullName", strFullName);
        updates.put("phoneNumber", strPhone);
        updates.put("gender", strGender);
        updates.put("dob", strDob);
        updates.put("citizenCard", strCccd);

        if (muri != null) {

            uploadImageToCloudinary(muri, updates, mDatabase, currentUser, strFullName);
        } else {
            updateDataToDatabase(currentUser, mDatabase, updates, strFullName, null);

        }
    }

}