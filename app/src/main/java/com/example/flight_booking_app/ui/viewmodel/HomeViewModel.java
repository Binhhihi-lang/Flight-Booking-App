package com.example.flight_booking_app.ui.viewmodel;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.City;
import com.example.flight_booking_app.data.model.Searchquerystate;
import com.example.flight_booking_app.ui.view.activity.SearchFlightActivity;

public class HomeViewModel extends ViewModel {

    // SearchQueryState lưu thông tin chuyến bay cần tìm kiếm
    private final MutableLiveData<Searchquerystate> searchState =
            new MutableLiveData<>(Searchquerystate.defaultState());

    private final MutableLiveData<AuthResult> validationError = new MutableLiveData<>();


    public LiveData<Searchquerystate> getSearchState() {
        return searchState;
    }

    public LiveData<AuthResult> getValidationError() {
        return validationError;
    }

    // ── Helper cập nhật

    public interface StateUpdater {
        Searchquerystate apply(Searchquerystate current);
    }

    private void update(StateUpdater updater) {
        Searchquerystate current = searchState.getValue();
        if (current == null) current = Searchquerystate.defaultState();
        searchState.setValue(updater.apply(current));
    }

    // Actions

    public void setFromCity(City city) {
        update(s -> s.withFromCity(city));
    }

    public void setToCity(City city) {
        update(s -> s.withToCity(city));
    }

    public void setDepartDate(String d) {
        update(s -> s.withDepartDate(d));
    }

    public void setReturnDate(String d) {
        update(s -> s.withReturnDate(d));
    }
    public void setPassengerCount(int adultCount, int childCount, int babyCount){
        update(s -> s.withPassenger(adultCount, childCount, babyCount));
    }

    public void setSeatClass(String seatClass) {
        update(s -> s.withSeatClass(seatClass));
    }

    public void setRoundTrip(boolean v) {
        update(s -> s.withRoundTrip(v));
    }

    public void swapCities() {
        update(Searchquerystate::withSwappedCities);
    }

    // validate passenger
    // Trong HomeViewModel.java
    public void  updatePassengers(int adultDelta, int childDelta, int babyDelta) {
        Searchquerystate s = searchState.getValue();
            int newAdult = s.adultCount + adultDelta;
            int newChild = s.childCount + childDelta;
            int newBaby = s.babyCount + babyDelta;


            // Người lớn tối thiểu là 1, trẻ em/em bé tối thiểu là 0
            if (newAdult < 1) newAdult = 1;
            if (newChild < 0) newChild = 0;
            if (newBaby < 0) newBaby = 0;

            // Tổng người lớn + trẻ em không quá 9
        if (newAdult + newChild > 9) {
            error("Tối đa 9 hành khách (người lớn và trẻ em)");
            return;
        }

        // 4. Validation logic Em bé không được nhiều hơn Người lớn mới
        if (newBaby > newAdult) {
            error("Số lượng em bé phải nhỏ hơn hoặc bằng số lượng người lớn");
            return;
        }
        setPassengerCount(newAdult, newChild, newBaby);

    }
    private void error(String msg) {
        validationError.setValue(AuthResult.error(msg));
    }

    /**
     * Fragment gọi hàm này, ViewModel tự đóng gói toàn bộ state vào Intent
     */
    public void buildSearchIntent(Intent intent) {
        Searchquerystate s = searchState.getValue();

        intent.putExtra(SearchFlightActivity.EXTRA_IS_ROUND_TRIP, s.isRoundTrip);
        intent.putExtra(SearchFlightActivity.EXTRA_FROM_CITY_ID, s.fromCity.getCityId());
        intent.putExtra(SearchFlightActivity.EXTRA_TO_CITY_ID, s.toCity.getCityId());
        intent.putExtra(SearchFlightActivity.EXTRA_FROM_CITY, s.fromCity.getCityName());
        intent.putExtra(SearchFlightActivity.EXTRA_TO_CITY, s.toCity.getCityName());
        intent.putExtra(SearchFlightActivity.EXTRA_FROM_IATA, s.fromCity.getIataCode());
        intent.putExtra(SearchFlightActivity.EXTRA_TO_IATA, s.toCity.getIataCode());
        intent.putExtra(SearchFlightActivity.EXTRA_DEPART_DATE, s.departDate);
        intent.putExtra(SearchFlightActivity.EXTRA_RETURN_DATE, s.returnDate);
        intent.putExtra(SearchFlightActivity.EXTRA_ADULT, s.adultCount);
        intent.putExtra(SearchFlightActivity.EXTRA_CHILD, s.childCount);
        intent.putExtra(SearchFlightActivity.EXTRA_BABY, s.babyCount);
        intent.putExtra(SearchFlightActivity.EXTRA_SEAT_CLASS, s.seatClass);

    }

}