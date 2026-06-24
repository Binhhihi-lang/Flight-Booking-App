package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.City;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CityRepository {

    private final FirebaseFirestore db;

    // Cache in-memory — cities gần như không đổi trong suốt session
    private List<City> cachedCities = null;

    public CityRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnCitiesLoaded {
        void onLoaded(List<City> cities);
        void onError(String error);
    }

    public void getAllCities(OnCitiesLoaded callback) {
        // Trả cache ngay nếu đã có, không fetch lại
        if (cachedCities != null && !cachedCities.isEmpty()) {
            callback.onLoaded(cachedCities);
            return;
        }

        db.collection("cities")
                .orderBy("cityName")   // sort sẵn để Adapter không cần sort thêm
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<City> cities = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        City city = doc.toObject(City.class);
                        if (city != null) cities.add(city);
                    }
                    cachedCities = cities;
                    callback.onLoaded(cities);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}