package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.FlightFilterState;
import com.example.flight_booking_app.data.repository.FlightRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightViewModel extends ViewModel {

    private final FlightRepository repository;

    private final MutableLiveData<List<Flight>> pagedFlightsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingMoreLive = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPageLive = new MutableLiveData<>(false);
    private final MutableLiveData<FlightFilterState> filterStateLive = new MutableLiveData<>(FlightFilterState.defaultState());
    private final MutableLiveData<Boolean> isOfflineLive = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsOfflineLive() {
        return isOfflineLive;
    }

    // Lưu trữ danh sách chuyến bay tích lũy từ server
    private final List<Flight> accumulatedFlights = new ArrayList<>();

    // Con trỏ giữ vị trí phân trang của Firestore
    private DocumentSnapshot lastVisibleDoc = null;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Các biến lưu trạng thái đặt vé phục vụ UI
    private FareClass currentlyViewingFare;
    private FareClass selectedOutboundFare;
    private FareClass selectedReturnFare;
    private Flight selectedOutboundFlight = null;
    private Flight selectedReturnFlight = null;
    private boolean isSelectingReturn = false;
    private Flight currentlyViewingFlight = null;

    // Lưu các tham số search để dùng lại khi phân trang (Load more)
    private String currentFromCityId, currentToCityId, currentDepartureDate;
    private int currentTotalPassengers, currentAdultCount, currentChildCount, currentBabyCount;

    //
    public FlightViewModel() {
        repository = new FlightRepository();
    }


    public LiveData<List<Flight>> getPagedFlightsLive() {
        return pagedFlightsLive;
    }

    public LiveData<UiState> getLoadState() {
        return loadState;
    }

    public LiveData<Boolean> getLoadingMoreLive() {
        return loadingMoreLive;
    }

    public LiveData<Boolean> getIsLastPageLive() {
        return isLastPageLive;
    }

    public LiveData<FlightFilterState> getFilterStateLive() {
        return filterStateLive;
    }

    public List<Flight> getFlights() {
        return accumulatedFlights;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public boolean isFirstLoad() {
        return accumulatedFlights.isEmpty() && lastVisibleDoc == null;
    }

    public void setCurrentlyViewingFare(FareClass fc) {
        this.currentlyViewingFare = fc;
    }

    public FareClass getCurrentlyViewingFare() {
        return currentlyViewingFare;
    }

    public void setSelectedOutboundFare(FareClass fc) {
        this.selectedOutboundFare = fc;
    }

    public FareClass getSelectedOutboundFare() {
        return selectedOutboundFare;
    }

    public void setSelectedReturnFare(FareClass fc) {
        this.selectedReturnFare = fc;
    }

    public FareClass getSelectedReturnFare() {
        return selectedReturnFare;
    }

    public Flight getSelectedOutboundFlight() {
        return selectedOutboundFlight;
    }

    public void setSelectedOutboundFlight(Flight f) {
        selectedOutboundFlight = f;
    }

    public Flight getSelectedReturnFlight() {
        return selectedReturnFlight;
    }

    public void setSelectedReturnFlight(Flight f) {
        selectedReturnFlight = f;
    }

    public boolean isSelectingReturn() {
        return isSelectingReturn;
    }

    public void setSelectingReturn(boolean v) {
        isSelectingReturn = v;
    }

    public Flight getCurrentlyViewingFlight() {
        return currentlyViewingFlight;
    }

    public void setCurrentlyViewingFlight(Flight f) {
        currentlyViewingFlight = f;
    }

    // Tìm chuyến bay từ server
    public void searchFlights(String fromCityId, String toCityId, String departureDate,
                              int adultCount, int childCount, int babyCount) {

        resetPaginationState();
        loadState.setValue(UiState.loading());

        // Lưu tham số để dùng cho việc kéo trang tiếp theo
        this.currentFromCityId = fromCityId;
        this.currentToCityId = toCityId;
        this.currentDepartureDate = departureDate;
        this.currentAdultCount = adultCount;
        this.currentChildCount = childCount;
        this.currentBabyCount = babyCount;
        this.currentTotalPassengers = adultCount + childCount + babyCount;

        executeServerSearch(adultCount, childCount, babyCount);
    }

    // Lấy trang đầu tiên từ Firestore Cloud
    private void executeServerSearch(int adultCount, int childCount, int babyCount) {
        repository.searchFlights(currentFromCityId, currentToCityId, currentDepartureDate, currentTotalPassengers, filterStateLive.getValue(),
                new FlightRepository.OnFlightsLoaded() {
                    @Override
                    public void onLoaded(List<Flight> flights, DocumentSnapshot lastVisible, Boolean isOffline) {
                        isLoading = false;
                        lastVisibleDoc = lastVisible;

                        if (isOffline != null) {
                            isOfflineLive.setValue(isOffline);
                        }

                        if (flights == null || flights.isEmpty()) {
                            isLastPage = true;
                            isLastPageLive.setValue(true);
                        }

                        for (Flight f : flights) {
                            f.setAdultCount(adultCount);
                            f.setChildCount(childCount);
                            f.setBabyCount(babyCount);
                        }

                        accumulatedFlights.clear();
                        accumulatedFlights.addAll(flights);

                        // Để lọc nốt Khung giờ / Hạng ghế trên RAM và hiển thị
                        renderDataToUI();
                        loadState.setValue(UiState.success());
                    }

                    @Override
                    public void onError(String error) {
                        isLoading = false;
                        loadState.setValue(UiState.error(error));
                    }

                });
    }

    public void searchReturnFlights(String fromCityId, String toCityId, String returnDate,
                                    int adultCount, int childCount, int babyCount) {
        searchFlights(toCityId, fromCityId, returnDate, adultCount, childCount, babyCount);
    }

    public void loadNextPage() {
        if (isLoading || isLastPage || lastVisibleDoc == null) return;

        isLoading = true;
        loadingMoreLive.setValue(true);

        repository.searchFlightsNextPage(currentFromCityId, currentToCityId, currentDepartureDate,
                currentTotalPassengers, filterStateLive.getValue(), lastVisibleDoc, new FlightRepository.OnFlightsLoaded() {
                    @Override
                    public void onLoaded(List<Flight> nextFlights, DocumentSnapshot nextCursor, Boolean isOffline) {
                        isLoading = false;
                        loadingMoreLive.setValue(false);

                        if (isOffline != null) {
                            isOfflineLive.setValue(isOffline);
                        }

                        if (nextFlights == null || nextFlights.isEmpty()) {
                            isLastPage = true;
                            isLastPageLive.setValue(true);
                            return;
                        }

                        lastVisibleDoc = nextCursor; // Cập nhật con trỏ trang tiếp theo

                        // Đồng bộ số lượng hành khách cho các thẻ mới nạp
                        if (!accumulatedFlights.isEmpty()) {
                            Flight first = accumulatedFlights.get(0);
                            for (Flight f : nextFlights) {
                                f.setAdultCount(first.getAdultCount());
                                f.setChildCount(first.getChildCount());
                                f.setBabyCount(first.getBabyCount());
                            }
                        }

                        // Gộp dữ liệu mới tải từ server vào bộ nhớ tích lũy
                        accumulatedFlights.addAll(nextFlights);
                        renderDataToUI();
                    }

                    @Override
                    public void onError(String error) {
                        isLoading = false;
                        loadingMoreLive.setValue(false);
                    }
                });
    }

    // lọc dữ liệu hiển thị

    public void applyFilterState(FlightFilterState newState) {
        filterStateLive.setValue(newState);

        accumulatedFlights.clear();
        lastVisibleDoc = null;
        isLastPage = false;
        isLastPageLive.setValue(false);

        // Ra lệnh cho Server tìm kiếm theo bộ lọc mới
        executeServerSearch(currentAdultCount, currentChildCount, currentBabyCount);
    }

    private void renderDataToUI() {
        FlightFilterState currentState = filterStateLive.getValue();

        // Nếu chưa áp dụng bộ lọc nào (Lần đầu tải app), hiển thị toàn bộ kho
        if (currentState == null) {
            pagedFlightsLive.setValue(new ArrayList<>(accumulatedFlights));
            return;
        }

        List<Flight> filteredList = new ArrayList<>();
        for (Flight f : accumulatedFlights) {
            // (Server đã lọc Hãng bay rồi

            //  Lọc Khung giờ
            boolean hasTimeSelected = currentState.timeSlots.contains(true);
            if (hasTimeSelected) {
                int hour = parseDepartureHour(f.getDepartureTime());
                boolean matchesTime = false;
                if (currentState.timeSlots.get(0) && hour >= 0 && hour < 12) matchesTime = true;
                if (currentState.timeSlots.get(1) && hour >= 12 && hour < 18) matchesTime = true;
                if (currentState.timeSlots.get(2) && hour >= 18 && hour < 24) matchesTime = true;
                if (!matchesTime) continue;
            }

            // Lọc Hạng ghế
            if (!"ALL".equals(currentState.seatClass)) {
                String targetSeatType = f.getSeatType();
                if (targetSeatType == null) continue;

                boolean matches = false;
                if ("ECONOMY".equals(currentState.seatClass) && "ECONOMY".equals(targetSeatType))
                    matches = true;
                else if ("PREMIUM".equals(currentState.seatClass) && "PREMIUM_ECONOMY".equals(targetSeatType))
                    matches = true;
                else if ("BUSINESS".equals(currentState.seatClass) && "BUSINESS".equals(targetSeatType))
                    matches = true;

                if (!matches) continue;
            }

            filteredList.add(f);
        }

        //  Sắp xếp giá Full có thuế theo thứ tự tăng dần
        if ("PRICE_ASC".equals(currentState.sortMode) && currentState.showFullPrice) {
            filteredList.sort(Comparator.comparingDouble(f -> f.getDisplayPrice() + f.getTaxFee()));
        }

        // set giá trị lọc vào LiveData để hiển thị
        pagedFlightsLive.setValue(filteredList);
    }

    private void resetPaginationState() {
        accumulatedFlights.clear();
        lastVisibleDoc = null;
        isLoading = false;
        isLastPage = false;
        isLastPageLive.setValue(false);
        loadingMoreLive.setValue(false);
        filterStateLive.setValue(FlightFilterState.defaultState());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────
    private int parseDepartureHour(String time) {
        if (time == null || !time.contains(":")) return -1;
        try {
            return Integer.parseInt(time.split(":")[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}