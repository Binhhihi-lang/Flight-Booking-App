package com.example.flight_booking_app.data.model;


/**
 * Chứa toàn bộ state của form tìm kiếm trên HomeFragment.
 * <p>
 * Thay vì 8 LiveData riêng → 1 LiveData<SearchQueryState>.
 * Khi bất kỳ field nào thay đổi → tạo object mới → setValue() 1 lần.
 * <p>
 * Immutable-style: mỗi thay đổi tạo bản sao mới qua các hàm withXxx().
 * Ưu điểm: DiffUtil, logging, undo dễ dàng vì object cũ vẫn còn nguyên.
 */
public class SearchQueryState {
    public final City fromCity;
    public final City toCity;
    public final Long departDateMillis;
    public final Long returnDateMillis;

    public final int adultCount;

    public final int childCount;
    public final int babyCount;
    public final boolean isRoundTrip;

    public SearchQueryState(City fromCity, City toCity,
                            Long departDateMillis, Long returnDateMillis,
                            int adultCount, int childCount, int babyCount, boolean isRoundTrip) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departDateMillis = departDateMillis;
        this.returnDateMillis = returnDateMillis;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.babyCount = babyCount;
        this.isRoundTrip = isRoundTrip;
    }

    // set dữ liệu đầu vào
    public static SearchQueryState defaultState() {
        // Mặc định lấy thời gian hiện tại làm ngày đi
        long todayMillis = System.currentTimeMillis();
        return new SearchQueryState(
                new City("HAN", "Hà Nội", "Sân bay quốc tế Nội Bài", "HAN"),
                new City("SGN", "TP. Hồ Chí Minh", "Sân bay quốc tế Tân Sơn Nhất", "SGN"),
                todayMillis, null, // Mặc định một chiều nên ngày về là null
                1, 0, 0,
                false
        );
    }



    // ── withXxx() — tạo bản sao với 1 field thay đổi
    // Giữ nguyên tất cả field khác, chỉ đổi field được chỉ định.

    public SearchQueryState withFromCity(City city) {
        return new SearchQueryState(city, toCity, departDateMillis, returnDateMillis,
                adultCount, childCount, babyCount, isRoundTrip);

    }
    public SearchQueryState withToCity(City city) {
        return new SearchQueryState(fromCity, city, departDateMillis, returnDateMillis,
                adultCount, childCount, babyCount, isRoundTrip);
    }

    public SearchQueryState withDepartDate(Long dateMillis) {
        return new SearchQueryState(fromCity, toCity, dateMillis, returnDateMillis, adultCount, childCount, babyCount, isRoundTrip);
    }

    public SearchQueryState withReturnDate(Long dateMillis) {
        return new SearchQueryState(fromCity, toCity, departDateMillis, dateMillis, adultCount, childCount, babyCount, isRoundTrip);
    }

    public SearchQueryState withPassenger(int adult, int child, int baby){
        return new SearchQueryState(fromCity, toCity, departDateMillis, returnDateMillis,
                adult,child,baby,isRoundTrip);
    }

    public SearchQueryState withRoundTrip(boolean roundTrip) {
        return new SearchQueryState(fromCity, toCity, departDateMillis, returnDateMillis,
                adultCount, childCount, babyCount, roundTrip);
    }

    /**
     * Hoán đổi điểm đi và điểm đến
     */
    public SearchQueryState withSwappedCities() {
        return new SearchQueryState(toCity, fromCity, departDateMillis, returnDateMillis,
                adultCount, childCount, babyCount, isRoundTrip);
    }

}