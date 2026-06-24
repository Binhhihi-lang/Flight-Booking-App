package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.Airline;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class AirlineRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnAirlinesLoaded {
        void onLoaded(List<Airline> airlines);

        void onError(String errorMessage);
    }

    public void getAllAirlines(OnAirlinesLoaded callback) {
        // Kiểm tra RAM Cache trước (nếu bạn làm AppCacheManager cho Airline)
        // Nếu không có thì gọi Firebase
        db.collection("airlines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Airline> result = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String logoUrl = doc.getString("logo");
                        result.add(new Airline(name, logoUrl));
                    }
                    callback.onLoaded(result);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
}
