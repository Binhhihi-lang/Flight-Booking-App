package com.example.flight_booking_app.ui.viewmodel;


import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;

import java.util.ArrayList;

public class BookingInfoViewModel extends ViewModel {

    private final MutableLiveData<Double> subTotalPriceLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();

    // lưu trạng thái trả về cho BookingInfo
    private final MutableLiveData<ArrayList<Passenger>> passengerListLive = new MutableLiveData<>();
    public LiveData<ArrayList<Passenger>> getPassengerListLive() { return passengerListLive; }

    public BookingInfoViewModel() {

    }
    public LiveData<Double> getSubTotalPriceLive() { return subTotalPriceLive; }
    public LiveData<UiState> getUiState() { return uiState; }


    /**
     * Hàm này được gọi từ Activity. Nó chỉ tạo danh sách MỘT LẦN DUY NHẤT.
     * Nếu xoay màn hình, list đã có data rồi -> Bỏ qua không tạo lại.
     */
    public void initPassengersIfNeeded(int adultCount, int childCount, int babyCount) {
        if (passengerListLive.getValue() == null) {
            ArrayList<Passenger> list = new ArrayList<>();
            for (int idx = 0; idx < adultCount; idx++) {
                list.add(new Passenger("ADULT", idx, "Người lớn " + (idx + 1)));
            }
            for (int idx = 0; idx < childCount; idx++) {
                list.add(new Passenger("CHILD", idx, "Trẻ em " + (idx + 1)));
            }
            for (int idx = 0; idx < babyCount; idx++) {
                list.add(new Passenger("BABY", idx, "Em bé " + (idx + 1)));
            }
            passengerListLive.setValue(list);
        }
    }

    /**
     * Cập nhật thông tin 1 hành khách khi nhập liệu xong
     */
    public void updatePassenger(Passenger updatedPassenger) {
        ArrayList<Passenger> currentList = passengerListLive.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                Passenger p = currentList.get(i);
                if (p.getType().equals(updatedPassenger.getType()) && p.getIndex() == updatedPassenger.getIndex()) {
                    currentList.set(i, updatedPassenger);
                    break;
                }
            }
            // Kích hoạt báo cho UI vẽ lại
            passengerListLive.setValue(currentList);
            updateSubTotalPrice();
        }
    }

    /**
     * Lưu ghế vừa chọn và tiến hành gán luôn ghế cho từng hành khách trong ViewModel
     */
    public void updateSeats(ArrayList<String> departSeats, ArrayList<Double> departPrices,
                            ArrayList<String> returnSeats, ArrayList<Double> returnPrices,
                            boolean isRoundTrip) {

        ArrayList<Passenger> currentList = passengerListLive.getValue();
        if (currentList != null) {
            int departIndex = 0;
            int returnIndex = 0;

            for (Passenger p : currentList) {
                if ("BABY".equals(p.getType())) {
                    p.setSeatNumber("Ngồi cùng ng.lớn");
                    p.setSeatPrice(0); // Em bé không tốn tiền ghế
                    continue;
                }

                double seatTotalPrice = 0;
                StringBuilder seatDisplay = new StringBuilder();

                // Tính giá và hiển thị cho lượt đi
                if (departSeats != null && departIndex < departSeats.size()) {
                    seatDisplay.append(isRoundTrip ? "Đi: " : "").append(departSeats.get(departIndex));
                    if (departPrices != null) seatTotalPrice += departPrices.get(departIndex);
                    departIndex++;
                }

                // Tính giá và hiển thị cho lượt về
                if (isRoundTrip) {
                    if (returnSeats != null && returnIndex < returnSeats.size()) {
                        seatDisplay.append(" | Về: ").append(returnSeats.get(returnIndex));
                        if (returnPrices != null) seatTotalPrice += returnPrices.get(returnIndex);
                        returnIndex++;
                    }
                }

                p.setSeatNumber(seatDisplay.toString());
                p.setSeatPrice(seatTotalPrice);
            }

            passengerListLive.setValue(currentList);
            updateSubTotalPrice();
        }
    }
    public void updateSubTotalPrice(){
        ArrayList<Passenger> currentList = passengerListLive.getValue();

        double totalPrice=0 ;
        if (currentList != null) {
            for (Passenger p : currentList) {
                // ghế, hành lý
                totalPrice += p.getSeatPrice() + p.getOutboundBaggagePrice() + p.getReturnBaggagePrice();

            }
            subTotalPriceLive.setValue(totalPrice);
        }
    }

    //Validate
    public boolean validateContactInfo(String fullName, String email, String phone) {
        if (fullName.isEmpty()) {
            uiState.setValue(UiState.error("Họ tên phải được nhập"));
            return false; // Dừng lại, báo lỗi
        }
        if (email.isEmpty()) {
            uiState.setValue(UiState.error("Email phải được nhập"));
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState.setValue(UiState.error("Email không hợp lệ!"));
            return false;
        }
        if (phone.isEmpty()) {
            uiState.setValue(UiState.error("Số điện thoại phải được nhập"));
            return false;
        }
        if (!phone.startsWith("0") || phone.length() != 10) {
            uiState.setValue(UiState.error("Số điện thoại không hợp lệ"));
            return false;
        }

        ArrayList<Passenger> passengerList = passengerListLive.getValue();
        for (Passenger p : passengerList) {
            if (!p.isComplete()) {
                uiState.setValue(UiState.error("Vui lòng nhập đầy đủ thông tin hành khách!"));
                return false;
            }
        }

        return true;
    }
}