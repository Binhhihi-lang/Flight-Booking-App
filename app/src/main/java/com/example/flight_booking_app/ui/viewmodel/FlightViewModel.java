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

    public FlightViewModel() {
        repository = new FlightRepository();
    }

    public LiveData<List<Flight>> getFlightList() {
        return flightList;
    }

    public LiveData<AuthResult> getLoadState() {
        return loadState;
    }

    /**
     * Tìm kiếm chuyến bay đầy đủ.
     *
     * @param fromCityId    ID thành phố đi
     * @param toCityId      ID thành phố đến
     * @param departureDate Ngày bay (dd/MM/yyyy)
     * @param seatClass     Hạng ghế ("Phổ thông" / "Thương gia")
     * @param adultCount    Số người lớn
     * @param childCount    Số trẻ em
     * @param babyCount     Số em bé
     */
    public void searchFlights(String fromCityId, String toCityId, String departureDate,
                              String seatClass, int adultCount, int childCount, int babyCount) {

        loadState.setValue(AuthResult.loading());

        int totalPassengers = adultCount + childCount + babyCount;

        repository.searchFlights(fromCityId, toCityId, departureDate, seatClass, totalPassengers,
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

    /**
     * Tìm chuyến bay lượt về (tab LƯỢT VỀ).
     * Đảo cityId + thay departureDate thành returnDate.
     */
    public void searchReturnFlights(String fromCityId, String toCityId, String returnDate,
                                    String seatClass, int adultCount, int childCount, int babyCount) {
        // Đảo chiều: toCityId → fromCityId
        searchFlights(toCityId, fromCityId, returnDate, seatClass, adultCount, childCount, babyCount);
    }
}