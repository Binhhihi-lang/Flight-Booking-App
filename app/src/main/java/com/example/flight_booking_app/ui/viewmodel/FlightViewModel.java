package com.example.flight_booking_app.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.FlightFilterState;
import com.example.flight_booking_app.data.repository.FlightRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightViewModel extends ViewModel {

    private static final int PAGE_SIZE = 10;
    private static final long LOAD_DELAY_MS = 800;

    private final FlightRepository repository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ─── LiveData cho UI observe ───────────────────────────────────────────
    /**
     * Danh sách đang hiển thị trên RecyclerView (tăng dần theo trang).
     */
    private final MutableLiveData<List<Flight>> pagedFlightsLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingMoreLive = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPageLive = new MutableLiveData<>(false);

    // QUẢN LÝ TRẠNG THÁI BỘ LỌC
    private final MutableLiveData<FlightFilterState> filterStateLive = new MutableLiveData<>(FlightFilterState.defaultState());

    // ─── NGUỒN DỮ LIỆU (Source of Truth) ──────────────────────────────────
    private List<Flight> allFlights = new ArrayList<>();         // Dữ liệu gốc từ Firebase
    private List<Flight> currentPoolFlights = new ArrayList<>(); // Dữ liệu đã lọc, dùng để cắt trang
    private List<Flight> pagedFlights = new ArrayList<>();       // Dữ liệu tích lũy đang hiển thị trên RecyclerView

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // ─── Trạng thái chọn chuyến bay
    private Flight selectedOutboundFlight = null;
    private Flight selectedReturnFlight = null;
    private boolean isSelectingReturn = false;
    private Flight currentlyViewingFlight = null;

    public FlightViewModel() {
        repository = new FlightRepository();
    }

    // ─── Getters ─────────────────────────────────────────────────────────
    public LiveData<List<Flight>> getPagedFlightsLive() { return pagedFlightsLive; }
    public LiveData<UiState> getLoadState() { return loadState; }
    public LiveData<Boolean> getLoadingMoreLive() { return loadingMoreLive; }
    public LiveData<Boolean> getIsLastPageLive() { return isLastPageLive; }

    public LiveData<FlightFilterState> getFilterStateLive() { return filterStateLive; }
    public List<Flight> getAllFlights() { return allFlights; }

    public boolean isLoading() { return isLoading; }
    public boolean isLastPage() { return isLastPage; }
    public boolean isFirstLoad() { return currentPage == 0 && allFlights.isEmpty(); }

    public Flight getSelectedOutboundFlight() { return selectedOutboundFlight; }
    public void setSelectedOutboundFlight(Flight f) { selectedOutboundFlight = f; }
    public Flight getSelectedReturnFlight() { return selectedReturnFlight; }
    public void setSelectedReturnFlight(Flight f) { selectedReturnFlight = f; }
    public boolean isSelectingReturn() { return isSelectingReturn; }
    public void setSelectingReturn(boolean v) { isSelectingReturn = v; }
    public Flight getCurrentlyViewingFlight() { return currentlyViewingFlight; }
    public void setCurrentlyViewingFlight(Flight f) { currentlyViewingFlight = f; }


    // ─── API TÌM KIẾM TỪ REPOSITORY ──────────────────────────────────────
    public void searchFlights(String fromCityId, String toCityId, String departureDate,
                              int adultCount, int childCount, int babyCount) {
        resetPaginationState();
        loadState.setValue(UiState.loading());

        int totalPassengers = adultCount + childCount + babyCount;

        repository.searchFlights(fromCityId, toCityId, departureDate, totalPassengers,
                new FlightRepository.OnFlightsLoaded() {
                    @Override
                    public void onLoaded(List<Flight> flights) {
                        for (Flight f : flights) {
                            f.setAdultCount(adultCount);
                            f.setChildCount(childCount);
                            f.setBabyCount(babyCount);
                        }
                        allFlights = new ArrayList<>(flights);


                        // Lần đầu tải về, chưa có bộ lọc nào được áp dụng
                        applyFilterState(FlightFilterState.defaultState());
                        loadState.setValue(UiState.success());
                    }

                    @Override
                    public void onError(String error) {
                        loadState.setValue(UiState.error(error));
                    }
                });
    }

    public void searchReturnFlights(String fromCityId, String toCityId, String returnDate,
                                    int adultCount, int childCount, int babyCount) {
        searchFlights(toCityId, fromCityId, returnDate, adultCount, childCount, babyCount);
    }

    // ─── API LỌC DỮ LIỆU & ÁP DỤNG STATE ─────────────────────────────────
    public void applyFilterState(FlightFilterState newState) {
        filterStateLive.setValue(newState);
        mainHandler.removeCallbacksAndMessages(null); // Hủy các tác vụ load delay cũ

        List<Flight> filteredList = new ArrayList<>();

        for (Flight f : allFlights) {
            // 1. Lọc Hãng bay
            // Nếu list không rỗng và không chứa hãng thì loại
            if (!newState.selectedAirlines.isEmpty() && !newState.selectedAirlines.contains(f.getAirlineName())) {
                continue;
            }

            // 2. Lọc Khung giờ [Sáng, Chiều, Tối]
            boolean hasTimeSelected = newState.timeSlots.contains(true);
            if (hasTimeSelected) {
                int hour = parseDepartureHour(f.getDepartureTime());
                boolean matchesTime = false;
                if (newState.timeSlots.get(0) && hour >= 0 && hour < 12) matchesTime = true;
                if (newState.timeSlots.get(1) && hour >= 12 && hour < 18) matchesTime = true;
                if (newState.timeSlots.get(2) && hour >= 18 && hour < 24) matchesTime = true;
                if (!matchesTime) continue;
            }

            // 3. Lọc Hạng ghế
            if (!"ALL".equals(newState.seatClass)) {
                String targetSeatType = f.getSeatType();

                // Nếu thẻ này bị lỗi mất dữ liệu seatType
                if (targetSeatType == null) continue;

                boolean matches = false;

                // Đối chiếu chuẩn xác giữa bộ lọc UI và bản chất vật lý của thẻ vé
                if ("ECONOMY".equals(newState.seatClass) && "ECONOMY".equals(targetSeatType)) {
                    matches = true;
                } else if ("PREMIUM".equals(newState.seatClass) && "PREMIUM_ECONOMY".equals(targetSeatType)) {
                    matches = true;
                } else if ("BUSINESS".equals(newState.seatClass) && "BUSINESS".equals(targetSeatType)) {
                    matches = true;
                }

                if (!matches) continue;
            }

            filteredList.add(f);
        }

        // 4. Sắp xếp
        switch (newState.sortMode) {
            case "PRICE_ASC":
                if (newState.showFullPrice) {
                    // Nếu chọn xem giá đầy đủ: Sắp xếp theo tổng Giá hiển thị + Thuế phí
                    filteredList.sort(Comparator.comparingDouble(f -> f.getDisplayPrice() + f.getTaxFee()));
                } else {
                    // Nếu chỉ xem giá gốc
                    filteredList.sort(Comparator.comparingDouble(Flight::getDisplayPrice));
                }
                break;
            case "DEPART_EARLY":
                filteredList.sort((a, b) -> {
                    if (a.getDepartureTime() == null) return 1;
                    if (b.getDepartureTime() == null) return -1;
                    return a.getDepartureTime().compareTo(b.getDepartureTime());
                });
                break;
            case "DURATION":
                filteredList.sort((a, b) ->
                        parseDurationMinutes(a.getDuration()) - parseDurationMinutes(b.getDuration()));
                break;
        }

        // Đổ vào pool và cắt trang lại từ đầu
        currentPoolFlights = filteredList;
        loadFirstPage();
    }

    // ─── API PHÂN TRANG (Cắt từ currentPoolFlights) ──────────────────────
    private void loadFirstPage() {
        currentPage = 1;
        isLastPage = false;
        isLastPageLive.setValue(false);

        if (currentPoolFlights.isEmpty()) {
            pagedFlights = new ArrayList<>();
            pagedFlightsLive.setValue(pagedFlights);
            isLastPage = true;
            isLastPageLive.setValue(true);
            return;
        }

        int toIndex = Math.min(PAGE_SIZE, currentPoolFlights.size());
        pagedFlights = new ArrayList<>(currentPoolFlights.subList(0, toIndex));
        pagedFlightsLive.setValue(new ArrayList<>(pagedFlights));

        isLastPage = (toIndex >= currentPoolFlights.size());
        isLastPageLive.setValue(isLastPage);
    }

    public void loadNextPage() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        loadingMoreLive.setValue(true);

        mainHandler.postDelayed(() -> {
            int fromIndex = currentPage * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, currentPoolFlights.size());

            if (fromIndex >= currentPoolFlights.size()) {
                isLastPage = true;
                isLastPageLive.setValue(true);
            } else {
                List<Flight> nextPage = currentPoolFlights.subList(fromIndex, toIndex);
                pagedFlights.addAll(nextPage);
                pagedFlightsLive.setValue(new ArrayList<>(pagedFlights));
                currentPage++;

                if (toIndex >= currentPoolFlights.size()) {
                    isLastPage = true;
                    isLastPageLive.setValue(true);
                }
            }

            isLoading = false;
            loadingMoreLive.setValue(false);
        }, LOAD_DELAY_MS);
    }

    private void resetPaginationState() {
        mainHandler.removeCallbacksAndMessages(null);
        allFlights = new ArrayList<>();
        currentPoolFlights = new ArrayList<>();
        pagedFlights = new ArrayList<>();
        currentPage = 0;
        isLoading = false;
        isLastPage = false;
        isLastPageLive.setValue(false);
        loadingMoreLive.setValue(false);
        filterStateLive.setValue(FlightFilterState.defaultState());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────
    private int parseDepartureHour(String time) {
        if (time == null || !time.contains(":")) return -1;
        try { return Integer.parseInt(time.split(":")[0]); }
        catch (NumberFormatException e) { return -1; }
    }

    private int parseDurationMinutes(String duration) {
        if (duration == null) return 0;
        int total = 0;
        try {
            String d = duration.toLowerCase().replace("p", "m").replace(" ", "");
            int hIdx = d.indexOf('h');
            int mIdx = d.indexOf('m');
            if (hIdx >= 0) total += Integer.parseInt(d.substring(0, hIdx)) * 60;
            if (mIdx > hIdx + 1) {
                String mPart = d.substring(hIdx + 1, mIdx);
                if (!mPart.isEmpty()) total += Integer.parseInt(mPart);
            }
        } catch (Exception ignored) { }
        return total;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mainHandler.removeCallbacksAndMessages(null);
    }
}