package com.example.flight_booking_app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.data.model.AuthResult;
import com.example.flight_booking_app.data.model.City;
import com.example.flight_booking_app.data.repository.CityRepository;

import java.util.ArrayList;
import java.util.List;

public class CityViewModel extends ViewModel {
    private final CityRepository repository ;
    private final MutableLiveData<List<City>> cityList = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> cityState = new MutableLiveData<>();
    private List<City> originalList = new ArrayList<>(); // Cache dữ liệu gốc

    public CityViewModel() {
        this.repository = new CityRepository();
        loadData();
    }

    public LiveData<List<City>> getCityList() {
        return cityList;
    }


    private void loadData() {
        repository.getAllCities(new CityRepository.OnCitiesLoaded() {
            @Override
            public void onLoaded(List<City> cities) {
                // gán danh sách thành phố
                originalList = cities;
                cityList.setValue(cities);
            }

            @Override
            public void onError(String errorMessage) {
                cityState.setValue(AuthResult.error(errorMessage));

            }
        });
    }

    public void search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            cityList.setValue(originalList);
            return;
        }

        String lower = keyword.toLowerCase().trim();
        List<City> filtered = new ArrayList<>();
        for (City city : originalList) {
            // Lọc theo tên thành phố, tên sân bay hoặc mã IATA[cite: 13]
            if (city.getCityName().toLowerCase().contains(lower)
                    || city.getAirportName().toLowerCase().contains(lower)
                    || city.getIataCode().toLowerCase().contains(lower)) {
                filtered.add(city);
            }
        }
        cityList.setValue(filtered);
    }
}