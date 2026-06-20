package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.data.model.SeatMapMetadata;
import com.example.flight_booking_app.data.repository.SeatRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatViewModel extends ViewModel {

    private final SeatRepository repository;

    private final MutableLiveData<List<Seat>> seatMapData = new MutableLiveData<>();

    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();

    // trạng thái ghế lúc chọn
    private final MutableLiveData<Seat> currentlyViewingSeatLive = new MutableLiveData<>();

    // trạng thái số cột hiện thị lưới ghế
    private final MutableLiveData<Integer> gridSpanCountLive = new MutableLiveData<>();


    // ─── Trạng thái chọn ghế ──────────────────────────────────────────────
    //  khôi phục màu ghế sau khi xoay màn hình
    private final Map<String, Seat> selectedSeatById = new HashMap<>();
    /**
     * Ghế đang chọn tab hiện tại (tạm thời).
     */
    private final List<Seat> selectedSeats = new ArrayList<>();
    /**
     * Ghế đã chốt lượt đi (khứ hồi). Không bị xóa bởi clearCurrentSelections().
     */
    private List<Seat> selectedOutboundSeats = new ArrayList<>();
    private List<Seat> selectedReturnSeats = new ArrayList<>();


    private boolean isSelectingReturn = false;

    // ─── Pre-selected seats từ BookingInfo ───────────────────────────────
    /**
     * SeatNumber đã chọn ở lần trước, truyền từ BookingInfoActivity qua Intent.
     * Lưu dạng seatNumber ("12A") vì lúc nhận Intent chưa có Seat object từ Firebase.
     * Được dùng trong buildGridFromSeats() để tự động đánh dấu isSelected + thêm vào
     * selectedSeats khi grid load xong, giúp UI hiển thị lại đúng ghế đã chọn.
     */
    private List<String> preSelectedOutSeatNumbers = new ArrayList<>();
    private List<String> preSelectedRetSeatNumbers = new ArrayList<>();

    public SeatViewModel() {
        this.repository = new SeatRepository();
    }

    // ─── LiveData getters ─────────────────────────────────────────────────

    public LiveData<List<Seat>> getSeatMapData() {
        return seatMapData;
    }

    public LiveData<UiState> getIsLoading() {
        return loadState;
    }

    public LiveData<Seat> getCurrentlyViewingSeatLive() {
        return currentlyViewingSeatLive;
    }

    public LiveData<Integer> getGridSpanCountLive() {
        return gridSpanCountLive;
    }

    // ─── State getters / setters ──────────────────────────────────────────

    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }

    public List<Seat> getSelectedOutboundSeats() {
        return selectedOutboundSeats;
    }

    public void setSelectedOutboundSeats(List<Seat> s) {
        selectedOutboundSeats = new ArrayList<>(s);
    }

    public List<Seat> getSelectedReturnSeats() {
        return selectedReturnSeats;
    }

    public void setSelectedReturnSeats(List<Seat> s) {
        selectedReturnSeats = new ArrayList<>(s);
    }

    public boolean isSelectingReturn() {
        return isSelectingReturn;
    }

    public void setSelectingReturn(boolean v) {
        isSelectingReturn = v;
    }

    // ─── Pre-selected: nhận danh sách seatNumber từ Activity ─────────────

    /**
     * Gọi 1 lần trong setupViewModel() của Activity, TRƯỚC loadSeatMap().
     * Lưu seatNumber đã chọn ở lần trước để buildGridFromSeats() có thể
     * khôi phục highlight khi Firebase trả về grid mới.
     * <p>
     * Tại sao dùng seatNumber thay vì seatId:
     * - Activity chỉ có seatNumber (vd "12A") được truyền qua Intent
     * - seatId chỉ biết khi đọc được Seat object từ Firebase
     * → Khớp theo seatNumber trong buildGridFromSeats() là đủ và chính xác
     *
     * @param outSeats list seatNumber lượt đi (null-safe)
     * @param retSeats list seatNumber lượt về (null-safe)
     */
    public void setPreSelectedSeats(List<String> outSeats, List<String> retSeats) {
        preSelectedOutSeatNumbers = outSeats != null ? new ArrayList<>(outSeats) : new ArrayList<>();
        preSelectedRetSeatNumbers = retSeats != null ? new ArrayList<>(retSeats) : new ArrayList<>();
    }

    // ─── Currently viewing ────────────────────────────────────────────────

    public void setCurrentlyViewingSeat(Seat seat, int maxPassengers) {
        if (seat.isSelected() || selectedSeats.size() < maxPassengers) {
            currentlyViewingSeatLive.setValue(seat);
        }
    }

    /**
     * Phát lại ghế cuối cùng trong selectedSeats vào currentlyViewingSeatLive.
     * Gọi sau khi buildGridFromSeats() restore pre-selected xong
     * tvSeatName hiển thị đúng ghế đã chọn lần trước khi quay từ BookingInfo về.
     * Nếu chưa chọn ghế nào thì phát null → "Vui lòng chọn".
     */
    public void postCurrentlyViewingSeat() {
        if (selectedSeats.isEmpty()) {
            currentlyViewingSeatLive.postValue(null);
        } else {
            currentlyViewingSeatLive.postValue(selectedSeats.get(selectedSeats.size() - 1));
        }
    }

    // ─── Select / Deselect ────────────────────────────────────────────────

    public void selectSeat(Seat seat) {
        seat.setSelected(true);
        selectedSeats.add(seat);
        selectedSeatById.put(seat.getSeatId(), seat);
    }

    public void deselectSeat(Seat seat) {
        seat.setSelected(false);
        selectedSeats.remove(seat);
        selectedSeatById.remove(seat.getSeatId());
    }

    /**
     * Xóa toàn bộ ghế đang chọn tab hiện tại.
     * Gọi khi chuyển tab đi → về.
     * KHÔNG xóa selectedOutboundSeats.
     */
    public void clearCurrentSelections() {
        for (Seat s : selectedSeats) s.setSelected(false);
        selectedSeats.clear();
        selectedSeatById.clear();
        currentlyViewingSeatLive.setValue(null);
    }

    // ─── Load seat map ────────────────────────────────────────────────────

    public void loadSeatMap(String templateId, String flightId) {
        if (templateId == null || templateId.isEmpty()
                || flightId == null || flightId.isEmpty()) return;

        loadState.setValue(UiState.loading());

        repository.fetchSeatsForFlight(templateId, flightId, new SeatRepository.OnSeatsLoadedListener() {
            @Override
            public void onLoaded(List<Seat> seats, SeatMapMetadata metadata) {
                List<Seat> grid = buildGridFromSeats(seats, metadata);
                restoreSelectionState(grid);
                seatMapData.setValue(grid);
                loadState.setValue(UiState.success());
            }

            @Override
            public void onError(String error) {
                loadState.setValue(UiState.error(error));
            }
        });
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    /**
     * Khôi phục isSelected cho ghế đang trong selectedSeatById (sau xoay màn hình).
     * Cập nhật tham chiếu object trong selectedSeats sang object mới từ Firebase.
     */
    private void restoreSelectionState(List<Seat> grid) {
        if (selectedSeatById.isEmpty()) return;
        for (Seat seat : grid) {
            if (seat.getSeatId() == null) continue;
            // Đánh dấu lại màu ghế khi đã được chọn
            if (selectedSeatById.containsKey(seat.getSeatId())) {
                seat.setSelected(true);
                int idx = findSelectedSeatIndex(seat.getSeatId());
                if (idx >= 0) selectedSeats.set(idx, seat);
                selectedSeatById.put(seat.getSeatId(), seat);
            }
        }
    }

    private int findSelectedSeatIndex(String seatId) {
        for (int i = 0; i < selectedSeats.size(); i++) {
            if (seatId.equals(selectedSeats.get(i).getSeatId())) return i;
        }
        return -1;
    }

    /**
     * Build lưới UI từ rawSeats + metadata.
     * FIX: Sau khi tạo grid, duyệt qua để khôi phục ghế pre-selected từ BookingInfo.
     * Dùng seatNumber để khớp (không cần seatId).
     * Ghế khớp sẽ được: đánh dấu isSelected=true + thêm vào selectedSeats + selectedSeatById
     * → updateBottomBar() và UI grid đều hiển thị đúng ngay khi grid load xong.
     */
    private List<Seat> buildGridFromSeats(List<Seat> rawSeats, SeatMapMetadata metadata) {
        Map<String, Seat> seatMap = new HashMap<>();
        int maxRow = 0;

        for (Seat s : rawSeats) {
            seatMap.put(s.getRow() + s.getColumn(), s);
            if (s.getRow() > maxRow) maxRow = s.getRow();
        }

        gridSpanCountLive.postValue(metadata.spanCount);

        List<Seat> grid = new ArrayList<>(maxRow * metadata.spanCount);

        // Chọn đúng list preSelected theo tab đang load
        List<String> preSelectedNumbers = isSelectingReturn
                ? preSelectedRetSeatNumbers
                : preSelectedOutSeatNumbers;

        for (int row = 1; row <= maxRow; row++) {
            int colIndex = 0;
            for (int i = 0; i < metadata.spanCount; i++) {

                if (metadata.aisles != null && metadata.aisles.contains(i)) {
                    Seat aisle = new Seat();
                    aisle.setType("AISLE");
                    aisle.setSeatNumber(String.valueOf(row));
                    grid.add(aisle);
                } else {
                    String columnLetter = metadata.columns.get(colIndex);
                    String key = row + columnLetter;
                    Seat firebaseSeat = seatMap.get(key);

                    if (firebaseSeat == null
                            || "BLOCKED".equalsIgnoreCase(firebaseSeat.getStatus())) {
                        Seat hidden = new Seat();
                        hidden.setType("HIDDEN");
                        grid.add(hidden);
                    } else {
                        // Khôi phục pre-selected từ BookingInfo
                        if (!preSelectedNumbers.isEmpty()
                                && preSelectedNumbers.contains(firebaseSeat.getSeatNumber())) {
                            firebaseSeat.setSelected(true);
                            // Chỉ thêm vào selectedSeats nếu chưa có (tránh duplicate khi reload)
                            if (!selectedSeatById.containsKey(firebaseSeat.getSeatId())) {
                                selectedSeats.add(firebaseSeat);
                                selectedSeatById.put(firebaseSeat.getSeatId(), firebaseSeat);
                            }
                        }
                        grid.add(firebaseSeat);
                    }
                    colIndex++;
                }
            }
        }

        // Sau khi restore pre-selected xong → phát ghế cuối để tvSeatName hiển thị đúng
        postCurrentlyViewingSeat();

        return grid;
    }
}