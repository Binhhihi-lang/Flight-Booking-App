package com.example.flight_booking_app.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.repository.FlightRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * FlightViewModel – MVVM ViewModel cho màn hình tìm kiếm chuyến bay.
 * <p>
 * ── Phân trang (Client-side Pagination) ────────────────────────────────────
 * Firebase Realtime Database trả về toàn bộ kết quả 1 lần (không hỗ trợ
 * offset query). ViewModel tự cắt list thành từng trang PAGE_SIZE item.
 * <p>
 * Luồng hoạt động:
 * 1. searchFlights() gọi Repository → nhận về allFlights (toàn bộ)
 * 2. ViewModel reset currentPage = 1, pagedFlights = trang đầu
 * 3. Khi scroll đến cuối → Activity gọi loadNextPage()
 * 4. ViewModel append trang tiếp vào pagedFlights (dùng delay giả lập network)
 * 5. isLastPage = true khi đã hiển thị hết allFlights
 * <p>
 * Toàn bộ trạng thái (currentPage, allFlights, pagedFlights, isLoading,
 * isLastPage) nằm trong ViewModel → xoay màn hình KHÔNG mất trạng thái.
 * <p>
 * ── Trạng thái chọn chuyến bay ─────────────────────────────────────────────
 * - selectedOutboundFlight / selectedReturnFlight : chuyến đã chốt mỗi chiều
 * - isSelectingReturn : tab đang hiển thị
 * - currentlyViewingFlight : chuyến đang xem trong BottomSheet
 */
public class FlightViewModel extends ViewModel {

    // ─── Hằng số phân trang
    private static final int PAGE_SIZE = 10;  // số item mỗi trang
    private static final long LOAD_DELAY_MS = 800; // ms giả lập network delay

    private final FlightRepository repository;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Danh sách đang hiển thị trên RecyclerView (tăng dần theo trang).
     */
    private final MutableLiveData<List<Flight>> pagedFlightsLive = new MutableLiveData<>();

    /**
     * Trạng thái load toàn màn hình (lần đầu vào / search mới).
     */
    private final MutableLiveData<AuthResult> loadState = new MutableLiveData<>();

    /**
     * Trạng thái load thêm trang (scroll xuống).
     * true  = đang load trang tiếp Activity hiện ProgressBar
     * false = load xong
     */
    private final MutableLiveData<Boolean> loadingMoreLive = new MutableLiveData<>(false);

    /**
     * true khi đã hiển thị hết toàn bộ kết quả.
     * Activity dùng để disable scroll listener.
     */
    private final MutableLiveData<Boolean> isLastPageLive = new MutableLiveData<>(false);

    // ─── Trạng thái phân trang
    /**
     * Toàn bộ kết quả từ Firebase (chưa cắt trang).
     */
    private List<Flight> allFlights = new ArrayList<>();
    /**
     * Danh sách đang hiển thị (tích lũy từ trang 1 → currentPage).
     */
    private List<Flight> pagedFlights = new ArrayList<>();
    private int currentPage = 0;  // 0 = chưa load lần nào
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // ─── Trạng thái chọn chuyến bay ──────────────────────────────────────
    private Flight selectedOutboundFlight = null;
    private Flight selectedReturnFlight = null;
    private boolean isSelectingReturn = false;
    private Flight currentlyViewingFlight = null;

    // ─── Constructor ─────────────────────────────────────────────────────
    public FlightViewModel() {
        repository = new FlightRepository();
    }

    // ─── LiveData getters ─────────────────────────────────────────────────

    /**
     * RecyclerView observe cái này để hiển thị danh sách.
     */
    public LiveData<List<Flight>> getPagedFlightsLive() {
        return pagedFlightsLive;
    }

    public LiveData<AuthResult> getLoadState() {
        return loadState;
    }

    /**
     * true khi đang append trang tiếp → Activity hiện/ẩn  ProgressBar.
     */
    public LiveData<Boolean> getLoadingMoreLive() {
        return loadingMoreLive;
    }

    /**
     * true khi hết data → Activity tắt scroll listener.
     */
    public LiveData<Boolean> getIsLastPageLive() {
        return isLastPageLive;
    }

    // ─── Pagination state getters (Activity dùng trong scroll listener)

    public boolean isLoading() {
        return isLoading;
    }

    // tắt xử lý khi load đến trang cuối cùng
    public boolean isLastPage() {
        return isLastPage;
    }

    /**
     * true khi chưa load lần nào (currentPage == 0 và allFlights rỗng).
     * Activity dùng để phân biệt lần đầu vào màn hình với xoay màn hình:
     * - Lần đầu vào  → currentPage == 0 → cần gọi searchFlights()
     * - Xoay màn hình → currentPage >= 1 → ViewModel đã có data, KHÔNG gọi lại
     */
    public boolean isFirstLoad() {
        return currentPage == 0 && allFlights.isEmpty();
    }

    // ─── Trạng thái chọn chuyến bay ──────────────────────────────────────

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

    public void clearSession() {
        selectedOutboundFlight = null;
        selectedReturnFlight = null;
        isSelectingReturn = false;
        currentlyViewingFlight = null;
    }

    // ─── Public API: Tìm kiếm

    /**
     * Tìm kiếm chuyến bay lượt đi.
     * Reset toàn bộ trạng thái phân trang trước khi load dữ liệu mới.
     */
    public void searchFlights(String fromCityId, String toCityId, String departureDate,
                              int adultCount, int childCount, int babyCount) {
        resetPaginationState();
        loadState.setValue(AuthResult.loading());

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
                        // Lưu toàn bộ kết quả, chỉ hiển thị trang 1
                        allFlights = new ArrayList<>(flights);
                        loadFirstPage();
                        loadState.setValue(AuthResult.success());
                    }

                    @Override
                    public void onError(String error) {
                        loadState.setValue(AuthResult.error(error));
                    }
                });
    }

    /**
     * Tìm kiếm chuyến bay lượt về (đảo chiều from/to).
     */
    public void searchReturnFlights(String fromCityId, String toCityId, String returnDate,
                                    int adultCount, int childCount, int babyCount) {
        searchFlights(toCityId, fromCityId, returnDate, adultCount, childCount, babyCount);
    }

    // Public API: Phân trang

    /**
     * Gọi từ scroll listener khi người dùng cuộn đến cuối danh sách.
     * Giả lập network delay LOAD_DELAY_MS trước khi append trang tiếp.
     */
    public void loadNextPage() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        loadingMoreLive.setValue(true);

        // Delay giả lập load
        mainHandler.postDelayed(() -> {
            int fromIndex = currentPage * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, allFlights.size());

            if (fromIndex >= allFlights.size()) {
                // Không còn data
                isLastPage = true;
                isLastPageLive.setValue(true);
            } else {
                List<Flight> nextPage = allFlights.subList(fromIndex, toIndex);
                pagedFlights.addAll(nextPage);
                pagedFlightsLive.setValue(new ArrayList<>(pagedFlights));
                currentPage++;

                // Kiểm tra hết trang chưa
                if (toIndex >= allFlights.size()) {
                    isLastPage = true;
                    isLastPageLive.setValue(true);
                }
            }

            isLoading = false;
            loadingMoreLive.setValue(false);
        }, LOAD_DELAY_MS);
    }

    /**
     * Filter: áp dụng danh sách đã lọc (từ FlightFilterBottomSheet).
     * Reset phân trang dựa trên filteredList thay vì allFlights.
     */
    public void applyFilter(List<Flight> filteredList) {
        // Giữ nguyên allFlights (để reset filter về ban đầu sau này)
        // Chỉ thay danh sách hiển thị bằng filteredList (hiển thị hết, không phân trang)
        // Vì filteredList thường ngắn hơn nhiều
        pagedFlights = new ArrayList<>(filteredList);
        pagedFlightsLive.setValue(new ArrayList<>(pagedFlights));
        isLastPage = true;  // Không load thêm khi đang filter
        isLastPageLive.setValue(true);
    }

    /**
     * Reset filter: quay về hiển thị allFlights với phân trang bình thường.
     */
    public void resetFilter() {
        resetPaginationState();
        loadFirstPage();
    }

    // Helper
    /**
     * Hiển thị trang đầu tiên ngay lập tức (không delay).
     */
    private void loadFirstPage() {
        if (allFlights.isEmpty()) {
            pagedFlights = new ArrayList<>();
            pagedFlightsLive.setValue(pagedFlights);
            isLastPage = true;
            isLastPageLive.setValue(true);
            return;
        }

        int toIndex = Math.min(PAGE_SIZE, allFlights.size());
        pagedFlights = new ArrayList<>(allFlights.subList(0, toIndex));
        pagedFlightsLive.setValue(new ArrayList<>(pagedFlights));
        currentPage = 1;

        isLastPage = (toIndex >= allFlights.size());
        isLastPageLive.setValue(isLastPage);
    }

    /**
     * Reset toàn bộ trạng thái phân trang về ban đầu khi ấn chuyển tab lượt đi và lượt về
     */
    private void resetPaginationState() {
        // Hủy delay đang chờ (nếu có) trước khi reset
        mainHandler.removeCallbacksAndMessages(null);
        allFlights = new ArrayList<>();
        pagedFlights = new ArrayList<>();
        currentPage = 0;
        isLoading = false;
        isLastPage = false;
        isLastPageLive.setValue(false);
        loadingMoreLive.setValue(false);
    }

    // Hủy các tác vụ chạy ngầm (Thread, Handler...)
    @Override
    protected void onCleared() {
        super.onCleared();
        // Tránh memory leak: hủy tất cả callback đang pending
        mainHandler.removeCallbacksAndMessages(null);
    }
}