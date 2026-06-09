package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Passenger;

public class PassengerInputViewModel extends ViewModel {

    private final MutableLiveData<Passenger> passengerLive = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    // Nhận dữ liệu ban đầu từ Intent
    public void initPassenger(Passenger passenger) {
        if (passengerLive.getValue() == null && passenger != null) {
            passengerLive.setValue(passenger);
        }
    }

    public LiveData<Passenger> getPassengerLive() { return passengerLive; }
    public LiveData<String> getValidationError() { return validationError; }
    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }

    // Cập nhật từng trường khi người dùng nhập
    public void updateFullName(String name) {
        Passenger p = passengerLive.getValue();
        if (p != null) p.setFullName(name);
    }

    public void updateTitle(String gender) {
        Passenger p = passengerLive.getValue();
        if (p != null) p.setTitle(gender);
    }

    public void updateDob(String dob) {
        Passenger p = passengerLive.getValue();
        if (p != null) p.setDateOfBirth(dob);
    }

    // Nút bấm "Tiếp tục" gọi hàm này
    public void validateAndSave() {
        Passenger p = passengerLive.getValue();
        if (p == null) return;

        if (p.getTitle() == null || p.getTitle().trim().isEmpty()) {
            validationError.setValue("Vui lòng chọn danh xưng");
            return;
        }
        if (p.getFullName() == null || p.getFullName().trim().isEmpty()) {
            validationError.setValue("Vui lòng nhập họ và tên");
            return;
        }
        if (p.getDateOfBirth() == null || p.getDateOfBirth().trim().isEmpty()) {
            validationError.setValue("Vui lòng chọn ngày sinh");
            return;
        }

        // Nếu qua hết các bước kiểm tra -> Thành công
        saveSuccess.setValue(true);
    }
}