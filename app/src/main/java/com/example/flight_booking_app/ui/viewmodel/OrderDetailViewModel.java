package com.example.flight_booking_app.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.repository.BookingRepository;
import com.example.flight_booking_app.data.repository.SeatRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailViewModel extends ViewModel {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    private final MutableLiveData<Booking> bookingLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState     = new MutableLiveData<>();

    private final MutableLiveData<Long>    countdownLive = new MutableLiveData<>();

    public LiveData<Booking> getBooking()       { return bookingLive;    }
    public LiveData<UiState> getUiState()       { return uiState;        }
    public LiveData<Long>    getCountdownLive() { return countdownLive;  }

    private final Handler  handler = new Handler(Looper.getMainLooper());
    private       Runnable expireRunnable;

    public OrderDetailViewModel() {
        bookingRepository = new BookingRepository();
        seatRepository    = new SeatRepository();
    }

   // quan sát realtime chi tiết đơn hàng theo id
    public void startObservingBooking(String bookingId) {
        uiState.setValue(UiState.loading());

        bookingRepository.observeBookingDetail(bookingId,
                new BookingRepository.OnBookingDetailLoadedCallback() {
                    @Override
                    public void onSuccess(Booking booking) {
                        uiState.postValue(UiState.success());
                        bookingLive.postValue(booking);

                        // gọi handleDeadlineCheck mỗi lần nhận data mới
                        handleDeadlineCheck(booking);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        uiState.postValue(UiState.error(errorMessage));
                    }
                });
    }

    // Cập nhật trạng thái đơn hàng
    public void updateBookingStatus(String bookingId, String newStatus) {
        bookingRepository.updateBookingStatus(bookingId, newStatus,
                new BookingRepository.OnStatusResultCallback() {
                    @Override public void onSuccess() { }
                    @Override public void onError(String error) {
                        uiState.postValue(UiState.error(error));
                    }
                });
    }


    // PRIVATE — logic đếm ngược / hết hạn
    private void handleDeadlineCheck(Booking booking) {
        // Chỉ xử lý khi đang chờ thanh toán
        if (!"RESERVATION_SUCCESS".equals(booking.getStatus())) {
            cancelCountdown(); // Status đã đổi (EXPIRED / PAID) → dừng countdown
            return;
        }

        if (booking.getPaymentDeadline() == null) return;

        long deadlineMs  = booking.getPaymentDeadline().toDate().getTime();
        long remainingMs = deadlineMs - System.currentTimeMillis();

        if (remainingMs <= 0) {
            // Mở lại app sau khi đã hết hạn → expire ngay lập tức
            expireBooking(booking);
        } else {
            // Còn hạn đặt hẹn giờ, đồng thời bắn countdown để UI hiển thị đồng hồ
            countdownLive.postValue(remainingMs);
            scheduleExpire(booking, remainingMs);
        }
    }

    // đặt cái hẹn giờ
    private void scheduleExpire(Booking booking, long delayMs) {
        cancelCountdown(); // Hủy hẹn giờ cũ trước khi đặt cái mới

        expireRunnable = () -> expireBooking(booking);
        handler.postDelayed(expireRunnable, delayMs);
    }

    // Hết hạn thanh toán
    private void expireBooking(Booking booking) {
        // 1. Đổi status → PAYMENT_EXPIRED trên Firestore
        //    Sau khi ghi xong, snapshot listener trong observeBookingDetail tự bắn lại
        //    → UI tự cập nhật mà không cần thêm code ở Activity
        bookingRepository.updateBookingStatus(
                booking.getBookingId(),
                "PAYMENT_EXPIRED",
                new BookingRepository.OnStatusResultCallback() {
                    @Override public void onSuccess() { }
                    @Override public void onError(String error) { }
                });

        // 2. Nhả ghế lượt đi
        List<String> outSeats = extractSeatNumbers(booking.getPassengers(), true);
        if (!outSeats.isEmpty()) {
            seatRepository.releaseSeats(
                    booking.getOutboundFlight().getFlightId(),
                    outSeats,
                    new SeatRepository.OnUpdateCallback() {
                        @Override public void onSuccess() { }
                        @Override public void onError(String e) { }
                    });
        }

        // 3. Nhả ghế lượt về (chỉ khứ hồi)
        if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
            List<String> retSeats = extractSeatNumbers(booking.getPassengers(), false);
            if (!retSeats.isEmpty()) {
                seatRepository.releaseSeats(
                        booking.getReturnFlight().getFlightId(),
                        retSeats,
                        new SeatRepository.OnUpdateCallback() {
                            @Override public void onSuccess() { }
                            @Override public void onError(String e) { }
                        });
            }
        }
    }

    // dọn dẹp khi hết hạn
    private void cancelCountdown() {
        if (expireRunnable != null) {
            handler.removeCallbacks(expireRunnable);
            expireRunnable = null;
        }
    }

    // Nó loại bỏ các ghế null, rỗng, và đặc biệt là lọc bỏ trường hợp em bé "Ngồi cùng ng.lớn"
    private List<String> extractSeatNumbers(List<Passenger> passengers, boolean isOutbound) {
        List<String> seats = new ArrayList<>();
        if (passengers == null) return seats;
        for (Passenger p : passengers) {
            String seat = isOutbound ? p.getOutboundSeat() : p.getReturnSeat();
            if (seat != null && !seat.isEmpty() && !seat.equals("Ngồi cùng ng.lớn")) {
                seats.add(seat);
            }
        }
        return seats;
    }

    // Dọn dẹp khi rời Activity
    @Override
    protected void onCleared() {
        super.onCleared();
        cancelCountdown();
        bookingRepository.removeObservers();
    }
}