package com.example.flight_booking_app.ui.viewmodel;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
}