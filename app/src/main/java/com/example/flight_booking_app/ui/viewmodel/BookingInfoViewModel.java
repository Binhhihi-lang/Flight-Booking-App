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
    
    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();

    // lưu trạng thái trả về cho BookingInfo
    private final MutableLiveData<ArrayList<Passenger>> passengerListLive = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> departSeatCodesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<String>> returnSeatCodesLive = new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<Passenger>> getPassengerListLive() { return passengerListLive; }
    public LiveData<ArrayList<String>> getDepartSeatCodesLive() { return departSeatCodesLive; }
    public LiveData<ArrayList<String>> getReturnSeatCodesLive() { return returnSeatCodesLive; }

    public BookingInfoViewModel() {
        this.repository = new BookingInfoRepository();
    }

    public LiveData<FareRule> getOutboundFareRuleLive() { return outboundFareRuleLive; }
    public LiveData<FareRule> getReturnFareRuleLive() { return returnFareRuleLive; }
    public LiveData<UiState> getLoadingLive() { return loadState; }

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
            intent.putExtra(SeatSelectionActivity.EXTRA_OUT_CABIN_CLASS, outRule.getFareClassName());
            if (outRule.getFreeIncludedSeatTypes() != null) {
                intent.putStringArrayListExtra(SeatSelectionActivity.EXTRA_OUT_FREE_SEATS, new ArrayList<>(outRule.getFreeIncludedSeatTypes()));
            }
        }

        // 2. Nhét dữ liệu lượt về (nếu có)
        if (isRoundTrip) {
            FareRule retRule = returnFareRuleLive.getValue();
            if (retRule != null) {
                intent.putExtra(SeatSelectionActivity.EXTRA_RET_CABIN_CLASS, retRule.getFareClassName());
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
        }
    }

    /**
     * Lưu ghế vừa chọn và tiến hành gán luôn ghế cho từng hành khách trong ViewModel
     */
    public void updateSeats(ArrayList<String> departSeats, ArrayList<String> returnSeats, boolean isRoundTrip) {
        departSeatCodesLive.setValue(departSeats != null ? departSeats : new ArrayList<>());
        returnSeatCodesLive.setValue(returnSeats != null ? returnSeats : new ArrayList<>());

        // Tiến hành ghép ghế (Mapping) ngay trong ViewModel để lưu vào Object Passenger
        ArrayList<Passenger> currentList = passengerListLive.getValue();
        if (currentList != null) {
            int departIndex = 0;
            int returnIndex = 0;

            for (Passenger p : currentList) {
                if ("BABY".equals(p.getType())) {
                    p.setSeatNumber("Ngồi cùng ng.lớn"); // Em bé không có ghế
                    continue;
                }

                StringBuilder seatDisplay = new StringBuilder();
                if (isRoundTrip) {
                    if (departSeats != null && departIndex < departSeats.size()) {
                        seatDisplay.append("Đi: ").append(departSeats.get(departIndex));
                        departIndex++;
                    } else seatDisplay.append("Đi: --");

                    if (returnSeats != null && returnIndex < returnSeats.size()) {
                        seatDisplay.append(" | Về: ").append(returnSeats.get(returnIndex));
                        returnIndex++;
                    } else seatDisplay.append(" | Về: --");
                } else {
                    if (departSeats != null && departIndex < departSeats.size()) {
                        seatDisplay.append(departSeats.get(departIndex));
                        departIndex++;
                    } else seatDisplay.append("--");
                }

                // Lưu thẳng chuỗi ghế vào Object
                p.setSeatNumber(seatDisplay.toString());
            }
            // Kích hoạt UI vẽ lại danh sách hành khách (lúc này đã có chữ ghế ngồi đi kèm)
            passengerListLive.setValue(currentList);
        }
    }
}