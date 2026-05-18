package com.example.flight_booking_app.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Chứa toàn bộ state của form tìm kiếm trên HomeFragment.
 * <p>
 * Thay vì 8 LiveData riêng → 1 LiveData<SearchQueryState>.
 * Khi bất kỳ field nào thay đổi → tạo object mới → setValue() 1 lần.
 * <p>
 * Immutable-style: mỗi thay đổi tạo bản sao mới qua các hàm withXxx().
 * Ưu điểm: DiffUtil, logging, undo dễ dàng vì object cũ vẫn còn nguyên.
 */
public class Searchquerystate {

    public final City fromCity;
    public final City toCity;
    public final String departDate;
    public final String returnDate;
    public final int adultCount;
    public final int childCount;
    public final int babyCount;
    public final String seatClass;
    public final boolean isRoundTrip;


    public Searchquerystate(City fromCity, City toCity,
                            String departDate, String returnDate,
                            int adultCount, int childCount, int babyCount,
                            String seatClass, boolean isRoundTrip) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departDate = departDate;
        this.returnDate = returnDate;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.babyCount = babyCount;
        this.seatClass = seatClass;
        this.isRoundTrip = isRoundTrip;
    }

    /**
     * Giá trị mặc định khi app mở lần đầu
     */
    public static Searchquerystate defaultState() {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
        return new Searchquerystate(
                new City("city_hn", "Hà Nội", "Sân bay quốc tế Nội Bài", "HAN"),
                new City("city_hcm", "TP. Hồ Chí Minh", "Sân bay quốc tế Tân Sơn Nhất", "SGN"),
                today, "",
                1, 0, 0,
                "Phổ thông",
                false
        );
    }

    // ── withXxx() — tạo bản sao với 1 field thay đổi
    // Giữ nguyên tất cả field khác, chỉ đổi field được chỉ định.

    public Searchquerystate withFromCity(City city) {
        return new Searchquerystate(city, toCity, departDate, returnDate,
                adultCount, childCount, babyCount, seatClass, isRoundTrip);
    }

    public Searchquerystate withToCity(City city) {
        return new Searchquerystate(fromCity, city, departDate, returnDate,
                adultCount, childCount, babyCount, seatClass, isRoundTrip);
    }

    public Searchquerystate withDepartDate(String date) {
        return new Searchquerystate(fromCity, toCity, date, returnDate,
                adultCount, childCount, babyCount, seatClass, isRoundTrip);
    }

    public Searchquerystate withReturnDate(String date) {
        return new Searchquerystate(fromCity, toCity, departDate, date,
                adultCount, childCount, babyCount, seatClass, isRoundTrip);
    }

    public Searchquerystate withPassenger(int adult, int child, int baby){
        return new Searchquerystate(fromCity, toCity, departDate, returnDate,
                adult,child,baby,seatClass,isRoundTrip);
    }
    public Searchquerystate withAdultCount(int adult) {
        return new Searchquerystate(fromCity, toCity, departDate, returnDate,
                adult, childCount, babyCount, seatClass, isRoundTrip);
    }
    public Searchquerystate withChildCount( int child) {
        return new Searchquerystate(fromCity, toCity, departDate, returnDate,
                adultCount, child, babyCount, seatClass, isRoundTrip);
    }
    public Searchquerystate withBabyCount(int baby) {
        return new Searchquerystate(fromCity, toCity, departDate, returnDate,
                adultCount, childCount, baby, seatClass, isRoundTrip);
    }

    public Searchquerystate withSeatClass(String seatClass) {
        return new Searchquerystate(fromCity, toCity, departDate, returnDate,
                adultCount, childCount, babyCount, seatClass, isRoundTrip);
    }

    public Searchquerystate withRoundTrip(boolean roundTrip) {
        return new Searchquerystate(fromCity, toCity, departDate, returnDate,
                adultCount, childCount, babyCount, seatClass, roundTrip);
    }

    /**
     * Hoán đổi điểm đi và điểm đến
     */
    public Searchquerystate withSwappedCities() {
        return new Searchquerystate(toCity, fromCity, departDate, returnDate,
                adultCount, childCount, babyCount, seatClass, isRoundTrip);
    }

}