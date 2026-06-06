package com.example.flight_booking_app.data.repository;

import androidx.annotation.NonNull;

import com.example.flight_booking_app.data.model.FareRule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookingInfoRepository {

    private final DatabaseReference dbFareRules;

    public BookingInfoRepository() {
        dbFareRules = FirebaseDatabase.getInstance().getReference("FareRules");
    }

    public interface OnFareRuleLoaded {
        void onLoaded(FareRule fareRule);
        void onError(String error);
    }

    // Chỉ nhận ID và Callback, đúng chuẩn
    public void getFareRuleById(String fareRuleId, OnFareRuleLoaded callback) {
        dbFareRules.child(fareRuleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onLoaded(snapshot.getValue(FareRule.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}