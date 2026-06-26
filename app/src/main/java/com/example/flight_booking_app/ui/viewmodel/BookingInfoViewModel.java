package com.example.flight_booking_app.ui.viewmodel;


import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.Booking;
import com.example.flight_booking_app.data.model.FareClass;
import com.example.flight_booking_app.data.model.Flight;
import com.example.flight_booking_app.data.model.Passenger;
import com.example.flight_booking_app.data.model.UiState;
import com.example.flight_booking_app.data.repository.BookingRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class BookingInfoViewModel extends ViewModel {
    private final BookingRepository bookingRepository;
    private final MutableLiveData<Double> subTotalPriceLive = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();

    // lưu trạng thái trả về cho BookingInfo
    private final MutableLiveData<ArrayList<Passenger>> passengerListLive = new MutableLiveData<>();
    public LiveData<ArrayList<Passenger>> getPassengerListLive() { return passengerListLive; }

    public BookingInfoViewModel() {
        this.bookingRepository = new BookingRepository();
    }
    public LiveData<Double> getSubTotalPriceLive() { return subTotalPriceLive; }
    public LiveData<UiState> getUiState() { return uiState; }


    /**
     * Hàm này được gọi từ Activity. Nó chỉ tạo danh sách MỘT LẦN DUY NHẤT.
     * Nếu xoay màn hình, list đã có data rồi -> Bỏ qua không tạo lại.
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
                if ("BABY".equals(p.getType())) {
                    p.setSeatNumber("Ngồi cùng ng.lớn");
                    p.setSeatPrice(0); // Em bé không tốn tiền ghế
                    continue;
                }

                double seatTotalPrice = 0;
                StringBuilder seatDisplay = new StringBuilder();

                // Tính giá và hiển thị cho lượt đi
                if (departSeats != null && departIndex < departSeats.size()) {
                    seatDisplay.append(isRoundTrip ? "Đi: " : "").append(departSeats.get(departIndex));
                    if (departPrices != null) seatTotalPrice += departPrices.get(departIndex);
                    departIndex++;
                }

                // Tính giá và hiển thị cho lượt về
                if (isRoundTrip) {
                    if (returnSeats != null && returnIndex < returnSeats.size()) {
                        seatDisplay.append(" | Về: ").append(returnSeats.get(returnIndex));
                        if (returnPrices != null) seatTotalPrice += returnPrices.get(returnIndex);
                        returnIndex++;
                    }
                }

                p.setSeatNumber(seatDisplay.toString());
                p.setSeatPrice(seatTotalPrice);
            }

            passengerListLive.setValue(currentList);
            updateSubTotalPrice();
        }
    }
    public void updateSubTotalPrice(){
        ArrayList<Passenger> currentList = passengerListLive.getValue();

        double totalPrice=0 ;
        if (currentList != null) {
            for (Passenger p : currentList) {
                // ghế, hành lý
                totalPrice += p.getSeatPrice() + p.getOutboundBaggagePrice() + p.getReturnBaggagePrice();

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
                uiState.setValue(UiState.error("Vui lòng nhập đầy đủ thông tin hành khách!"));
                return false;
            }
        }

        return true;
    }


    // đặt vé giữ chỗ
    public void submitBooking(String currentUserId, Flight outFlight, FareClass outFare,
                              Flight retFlight, FareClass retFare, boolean isRoundTrip,
                              double totalAmount) {

        uiState.setValue(UiState.loading());

        Booking booking = new Booking();
        // 1. Tự sinh Mã hiển thị và set trạng thái
        booking.setOrderCode("DH" + System.currentTimeMillis());
        booking.setStatus("RESERVATION_SUCCESS");
        booking.setUserId(currentUserId);
        booking.setTotalAmount(totalAmount);

        // 2. Set Thời gian tạo và Thời gian hết hạn (+15 phút)
        booking.setCreatedAt(Timestamp.now());
        long deadlineMillis = System.currentTimeMillis() + (15 * 60 * 1000);
        booking.setPaymentDeadline(new Timestamp(new java.util.Date(deadlineMillis)));

        // 3. Phẳng hóa dữ liệu chuyến bay
        booking.setOutboundFlightId(outFlight.getFlightId());
        booking.setOutboundFareClassId(outFare.getFareClassId());
        booking.setDepartureCity(outFlight.getFrom());

        // Cần truyền đúng định dạng Date vào Timestamp (Bạn tự map với hàm lấy Date của Flight nhé)
        // booking.setDepartureTime(new Timestamp(outFlight.getDepartureDateObj()));

        if (isRoundTrip && retFlight != null) {
            booking.setReturnFlightId(retFlight.getFlightId());
            booking.setReturnFareClassId(retFare.getFareClassId());
            booking.setArrivalCity(retFlight.getFrom()); // Điểm đến của hành trình khứ hồi
            booking.setArrivalTime(retFlight.getArrivalTime());
        } else {
            booking.setReturnFlightId(null);
            booking.setReturnFareClassId(null);
            booking.setArrivalCity(outFlight.getTo()); // Điểm đến của 1 chiều
            booking.setArrivalTime(outFlight.getArrivalTime());
        }

        // 4. Lấy danh sách hành khách đã nhập vào
        booking.setPassengers(getPassengerListLive().getValue());

        // 5. Gọi Repository để lưu lên Firestore
        bookingRepository.createBookingWithNotification(booking, currentUserId, new BookingRepository.BookingCallback() {
            @Override
            public void onSuccess(String bookingId) {
                // Đẩy bookingId tự sinh lên cho Activity
                uiState.postValue(UiState.success());
            }

            @Override
            public void onFailure(String errorMessage) {
                uiState.postValue(UiState.error(errorMessage));
            }
        });
    }
}