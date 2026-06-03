package com.example.flight_booking_app.ui.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.Seat;
import com.example.flight_booking_app.data.model.SeatMapMetadata;
import com.example.flight_booking_app.data.repository.SeatRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Trách nhiệm:
 * 1. Tải danh sách ghế thô từ Firebase qua SeatRepository.
 * 2. Biến đổi thô → lưới UI (thêm AISLE, ẩn BLOCKED/HIDDEN).
 * 3. Giữ toàn bộ trạng thái chọn ghế QUA CẢ XOAY MÀN HÌNH:
 * - selectedSeats         : ghế đang chọn trong tab hiện tại (tạm thời)
 * - selectedOutboundSeats : danh sách ghế đã chốt lượt đi (khứ hồi)
 * - isSelectingReturn     : đang chọn tab nào
 * 4. currentlyViewingSeatLive CHỈ phát khi số ghế chưa đủ maxPassengers.
 * Khi đã đủ ghế, label bottom bar hiển thị cố định từ selectedSeats.
 * 5. Khi grid được rebuild (loadSeatMap), khôi phục isSelected trên các
 * đối tượng Seat mới để UI hiển thị đúng màu.
 * <p>
 * Lưới A320/A321: 7 cột/hàng = A B C | aisle | D E F
 */
public class SeatViewModel extends ViewModel {

    private static final String[] COLUMNS = {"A", "B", "C", "D", "E", "F"};
    private static final int AISLE_INDEX = 3;

    private final SeatRepository repository;

    private final MutableLiveData<List<Seat>> seatMapData = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> loadState = new MutableLiveData<>();

    /**
     * Ghế vừa được click — Activity observe để cập nhật label realtime.
     * CHỈ phát giá trị khi số ghế đang chọn CHƯA đủ maxPassengers,
     * hoặc khi bỏ chọn một ghế đang được chọn.
     * Khi đã đủ ghế, Activity dùng updateBottomBar() thay vì LiveData này.
     */
    private final MutableLiveData<Seat> currentlyViewingSeatLive = new MutableLiveData<>();

    // XỬ LÝ SỐ CỘT ĐỘNG (SPAN COUNT)
    private final MutableLiveData<Integer> gridSpanCountLive = new MutableLiveData<>();

    public LiveData<Integer> getGridSpanCountLive() {
        return gridSpanCountLive;
    }

    /**
     * Map seatId → Seat của những ghế ĐANG chọn trong tab hiện tại.
     * Dùng để khôi phục isSelected khi grid được rebuild từ Firebase.
     */
    private final Map<String, Seat> selectedSeatById = new HashMap<>();

    /**
     * Ghế đang chọn trong tab hiện tại (tạm thời, xoá khi chuyển tab).
     */
    private final List<Seat> selectedSeats = new ArrayList<>();

    /**
     * Ghế đã chốt lượt ĐI (chỉ dùng cho khứ hồi).
     * Được set bởi Activity khi bấm TIẾP TỤC ở lượt đi trước khi chuyển tab.
     * Không bị xoá bởi clearCurrentSelections().
     */
    private List<Seat> selectedOutboundSeats = new ArrayList<>();

    private boolean isSelectingReturn = false;

    public SeatViewModel() {
        this.repository = new SeatRepository();
    }

    // quan sát lưới ghế

    public LiveData<List<Seat>> getSeatMapData() {
        return seatMapData;
    }

    // trạng thái loading
    public LiveData<AuthResult> getIsLoading() {
        return loadState;
    }


    // quan sát ds trạng thái ghế đã chọn
    public LiveData<Seat> getCurrentlyViewingSeatLive() {
        return currentlyViewingSeatLive;
    }

    // Lưu danh sách các ghế đang chọn hiện tại để tính toán (đã đủ số lượng chưa).
    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }

    /**
     * Lấy danh sách ghế đã chốt lượt đi.
     * Trả về list rỗng (không null) nếu chưa set.
     */
    public List<Seat> getSelectedOutboundSeats() {
        return selectedOutboundSeats;
    }

    /**
     * Lưu toàn bộ danh sách ghế lượt đi sau khi người dùng bấm TIẾP TỤC.
     * Activity truyền vào snapshot của selectedSeats hiện tại.
     */
    public void setSelectedOutboundSeats(List<Seat> seats) {
        this.selectedOutboundSeats = new ArrayList<>(seats);
    }

    // trạng thái đã chọn sang tab lượt về chưa
    public boolean isSelectingReturn() {
        return isSelectingReturn;
    }

    public void setSelectingReturn(boolean v) {
        this.isSelectingReturn = v;
    }

    /**
     * Phát sự kiện ghế đang xem — CHỈ khi chưa đủ ghế hoặc đang bỏ chọn.
     * <p>
     * Logic:
     * - Đang bỏ chọn (seat.isSelected() == true) → luôn phát để label cập nhật
     * - Chưa đủ ghế (selectedSeats.size() < max)  → phát để hiển thị ghế đang hover
     * - Đã đủ ghế và click ghế MỚI               → KHÔNG phát, tránh label nhảy
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

    /**
     * Tải sơ đồ ghế từ Firebase.
     * Có thể gọi nhiều lần (khi chuyển tab Đi ↔ Về).
     * Sau khi grid xây xong, khôi phục trạng thái isSelected từ selectedSeatById.
     */

    public void loadSeatMap(String templateId, String flightId) {
        if (templateId == null || templateId.isEmpty() || flightId == null || flightId.isEmpty()) return;

        loadState.setValue(AuthResult.loading());

        repository.fetchSeatsForFlight(templateId, flightId, new SeatRepository.OnSeatsLoadedListener() {
            @Override
            public void onLoaded(List<Seat> seats, SeatMapMetadata metadata) {

                List<Seat> grid = buildGridFromSeats(seats, metadata);
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
     * <p>
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
     * Biến danh sách ghế thành mảng 2 chiều với  lưới UI 7 cột.
     * Layout mỗi hàng: A | B | C | [số hàng - lối đi] | D | E | F
     */
    private List<Seat> buildGridFromSeats(List<Seat> rawSeats, SeatMapMetadata metadata) {
        Map<String, Seat> seatMap = new HashMap<>();
        int maxRow = 0;

        for (Seat s : rawSeats) {
            seatMap.put(s.getRow() + s.getColumn(), s);
            if (s.getRow() > maxRow) maxRow = s.getRow();
        }

        // postValue(): Có thể gọi ở bất kỳ luồng nào
        gridSpanCountLive.postValue(metadata.spanCount); // Báo cho Activity biết số cột

        List<Seat> grid = new ArrayList<>(maxRow * metadata.spanCount);

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

                    if (firebaseSeat == null || "BLOCKED".equalsIgnoreCase(firebaseSeat.getStatus())) {
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