package com.example.flight_booking_app.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flight_booking_app.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;

public class UserEditProfileActivity extends AppCompatActivity {

    MaterialToolbar toolbar ;
    EditText edtGender, edtDob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_edit_profile);

        toolbar = findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        // Ánh xạ View
        edtGender = findViewById(R.id.edtGender);
        edtDob = findViewById(R.id.edtDob);

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
    }
}