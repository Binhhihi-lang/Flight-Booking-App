package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.repository.FlightRepository;

import java.util.List;

/**
 * FlightViewModel phiên bản hoàn chỉnh.
 *
 * Tìm kiếm theo TẤT CẢ tiêu chí:
 *   - fromCityId, toCityId (điểm đi/đến)
 *   - departureDate (ngày bay)
 *   - seatClass (Phổ thông/Thương gia)
 *   - totalPassengers (adult + child + baby)
 *
 * Repository sẽ:
 *   1. Lọc Flights theo cityId + date
 *   2. Lọc fareOptions theo seatType
 *   3. Kiểm tra availableSeats >= totalPassengers
 *   4. Tính displayPrice = giá rẻ nhất phù hợp
 */
public class FlightViewModel extends ViewModel {

    private final FlightRepository repository;

    private final MutableLiveData<List<Flight>> flightList = new MutableLiveData<>();
    private final MutableLiveData<AuthResult>   loadState  = new MutableLiveData<>();

    // Biến giữ chuyến bay khi chọn
    private Flight selectedOutboundFlight = null; // Lưu chuyến bay đi đã chốt
    private Flight selectedReturnFlight = null;   // Lưu chuyến bay về đã chốt
    private boolean isSelectingReturn = false;     // Mẹo: false = đang chọn đi, true = đang chọn về
    private Flight currentlyViewingFlight = null;  // Lưu chuyến bay đang xem trong BottomSheet


    public FlightViewModel() {
        repository = new FlightRepository();
    }

    // ── Getters và Setters cho các biến trạng thái
    public Flight getSelectedOutboundFlight() { return selectedOutboundFlight; }
    public void setSelectedOutboundFlight(Flight flight) { this.selectedOutboundFlight = flight; }

    public Flight getSelectedReturnFlight() { return selectedReturnFlight; }
    public void setSelectedReturnFlight(Flight flight) { this.selectedReturnFlight = flight; }

    public boolean isSelectingReturn() { return isSelectingReturn; }
    public void setSelectingReturn(boolean selectingReturn) { isSelectingReturn = selectingReturn; }

    public Flight getCurrentlyViewingFlight() { return currentlyViewingFlight; }

    public void setCurrentlyViewingFlight(Flight flight) { this.currentlyViewingFlight = flight; }

    // Hàm xóa sạch dữ liệu cũ khi người dùng kết thúc đặt vé hoặc quay lại tìm chuyến khác
    public void clearSession() {
        this.selectedOutboundFlight = null;
        this.selectedReturnFlight = null;
        this.isSelectingReturn = false;
        this.currentlyViewingFlight = null;
    }

    public LiveData<List<Flight>> getFlightList() {
        return flightList;
    }

    public LiveData<AuthResult> getLoadState() {
        return loadState;
    }

    public void searchFlights(String fromCityId, String toCityId, String departureDate,
                              int adultCount, int childCount, int babyCount) {

        loadState.setValue(AuthResult.loading());

        int totalPassengers = adultCount + childCount + babyCount;

        repository.searchFlights(fromCityId, toCityId, departureDate, totalPassengers,
                new FlightRepository.OnFlightsLoaded() {
                    @Override
                    public void onLoaded(List<Flight> flights) {
                        // Gắn thông tin hành khách vào từng chuyến
                        for (Flight f : flights) {
                            f.setAdultCount(adultCount);
                            f.setChildCount(childCount);
                            f.setBabyCount(babyCount);
                            // selectedSeatClass đã được gắn trong Repository
                        }
                        flightList.setValue(flights);
                        loadState.setValue(AuthResult.success());
                    }

                    @Override
                    public void onError(String error) {
                        loadState.setValue(AuthResult.error(error));
                    }
                });
    }


    public void searchReturnFlights(String fromCityId, String toCityId, String returnDate,
                                    int adultCount, int childCount, int babyCount) {
        // Đảo chiều: toCityId → fromCityId
        searchFlights(toCityId, fromCityId, returnDate, adultCount, childCount, babyCount);
    }


}

