package com.example.flight_booking_app.ui.viewmodel;




import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.repository.BookingRepository;
import com.example.flight_booking_app.data.repository.SeatRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class BookingViewModel extends ViewModel {
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    private final MutableLiveData<Double> subTotalPriceLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> loadState = new MutableLiveData<>();
    private final MutableLiveData<String> bookingIdLive = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> bookingListLive = new MutableLiveData<>();
    public LiveData<List<Booking>> getBookingListLive() {
        return bookingListLive;
    }


    public LiveData<UiState> getLoadState() {
        return loadState;
    }
    public LiveData<String> getBookingIdLive() { return bookingIdLive; }

    // lưu trạng thái trả về cho BookingInfo
    private final MutableLiveData<ArrayList<Passenger>> passengerListLive = new MutableLiveData<>();
    public LiveData<ArrayList<Passenger>> getPassengerListLive() { return passengerListLive; }

    public BookingViewModel() {
        this.bookingRepository = new BookingRepository();
        this.seatRepository = new SeatRepository();
    }
    public LiveData<Double> getSubTotalPriceLive() { return subTotalPriceLive; }


    /**
     * Hàm này được gọi từ Activity. Nó chỉ tạo danh sách MỘT LẦN DUY NHẤT.
     * Nếu xoay màn hình, list đã có data rồi  Bỏ qua không tạo lại.
     */
    public void initPassengersIfNeeded(int adultCount, int childCount, int babyCount) {
        if (passengerListLive.getValue() == null) {
            ArrayList<Passenger> list = new ArrayList<>();
            for (int idx = 0; idx < adultCount; idx++) {
                list.add(new Passenger("ADULT", idx, "Người lớn " + (idx + 1)));
            }
            for (int idx = 0; idx < childCount; idx++) {
                list.add(new Passenger("CHILD", idx, "Trẻ em " + (idx + 1)));
            }
            for (int idx = 0; idx < babyCount; idx++) {
                list.add(new Passenger("BABY", idx, "Em bé " + (idx + 1)));
            }
            passengerListLive.setValue(list);
        }
    }

    /**
     * Cập nhật thông tin 1 hành khách khi nhập liệu xong
     */
    public void updatePassenger(Passenger updatedPassenger) {
        ArrayList<Passenger> currentList = passengerListLive.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                Passenger p = currentList.get(i);
                if (p.getType().equals(updatedPassenger.getType()) && p.getIndex() == updatedPassenger.getIndex()) {
                    currentList.set(i, updatedPassenger);
                    break;
                }
            }
            // Kích hoạt báo cho UI vẽ lại
            passengerListLive.setValue(currentList);
            // Cập nhật giá tiền ghế
            updateSubTotalPrice();
        }
    }

    /**
     * Lưu ghế vừa chọn và tiến hành gán luôn ghế cho từng hành khách trong ViewModel
     */
    public void updateSeats(ArrayList<String> departSeats, ArrayList<Double> departPrices,
                            ArrayList<String> returnSeats, ArrayList<Double> returnPrices,
                            boolean isRoundTrip) {

        ArrayList<Passenger> currentList = passengerListLive.getValue();
        if (currentList != null) {
            int departIndex = 0;
            int returnIndex = 0;

            for (Passenger p : currentList) {
                // Trường hợp là Em bé (BABY)
                if ("BABY".equals(p.getType())) {
                    p.setOutboundSeat("Ngồi cùng ng.lớn");
                    p.setOutboundSeatPrice(0);
                    if (isRoundTrip) {
                        p.setReturnSeat("Ngồi cùng ng.lớn");
                        p.setReturnSeatPrice(0);
                    }
                    continue;
                }

                // 1. Gán dữ liệu Lượt đi (Depart)
                if (departSeats != null && departIndex < departSeats.size()) {
                    p.setOutboundSeat(departSeats.get(departIndex));
                    p.setOutboundSeatPrice(departPrices != null ? departPrices.get(departIndex) : 0.0);
                    departIndex++;
                } else {
                    p.setOutboundSeat("");
                    p.setOutboundSeatPrice(0.0);
                }

                // 2. Gán dữ liệu Lượt về (Return) nếu là khứ hồi
                if (isRoundTrip) {
                    if (returnSeats != null && returnIndex < returnSeats.size()) {
                        p.setReturnSeat(returnSeats.get(returnIndex));
                        p.setReturnSeatPrice(returnPrices != null ? returnPrices.get(returnIndex) : 0.0);
                        returnIndex++;
                    } else {
                        p.setReturnSeat("");
                        p.setReturnSeatPrice(0.0);
                    }
                } else {
                    p.setReturnSeat(null);
                    p.setReturnSeatPrice(0.0);
                }
            }

            passengerListLive.setValue(currentList);
            updateSubTotalPrice();
        }
    }
    public void updateSubTotalPrice() {
        ArrayList<Passenger> currentList = passengerListLive.getValue();

        double totalPrice = 0;
        if (currentList != null) {
            for (Passenger p : currentList) {
                totalPrice += p.getOutboundSeatPrice()
                        + p.getReturnSeatPrice()
                        + p.getOutboundBaggagePrice()
                        + p.getReturnBaggagePrice();
            }
            subTotalPriceLive.setValue(totalPrice);
        }
    }

    //Validate
    public boolean validateInfo(String fullName, String email, String phone) {
        if (fullName.isEmpty()) {
            return false; // Dừng lại, báo lỗi
        }
        if (email.isEmpty()) {
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        if (phone.isEmpty()) {
            return false;
        }
        if (!phone.startsWith("0") || phone.length() != 10) {
            return false;
        }

        ArrayList<Passenger> passengerList = passengerListLive.getValue();
        for (Passenger p : passengerList) {
            if (!p.isComplete()) {
                loadState.setValue(UiState.error("Vui lòng nhập đầy đủ thông tin hành khách!"));
                return false;
            }
        }

        return true;
    }


    // đặt vé giữ chỗ
    public void submitBooking(String currentUserId,
                              Flight outFlight, FareClass outFare, List<String> outboundSeats,
                              Flight retFlight, FareClass retFare, List<String> returnSeats,
                              boolean isRoundTrip, double totalAmount,
                              String contactName, String contactEmail, String contactPhone) {

        // Bật vòng xoay loading
        loadState.setValue(UiState.loading());

        // Tạo object Booking
        Booking booking = new Booking();
        booking.setOrderCode("DH" + System.currentTimeMillis());
        booking.setUserId(currentUserId);
        booking.setTotalAmount(totalAmount);

        booking.setCreatedAt(Timestamp.now());
        long deadlineMillis = System.currentTimeMillis() + (1 * 60 * 1000); // +15 phút
        booking.setPaymentDeadline(new Timestamp(new java.util.Date(deadlineMillis)));

        booking.setOutboundFlight(outFlight);
        booking.setOutboundFare(outFare);

        booking.setRoundTrip(isRoundTrip);
        if (isRoundTrip && retFlight != null && retFare != null) {
            booking.setReturnFlight(retFlight);
            booking.setReturnFare(retFare);
        }

        booking.setPassengers(getPassengerListLive().getValue());
        booking.setContactName(contactName);
        booking.setContactEmail(contactEmail);
        booking.setContactPhone(contactPhone);

        // Giữ ghế
        seatRepository.reserveMultipleSeatsWithTransaction(outFlight.getFlightId(), outboundSeats, currentUserId, new SeatRepository.OnUpdateCallback() {
            @Override
            public void onSuccess() {

                // nếu có khứ hồi
                if (isRoundTrip && retFlight != null) {
                    seatRepository.reserveMultipleSeatsWithTransaction(retFlight.getFlightId(), returnSeats, currentUserId, new SeatRepository.OnUpdateCallback() {
                        @Override
                        public void onSuccess() {
                            booking.setStatus("RESERVATION_SUCCESS");
                            saveBookingToFirestore(booking, currentUserId, true, "");
                        }

                        @Override
                        public void onError(String error) {
                            // LỖI: Chiều về bị cướp mất ghế -> ROLLBACK nhả ghế chiều đi ra
                            seatRepository.releaseSeats(outFlight.getFlightId(), outboundSeats, new SeatRepository.OnUpdateCallback() {
                                @Override public void onSuccess() {}
                                @Override public void onError(String e) {}
                            });

                            // Vẫn lưu đơn hàng nhưng với trạng thái FAILED
                            booking.setStatus("RESERVATION_FAILED");
                            saveBookingToFirestore(booking, currentUserId, false, "Ghế chuyến về vừa có người đặt. Vui lòng chọn lại!");
                        }
                    });
                } else {
                    // Nếu 1 chiều
                    booking.setStatus("RESERVATION_SUCCESS");
                    saveBookingToFirestore(booking, currentUserId, true, "");
                }
            }

            @Override
            public void onError(String error) {
                // Lỗi ngay từ chuyến đi  Vẫn lưu đơn hàng nhưng với trạng thái FAILED
                booking.setStatus("RESERVATION_FAILED");
                saveBookingToFirestore(booking, currentUserId, false, "Ghế chuyến đi vừa có người đặt. Vui lòng chọn lại!");
            }
        });
    }

    /**
     * Hàm Helper gọi Repository để lưu lên Firestore
     */
    private void saveBookingToFirestore(Booking booking, String currentUserId, boolean isReservationSuccess, String errorMessage) {
        bookingRepository.createBookingWithNotification(booking, currentUserId, new BookingRepository.BookingIdCallback() {
            @Override
            public void onSuccess(String bookingId) {
                if (isReservationSuccess) {
                    // id sang OrderDetail và báo Success
                    bookingIdLive.postValue(bookingId);
                    loadState.postValue(UiState.success());
                } else {
                    // Đã lưu lịch sử đơn hàng FAILED lên hệ thống thành công.
                    loadState.postValue(UiState.error(errorMessage));
                }
            }

            @Override
            public void onFailure(String errorMessageFromRepo) {
                // Lỗi hệ thống khi lưu DB (vd: rớt mạng)
                loadState.postValue(UiState.error("Lỗi tạo đơn hàng: " + errorMessageFromRepo));
            }
        });
    }

    public void startListening(String userId) {
        loadState.setValue(UiState.loading());

        if (userId == null || userId.trim().isEmpty() || "GUEST_USER".equals(userId)) {
            loadState.setValue(UiState.error("Chưa đăng nhập hoặc tài khoản không hợp lệ"));
            return;
        }
        bookingRepository.observeBookingList(userId, new BookingRepository.OnBookingListLoadedCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                // Tự động expire các đơn hàng hết hạn trước khi đẩy lên UI
                autoExpireIfNeeded(bookings);
                bookingListLive.postValue(bookings);
                loadState.postValue(UiState.success());
            }
            @Override
            public void onFailure(String errorMessage) {
                loadState.postValue(UiState.error(errorMessage));
            }
        });
    }

    // tự động nhả ghế khi hết hạn
    private void autoExpireIfNeeded(List<Booking> bookings) {
        long now = System.currentTimeMillis();
        for (Booking b : bookings) {
            if (!"RESERVATION_SUCCESS".equals(b.getStatus())) continue;
            if (b.getPaymentDeadline() == null) continue;
            if (b.getPaymentDeadline().toDate().getTime() > now) continue;

            // 1. Cập nhật status booking
            bookingRepository.updateBookingStatus(
                    b.getBookingId(),
                    "PAYMENT_EXPIRED",
                    new BookingRepository.OnStatusResultCallback() {
                        @Override public void onSuccess() { }
                        @Override public void onError(String e) { }
                    });

            // 2. Xóa ghế lượt đi khỏi flightSeats
            List<String> outSeats = extractSeatNumbers(b.getPassengers(), true);
            if (!outSeats.isEmpty()) {
                seatRepository.releaseSeats(
                        b.getOutboundFlight().getFlightId(),
                        outSeats,
                        new SeatRepository.OnUpdateCallback() {
                            @Override public void onSuccess() { }
                            @Override public void onError(String e) { }
                        });
            }

            // 3. Xóa ghế lượt về khỏi flightSeats (nếu khứ hồi)
            if (b.isRoundTrip() && b.getReturnFlight() != null) {
                List<String> retSeats = extractSeatNumbers(b.getPassengers(), false);
                if (!retSeats.isEmpty()) {
                    seatRepository.releaseSeats(
                            b.getReturnFlight().getFlightId(),
                            retSeats,
                            new SeatRepository.OnUpdateCallback() {
                                @Override public void onSuccess() { }
                                @Override public void onError(String e) { }
                            });
                }
            }
        }
    }

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


}