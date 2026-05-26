package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.data.repository.SeatRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SeatViewModel – Trung gian giữa SeatRepository và SeatSelectionActivity.
 *
 * Trách nhiệm:
 *  1. Tải danh sách ghế thô từ Firebase qua SeatRepository.
 *  2. Biến đổi thô → lưới UI (thêm AISLE, ẩn BLOCKED/HIDDEN).
 *  3. Giữ toàn bộ trạng thái chọn ghế QUA CẢ XOAY MÀN HÌNH:
 *       - selectedSeats         : ghế đang chọn trong tab hiện tại (tạm thời)
 *       - selectedOutboundSeats : danh sách ghế đã chốt lượt đi (khứ hồi)
 *       - isSelectingReturn     : đang chọn tab nào
 *  4. currentlyViewingSeatLive CHỈ phát khi số ghế chưa đủ maxPassengers.
 *     Khi đã đủ ghế, label bottom bar hiển thị cố định từ selectedSeats.
 *  5. Khi grid được rebuild (loadSeatMap), khôi phục isSelected trên các
 *     đối tượng Seat mới để UI hiển thị đúng màu.
 *
 * Lưới A320/A321: 7 cột/hàng = A B C | aisle | D E F
 */
public class SeatViewModel extends ViewModel {

    private static final String[] COLUMNS     = {"A", "B", "C", "D", "E", "F"};
    private static final int      AISLE_INDEX = 3;

    private final SeatRepository repository;

    // ─── LiveData phơi ra ngoài ───────────────────────────────────────────
    private final MutableLiveData<List<Seat>> seatMapData = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> loadState   = new MutableLiveData<>();

    /**
     * Ghế vừa được click — Activity observe để cập nhật label realtime.
     * CHỈ phát giá trị khi số ghế đang chọn CHƯA đủ maxPassengers,
     * hoặc khi bỏ chọn một ghế đang được chọn.
     * Khi đã đủ ghế, Activity dùng updateBottomBar() thay vì LiveData này.
     */
    private final MutableLiveData<Seat> currentlyViewingSeatLive = new MutableLiveData<>();

    // ─── Trạng thái chọn ghế (sống sót qua xoay màn hình) ────────────────

    /**
     * Map seatId → Seat của những ghế ĐANG chọn trong tab hiện tại.
     * Dùng để khôi phục isSelected khi grid được rebuild từ Firebase.
     */
    private final Map<String, Seat> selectedSeatById = new HashMap<>();

    /** Ghế đang chọn trong tab hiện tại (tạm thời, xoá khi chuyển tab). */
    private final List<Seat> selectedSeats = new ArrayList<>();

    /**
     * Ghế đã chốt lượt ĐI (chỉ dùng cho khứ hồi).
     * Được set bởi Activity khi bấm TIẾP TỤC ở lượt đi trước khi chuyển tab.
     * Không bị xoá bởi clearCurrentSelections().
     */
    private List<Seat> selectedOutboundSeats = new ArrayList<>();

    private boolean isSelectingReturn = false;

    // ─── Constructor ──────────────────────────────────────────────────────

    public SeatViewModel() {
        this.repository = new SeatRepository();
    }

    // ─── LiveData getters ─────────────────────────────────────────────────

    public LiveData<List<Seat>> getSeatMapData()              { return seatMapData; }
    public LiveData<AuthResult> getIsLoading()                { return loadState; }
    public LiveData<Seat>       getCurrentlyViewingSeatLive() { return currentlyViewingSeatLive; }

    // ─── State getters / setters ──────────────────────────────────────────

    public List<Seat> getSelectedSeats()       { return selectedSeats; }

    /**
     * Lấy danh sách ghế đã chốt lượt đi.
     * Trả về list rỗng (không null) nếu chưa set.
     */
    public List<Seat> getSelectedOutboundSeats() { return selectedOutboundSeats; }

    /**
     * Lưu toàn bộ danh sách ghế lượt đi sau khi người dùng bấm TIẾP TỤC.
     * Activity truyền vào snapshot của selectedSeats hiện tại.
     */
    public void setSelectedOutboundSeats(List<Seat> seats) {
        this.selectedOutboundSeats = new ArrayList<>(seats);
    }

    public boolean isSelectingReturn()           { return isSelectingReturn; }
    public void    setSelectingReturn(boolean v) { this.isSelectingReturn = v; }

    /**
     * Phát sự kiện ghế đang xem — CHỈ khi chưa đủ ghế hoặc đang bỏ chọn.
     *
     * Logic:
     *   - Đang bỏ chọn (seat.isSelected() == true) → luôn phát để label cập nhật
     *   - Chưa đủ ghế (selectedSeats.size() < max)  → phát để hiển thị ghế đang hover
     *   - Đã đủ ghế và click ghế MỚI               → KHÔNG phát, tránh label nhảy
     *
     * @param seat          ghế vừa click
     * @param maxPassengers số ghế tối đa cần chọn (từ Activity)
     */
    public void setCurrentlyViewingSeat(Seat seat, int maxPassengers) {
        if (seat.isSelected() || selectedSeats.size() < maxPassengers) {
            currentlyViewingSeatLive.setValue(seat);
        }
    }

    // ─── Seat selection helpers (gọi từ Activity) ─────────────────────────

    /**
     * Thêm ghế vào danh sách đang chọn và đánh dấu isSelected.
     * Đồng thời lưu vào selectedSeatById để khôi phục sau xoay màn hình.
     */
    public void selectSeat(Seat seat) {
        seat.setSelected(true);
        selectedSeats.add(seat);
        selectedSeatById.put(seat.getSeatId(), seat);
    }

    /**
     * Bỏ chọn ghế và xoá khỏi các collections liên quan.
     */
    public void deselectSeat(Seat seat) {
        seat.setSelected(false);
        selectedSeats.remove(seat);
        selectedSeatById.remove(seat.getSeatId());
    }

    /**
     * Xoá toàn bộ ghế đang chọn trong tab hiện tại.
     * Gọi khi chuyển từ tab lượt đi → lượt về để bắt đầu chọn sạch.
     * KHÔNG xoá selectedOutboundSeats (đã chốt, không được xoá).
     */
    public void clearCurrentSelections() {
        for (Seat s : selectedSeats) {
            s.setSelected(false);
        }
        selectedSeats.clear();
        selectedSeatById.clear();
        // Reset LiveData về null → observer ở Activity nhận null → hiển thị "Chưa chọn ghế"
        currentlyViewingSeatLive.setValue(null);
    }

    // ─── Public API ───────────────────────────────────────────────────────

    /**
     * Tải sơ đồ ghế từ Firebase.
     * Có thể gọi nhiều lần (khi chuyển tab Đi ↔ Về).
     * Sau khi grid xây xong, khôi phục trạng thái isSelected từ selectedSeatById.
     */
    public void loadSeatMap(String seatMapId) {
        if (seatMapId == null || seatMapId.isEmpty()) return;

        loadState.setValue(AuthResult.loading());

        repository.fetchSeatsByMapId(seatMapId, new SeatRepository.OnSeatsLoadedListener() {
            @Override
            public void onLoaded(List<Seat> seats) {
                List<Seat> grid = buildGridFromSeats(seats);
                restoreSelectionState(grid);
                seatMapData.setValue(grid);
                loadState.setValue(AuthResult.success());
            }

            @Override
            public void onError(String error) {
                loadState.setValue(AuthResult.error(error));
            }
        });
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    /**
     * Sau khi grid được rebuild từ Firebase, đánh dấu lại isSelected
     * cho những ghế nằm trong selectedSeatById.
     *
     * Lý do cần: Firebase tạo đối tượng Seat mới (reference mới) mỗi lần đọc,
     * nên isSelected = false theo mặc định. Map này khớp theo seatId.
     */
    private void restoreSelectionState(List<Seat> grid) {
        if (selectedSeatById.isEmpty()) return;
        for (Seat seat : grid) {
            if (seat.getSeatId() != null && selectedSeatById.containsKey(seat.getSeatId())) {
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
     * Biến danh sách ghế thô (Firebase) thành lưới UI 7 cột.
     * Layout mỗi hàng: A | B | C | [số hàng - lối đi] | D | E | F
     */
    private List<Seat> buildGridFromSeats(List<Seat> rawSeats) {
        Map<String, Seat> seatMap = new HashMap<>();
        int maxRow = 0;

        for (Seat s : rawSeats) {
            seatMap.put(s.getRow() + s.getColumn(), s);
            if (s.getRow() > maxRow) maxRow = s.getRow();
        }

        List<Seat> grid = new ArrayList<>(maxRow * 7);

        for (int row = 1; row <= maxRow; row++) {
            int colIndex = 0;
            for (int i = 0; i < 7; i++) {
                if (i == AISLE_INDEX) {
                    Seat aisle = new Seat();
                    aisle.setType("AISLE");
                    aisle.setSeatNumber(String.valueOf(row));
                    grid.add(aisle);
                } else {
                    String key          = row + COLUMNS[colIndex];
                    Seat   firebaseSeat = seatMap.get(key);
                    if (firebaseSeat == null
                            || "BLOCKED".equalsIgnoreCase(firebaseSeat.getStatus())) {
                        Seat hidden = new Seat();
                        hidden.setType("HIDDEN");
                        grid.add(hidden);
                    } else {
                        grid.add(firebaseSeat);
                    }
                    colIndex++;
                }
            }
        }

        return grid;
    }
}