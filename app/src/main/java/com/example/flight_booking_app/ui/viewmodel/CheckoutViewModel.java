//package com.example.flight_booking_app.ui.viewmodel;
//
//import com.example.flight_booking_app.data.model.Booking;
//import com.example.flight_booking_app.data.model.Flight;
//
//public class CheckoutViewModel {
//
//
//    public void submitBooking(
//            String currentUserId,
//            Flight outboundFlight, Fare outboundFare, List<String> outboundSeats,
//            Flight returnFlight, Fare returnFare, List<String> returnSeats,
//            boolean isRoundTrip, double totalPayment,
//            String fullName, String email, String phone) {
//
//        // 1. TẠO OBJECT BOOKING TẠM THỜI
//        Booking newBooking = createBookingObject(...); // Hàm gói dữ liệu của bạn
//        newBooking.setStatus("RESERVATION_SUCCESS");   // Mặc định là chờ thanh toán
//
//        // 2. KHÓA GHẾ LƯỢT ĐI TRƯỚC
//        seatRepository.reserveMultipleSeatsWithTransaction(outboundFlight.getId(), outboundSeats, currentUserId, new SeatRepository.OnUpdateCallback() {
//            @Override
//            public void onSuccess() {
//
//                // 3. NẾU LÀ VÉ KHỨ HỒI -> KHÓA TIẾP GHẾ LƯỢT VÊ
//                if (isRoundTrip && returnFlight != null) {
//                    seatRepository.reserveMultipleSeatsWithTransaction(returnFlight.getId(), returnSeats, currentUserId, new SeatRepository.OnUpdateCallback() {
//                        @Override
//                        public void onSuccess() {
//                            // Khóa 2 chiều thành công -> Tạo Booking
//                            saveBookingToFirestore(newBooking);
//                        }
//
//                        @Override
//                        public void onError(String error) {
//                            // LỖI: Khóa chiều đi thành công nhưng chiều về bị cướp mất
//                            // BẮT BUỘC ROLLBACK: Nhả ghế chiều đi ra
//                            seatRepository.releaseSeats(outboundFlight.getId(), outboundSeats, /* callback */);
//                            uiState.setValue(UiState.error("Ghế chuyến về vừa có người đặt. Vui lòng chọn lại!"));
//                        }
//                    });
//                } else {
//                    // Nếu là vé một chiều -> Khóa xong chiều đi là Tạo Booking luôn
//                    saveBookingToFirestore(newBooking);
//                }
//            }
//
//            @Override
//            public void onError(String error) {
//                // Lỗi ngay từ chuyến đi
//                uiState.setValue(UiState.error("Ghế chuyến đi vừa có người đặt. Vui lòng chọn lại!"));
//            }
//        });
//    }
//
//    // Hàm đẩy lên Firestore sau khi ghế đã được khóa an toàn
//    private void saveBookingToFirestore(Booking newBooking) {
//        bookingRepository.createBooking(newBooking, new BookingRepository.OnResultCallback() {
//            @Override
//            public void onSuccess() {
//                uiState.setValue(UiState.success()); // Báo cho UI tắt Dialog và chuyển màn hình
//            }
//
//            @Override
//            public void onError(String error) {
//                uiState.setValue(UiState.error("Lỗi tạo đơn hàng: " + error));
//                // Cẩn thận hơn: Nếu lỗi tạo đơn, gọi hàm releaseSeats để nhả ghế ra
//            }
//        });
//    }
//}
