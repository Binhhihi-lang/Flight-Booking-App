package com.example.flight_booking_app.ui.viewmodel;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.FareRule;
import com.example.flight_booking_app.data.repository.BookingInfoRepository;
import com.example.flight_booking_app.ui.view.activity.SeatSelectionActivity;

import java.util.ArrayList;

public class BookingInfoViewModel extends ViewModel {

    private final BookingInfoRepository repository;

    private final MutableLiveData<FareRule> outboundFareRuleLive = new MutableLiveData<>();
    private final MutableLiveData<FareRule> returnFareRuleLive = new MutableLiveData<>();
    private final MutableLiveData<Double> totalPriceLive = new MutableLiveData<>();
    private final MutableLiveData<Double> farePriceLive = new MutableLiveData<>();
    private final MutableLiveData<Double> subTotalPriceLive = new MutableLiveData<>();

    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();

    // lưu trạng thái trả về cho BookingInfo
    private final MutableLiveData<ArrayList<Passenger>> passengerListLive = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> departSeatCodesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<String>> returnSeatCodesLive = new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<Passenger>> getPassengerListLive() { return passengerListLive; }

    public BookingInfoViewModel() {
        this.repository = new BookingInfoRepository();
    }

    public LiveData<FareRule> getOutboundFareRuleLive() { return outboundFareRuleLive; }
    public LiveData<FareRule> getReturnFareRuleLive() { return returnFareRuleLive; }
    public LiveData<UiState> getLoadingLive() { return loadState; }
    public LiveData<Double> getTotalPriceLive() { return totalPriceLive; }
    public LiveData<Double> getFarePriceLive() { return farePriceLive; }
    public LiveData<Double> getSubTotalPriceLive() { return subTotalPriceLive; }

    /**
     * Tải nối tiếp: Lượt đi -> Lượt về -> Tắt Loading
     */
    public void loadFareRules(String outFareRuleId, String retFareRuleId) {
        loadState.setValue(UiState.loading());

            repository.getFareRuleById(outFareRuleId, new BookingInfoRepository.OnFareRuleLoaded() {
                @Override
                public void onLoaded(FareRule outRule) {
                    outboundFareRuleLive.setValue(outRule);

                    // Xong lượt đi -> Kiểm tra và lấy tiếp lượt về (nếu có)
                    if (retFareRuleId != null && !retFareRuleId.isEmpty()) {
                        loadReturnRule(retFareRuleId);
                    } else {
                        // Không có lượt về thì kết thúc luôn
                        loadState.setValue(UiState.success());
                    }
                }

                @Override
                public void onError(String error) {
                    loadState.setValue(UiState.error(error));
                }
            });
    }
    private void loadReturnRule(String retFareRuleId) {
        repository.getFareRuleById(retFareRuleId, new BookingInfoRepository.OnFareRuleLoaded() {
            @Override
            public void onLoaded(FareRule retRule) {
                returnFareRuleLive.setValue(retRule);
                loadState.setValue(UiState.success());
            }

            @Override
            public void onError(String error) {
                loadState.setValue(UiState.error(error));
            }
        });
    }

    // truyền dữ liệu sang SeatSelection
    public void buildSeatSelectionIntent(Intent intent, boolean isRoundTrip) {
        // 1. Nhét dữ liệu lượt đi
        FareRule outRule = outboundFareRuleLive.getValue();
        if (outRule != null) {
            intent.putExtra(SeatSelectionActivity.EXTRA_OUT_FARE_CLASS, outRule.getFareClassName());
            if (outRule.getFreeIncludedSeatTypes() != null) {
                intent.putStringArrayListExtra(SeatSelectionActivity.EXTRA_OUT_FREE_SEATS, new ArrayList<>(outRule.getFreeIncludedSeatTypes()));
            }
        }

        // 2. Nhét dữ liệu lượt về (nếu có)
        if (isRoundTrip) {
            FareRule retRule = returnFareRuleLive.getValue();
            if (retRule != null) {
                intent.putExtra(SeatSelectionActivity.EXTRA_RET_FARE_CLASS, retRule.getFareClassName());
                if (retRule.getFreeIncludedSeatTypes() != null) {
                    intent.putStringArrayListExtra(SeatSelectionActivity.EXTRA_RET_FREE_SEATS, new ArrayList<>(retRule.getFreeIncludedSeatTypes()));
                }
            }
        }
    }

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
    // Trong BookingInfoViewModel.java

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
                // ghế, hành lý , món ăn
                totalPrice += p.getSeatPrice() + p.getOutboundBaggagePrice() + p.getReturnBaggagePrice();

            }
            subTotalPriceLive.setValue(totalPrice);
        }
    }
}