    package com.example.flight_booking_app.data.repository;

    import androidx.annotation.NonNull;

    import com.example.flight_booking_app.data.model.Aircraft;
    import com.example.flight_booking_app.data.model.Airline;
    import com.example.flight_booking_app.data.model.City;
    import com.example.flight_booking_app.data.model.FareClass;
    import com.example.flight_booking_app.data.model.Flight;
    import com.example.flight_booking_app.data.model.FareOption;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    /**
     * FlightRepository - Tìm kiếm chuyến bay với đầy đủ tiêu chí.
     * LỌC THEO:
     *   1. fromCityId, toCityId (điểm đi/đến)
     *   2. departureDate (ngày bay)
     *   3. seatType (ECONOMY / BUSINESS) - lọc qua fareOptions
     *   4. totalPassengers (kiểm tra availableSeats)
     * JOIN:
     *   - Cities: cityName, iataCode
     *   - Airlines: name, logo
     *   - FareClasses: để xác định seatType của fareOptions
     * LOGIC GIÁ:
     *   - displayPrice = giá RẺ NHẤT trong fareOptions phù hợp với seatType
     *   - Ví dụ: User chọn "Phổ thông" → lấy giá rẻ nhất trong các gói ECONOMY
     */
    public class FlightRepository {

        private final DatabaseReference dbFlights;
        private final DatabaseReference dbCities;
        private final DatabaseReference dbAirlines;
        private final DatabaseReference dbAircrafts;
        private final DatabaseReference dbFareClasses;

        // Cache
        private Map<String, City> citiesMap = new HashMap<>();
        private Map<String, Airline> airlinesMap = new HashMap<>();
        private Map<String, FareClass> fareClassesMap = new HashMap<>();

        private Map<String, Aircraft> aircraftsMap = new HashMap<>();

        public FlightRepository() {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            dbFlights     = db.getReference("Flights");
            dbCities      = db.getReference("Cities");
            dbAirlines    = db.getReference("Airlines");
            dbFareClasses = db.getReference("FareClasses");
            dbAircrafts   = db.getReference("Aircrafts");
        }

        public interface OnFlightsLoaded {
            void onLoaded(List<Flight> flights);
            void onError(String error);
        }

        // tìm kiếm chuyến bay theo trang HOME
        public void searchFlights(String fromCityId, String toCityId, String departureDate,
                                  int totalPassengers, OnFlightsLoaded callback) {

            // Load cache trước
            loadAllCaches(() -> {
                // Query Flights từ Firebase
                queryAndFilterFlights(fromCityId, toCityId, departureDate, totalPassengers, callback);
            });
        }

        /**
         * Load tất cả cache cần thiết theo thứ tự nối tiếp nhau (Chain of Callbacks).
         * Cities -> Airlines -> FareClasses -> Aircrafts
         */
        private void loadAllCaches(Runnable onComplete) {

            // Bước 4: Load Aircrafts xong thì chạy onComplete (Bắt đầu Query chuyến bay)
            Runnable loadAircraftsStep = () -> {
                if (aircraftsMap.isEmpty()) {
                    loadAircraftsCache(onComplete);
                } else {
                    onComplete.run();
                }
            };

            // Bước 3: Load FareClasses xong thì gọi Bước 4
            Runnable loadFareClassesStep = () -> {
                if (fareClassesMap.isEmpty()) {
                    loadFareClassesCache(loadAircraftsStep);
                } else {
                    loadAircraftsStep.run();
                }
            };

            // Bước 2: Load Airlines xong thì gọi Bước 3
            Runnable loadAirlinesStep = () -> {
                if (airlinesMap.isEmpty()) {
                    loadAirlinesCache(loadFareClassesStep);
                } else {
                    loadFareClassesStep.run();
                }
            };

            // Bước 1: Bắt đầu từ Load Cities, xong thì gọi Bước 2
            if (citiesMap.isEmpty()) {
                loadCitiesCache(loadAirlinesStep);
            } else {
                loadAirlinesStep.run();
            }
        }

        private void loadAircraftsCache(Runnable onComplete) {
            dbAircrafts.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    aircraftsMap.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Aircraft aircraft = child.getValue(Aircraft.class);
                        if (aircraft != null && aircraft.getAirCraftId() != null) {
                            aircraftsMap.put(aircraft.getAirCraftId(), aircraft);
                        }
                    }
                    onComplete.run();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    onComplete.run();
                }
            });
        }

        private void loadCitiesCache(Runnable onComplete) {
            dbCities.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    citiesMap.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        City city = child.getValue(City.class);
                        if (city != null && city.getCityId() != null) {
                            citiesMap.put(city.getCityId(), city);
                        }
                    }
                    onComplete.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    onComplete.run();
                }
            });
        }

        private void loadAirlinesCache(Runnable onComplete) {
            dbAirlines.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    airlinesMap.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Airline airline = child.getValue(Airline.class);
                        if (airline != null && airline.getAirlineId() != null) {
                            airlinesMap.put(airline.getAirlineId(), airline);
                        }
                    }
                    onComplete.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    onComplete.run();
                }
            });
        }

        private void loadFareClassesCache(Runnable onComplete) {
            dbFareClasses.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    fareClassesMap.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        FareClass fc = child.getValue(FareClass.class);
                        if (fc != null && fc.getFareClassId() != null) {
                            fareClassesMap.put(fc.getFareClassId(), fc);
                        }
                    }
                    onComplete.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    onComplete.run();
                }
            });
        }

        /**
         * Query Flights và lọc theo tất cả tiêu chí.
         */
        private void queryAndFilterFlights(String fromCityId, String toCityId, String departureDate,
                                           int totalPassengers, OnFlightsLoaded callback) {

            // Query server-side theo fromCityId
            Query query = dbFlights.orderByChild("fromCityId").equalTo(fromCityId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Flight> result = new ArrayList<>();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        Flight originalFlight = child.getValue(Flight.class);
                        if (originalFlight == null) continue;

                        // 1. Lọc theo điểm đến, ngày bay, số ghế trống (Giữ nguyên như cũ)
                        if (!toCityId.equals(originalFlight.getToCityId())) continue;
                        if (departureDate != null && !departureDate.isEmpty()) {
                            if (!departureDate.equals(originalFlight.getDepartureDate())) continue;
                        }
                        if (originalFlight.getAvailableSeats() < totalPassengers) continue;
                        if (!"ON_TIME".equalsIgnoreCase(originalFlight.getStatus())) continue;

                        List<FareOption> fareOptions = originalFlight.getFareOptions();
                        if (fareOptions == null || fareOptions.isEmpty()) continue;

                        // ══════════════════════════════════════════════════════════════════════
                        // 2. LOGIC MỚI: DUYỆT TỪNG TÙY CHỌN VÉ VÀ TẠO RA CÁC THẺ ĐỘC LẬP
                        // ══════════════════════════════════════════════════════════════════════
                        for (FareOption option : fareOptions) {
                            // Kiểm tra gói vé này còn chỗ không
                            if (!option.isAvailable()) continue;

                            // Lấy lại dữ liệu từ child.getValue() để tạo ra một bản sao (clone)
                            // độc lập của chuyến bay này, tránh việc tham chiếu đè dữ liệu lên nhau.
                            Flight flightCard = child.getValue(Flight.class);

                            // --- JOIN THÔNG TIN CITY / AIRLINE ---
                            City fromCity = citiesMap.get(flightCard.getFromCityId());
                            City toCity   = citiesMap.get(flightCard.getToCityId());

                            if (fromCity != null) {
                                flightCard.setFrom(fromCity.getCityName());
                                flightCard.setFromIata(fromCity.getIataCode());
                            }
                            if (toCity != null) {
                                flightCard.setTo(toCity.getCityName());
                                flightCard.setToIata(toCity.getIataCode());
                            }

                            Airline airline = airlinesMap.get(flightCard.getAirlineId());
                            if (airline != null) {
                                flightCard.setAirlineName(airline.getName());
                                flightCard.setAirlineLogo(airline.getLogo());
                            }


                            // --- GẮN GIÁ VÀ TÊN HẠNG VÉ RIÊNG BIỆT CHO THẺ NÀY ---
                            flightCard.setDisplayPrice(option.getBasePrice());

                            FareClass fareClass = fareClassesMap.get(option.getFareClassId());
                            if (fareClass != null) {
                                flightCard.setFareClassName(fareClass.getTitle()); // VD: "Economy Flex" hoặc "Premium Economy"
                            }

                            if (flightCard.getAircraftId() != null) {
                                Aircraft aircraft = aircraftsMap.get(flightCard.getAircraftId());
                                if (aircraft != null) {
                                    flightCard.setAirCraftName(aircraft.getModelName());
                                    flightCard.setSeatMapId(aircraft.getSeatMapId());
                                }
                            }


                            // Add thẻ độc lập này vào danh sách
                            result.add(flightCard);
                        }
                    }

                    // 3. SẮP XẾP TOÀN BỘ DANH SÁCH TỪ RẺ ĐẾN ĐẮT NHƯ TRONG ẢNH APP 123.BAY
                    result.sort(Comparator.comparingDouble(Flight::getDisplayPrice));

                    callback.onLoaded(result);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }

        /**
         * Tìm giá rẻ nhất trong fareOptions phù hợp với seatType.
         *
         * @param fareOptions  Danh sách gói vé của chuyến bay
         * @return Giá rẻ nhất, hoặc 0.0 nếu không tìm thấy
         */
    //    private FareOption findCheapestOptionForSeatType(List<FareOption> fareOptions) {
    //        FareOption cheapestOption = null;
    //        double minPrice = Double.MAX_VALUE;
    //
    //        for (FareOption option : fareOptions) {
    //            // Kiểm tra còn chỗ không
    //            if (!option.isAvailable()) continue;
    //
    //            // Lấy FareClass để biết seatType
    //            FareClass fareClass = fareClassesMap.get(option.getFareClassId());
    //            if (fareClass == null) continue;
    //
    //            // Lấy giá rẻ nhất
    //            if (option.getBasePrice() < minPrice) {
    //                minPrice = option.getBasePrice();
    //                cheapestOption = option;
    //            }
    //        }
    //
    //        return cheapestOption;
    //    }

    //    private String mapSeatClassToType(String seatClass) {
    //        if (seatClass == null) return "ECONOMY";
    //
    //        switch (seatClass.toLowerCase()) {
    //            case "phổ thông":
    //                return "ECONOMY";
    //            case "thương gia":
    //                return "BUSINESS";
    //            case "hạng nhất":
    //                return "FIRST_CLASS";
    //            default:
    //                return "ECONOMY";
    //        }
    //    }

        /**
         * Clear cache khi cần refresh.
         */
    //    public void clearCache() {
    //        citiesMap.clear();
    //        airlinesMap.clear();
    //        fareClassesMap.clear();
    //    }
    }