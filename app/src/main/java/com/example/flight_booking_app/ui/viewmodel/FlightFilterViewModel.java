package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.FlightFilterState;

import java.util.ArrayList;
import java.util.List;

public class FlightFilterViewModel extends ViewModel {

    // Lưu trữ trạng thái bộ lọc đang thao tác tạm thời trên BottomSheet
    private final MutableLiveData<FlightFilterState> filterState = new MutableLiveData<>(FlightFilterState.defaultState());

    // Sự kiện dùng để lưu và gửi dữ liệu FlightFilterState sang cho SearchFlightActivity
    private final MutableLiveData<FlightFilterState> applyFilterEvent = new MutableLiveData<>();

    public LiveData<FlightFilterState> getFilterState() {
        return filterState;
    }

    public LiveData<FlightFilterState> getApplyFilterEvent() {
        return applyFilterEvent;
    }

    public interface StateUpdater {
        FlightFilterState apply(FlightFilterState current);
    }

    // Hàm helper cập nhật trạng thái bất biến (Immutable style)
    private void update(StateUpdater updater) {
        FlightFilterState current = filterState.getValue();
        if (current == null) current = FlightFilterState.defaultState();
        filterState.setValue(updater.apply(current));
    }

    // ─── Các hàm cập nhật từng trạng thái thành phần ──────────────────

    public void setShowFullPrice(boolean showFullPrice) {
        update(current -> current.withShowFullPrice(showFullPrice));
    }

    public void setSortMode(String sortMode) {
        update(current -> current.withSortMode(sortMode));
    }

    public void setSelectedAirlines(List<String> selectedAirlines) {
        update(current -> current.withSelectedAirlines(selectedAirlines));
    }

    public void setTimeSlotAtIndex(int index, boolean isSelected) {
        update(current -> {
            List<Boolean> updatedSlots = new ArrayList<>(current.timeSlots);
            if (index >= 0 && index < updatedSlots.size()) {
                updatedSlots.set(index, isSelected);
            }
            return current.withTimeSlots(updatedSlots);
        });
    }

    public void setSeatClass(String seatClass) {
        update(current -> current.withSeatClass(seatClass));
    }

    public void resetToDefault() {
        filterState.setValue(FlightFilterState.defaultState());
    }

    /**
     * Hàm kích hoạt gửi dữ liệu sang cho SearchFlightActivity xử lý
     */
    public void applyFilter() {
        FlightFilterState currentState = filterState.getValue();
        if (currentState != null) {
            applyFilterEvent.setValue(currentState);
        }
    }
}