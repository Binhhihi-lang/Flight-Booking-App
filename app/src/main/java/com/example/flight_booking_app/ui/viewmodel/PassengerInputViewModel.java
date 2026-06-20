package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.BaggageOption;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;

import java.util.List;

public class PassengerInputViewModel extends ViewModel {

    private final MutableLiveData<Passenger> passengerLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();

    private final MutableLiveData<List<BaggageOption>> outboundBaggageLive = new MutableLiveData<>();
    private final MutableLiveData<List<BaggageOption>> returnBaggageLive   = new MutableLiveData<>();

    // Nhận dữ liệu ban đầu từ Intent
    public void initPassenger(Passenger passenger) {
        if (passengerLive.getValue() == null && passenger != null) {
            passengerLive.setValue(passenger);
        }
    }

    public void initBaggageOptions(List<BaggageOption> outbound, List<BaggageOption> returnOptions) {
        if (outboundBaggageLive.getValue() == null && outbound != null) {
            outboundBaggageLive.setValue(outbound);
        }
        if (returnBaggageLive.getValue() == null && returnOptions != null) {
            returnBaggageLive.setValue(returnOptions);
        }
    }

    public LiveData<Passenger> getPassengerLive() { return passengerLive; }
    public LiveData<UiState> getUiState() { return uiState; }
    public LiveData<List<BaggageOption>> getOutboundBaggageLive() { return outboundBaggageLive; }
    public LiveData<List<BaggageOption>> getReturnBaggageLive()   { return returnBaggageLive; }

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

    public void updateOutboundBaggage(BaggageOption selected) {
        Passenger p = passengerLive.getValue();
        if (p == null || selected == null) return;
        p.setOutboundBaggageId(selected.getBaggageId());
        p.setOutboundBaggagePrice(selected.isFree() ? 0 : selected.getPriceVnd());
        p.setOutboundBaggageWeight(selected.getWeightKg());
    }

    /**
     * Được gọi từ BaggageAdapter callback khi người dùng chọn gói lượt về.
     */
    public void updateReturnBaggage(BaggageOption selected) {
        Passenger p = passengerLive.getValue();
        if (p == null || selected == null) return;
        p.setReturnBaggageId(selected.getBaggageId());
        p.setReturnBaggagePrice(selected.isFree() ? 0 : selected.getPriceVnd());
        p.setReturnBaggageWeight(selected.getWeightKg());
    }

    // Nút bấm "Tiếp tục" gọi hàm này
    public void validateAndSave() {
        Passenger p = passengerLive.getValue();
        if (p == null) return;

        if (p.getTitle() == null || p.getTitle().trim().isEmpty()) {
            return;
        }
        if (p.getFullName() == null || p.getFullName().trim().isEmpty()) {

            return;
        }
        if (p.getDateOfBirth() == null || p.getDateOfBirth().trim().isEmpty()) {
            return;
        }

        // Thành công
        uiState.setValue(UiState.success());
    }
}