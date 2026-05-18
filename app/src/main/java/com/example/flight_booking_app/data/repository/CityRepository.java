package com.example.flight_booking_app.data.repository;

import androidx.annotation.NonNull;
import com.example.flight_booking_app.data.model.City;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CityRepository {
    private final DatabaseReference dbRef;

    public CityRepository() {
        // Trỏ thẳng vào node cities
        this.dbRef = FirebaseDatabase.getInstance().getReference("Cities");
    }

    public interface OnCitiesLoaded {
        void onLoaded(List<City> cities);
        void onError(String error);
    }

    public void getAllCities(OnCitiesLoaded callCitiesLoaded) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<City> cities = new ArrayList<>();
                // Duyệt qua từng Key (HAN, SGN, ...) để lấy Object City[cite: 13]
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    City city = postSnapshot.getValue(City.class);
                    if (city != null) cities.add(city);
                }
                callCitiesLoaded.onLoaded(cities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callCitiesLoaded.onError(error.getMessage());
            }
        });
    }

}