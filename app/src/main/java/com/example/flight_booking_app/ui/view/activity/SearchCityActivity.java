package com.example.flight_booking_app.ui.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.City;
import com.example.flight_booking_app.ui.view.adapter.CityAdapter;
import com.example.flight_booking_app.ui.viewmodel.CityViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Trả về đủ thông tin của City được chọn:
 * RESULT_CITY_ID    — để HomeViewModel lưu và dùng cho Firebase query
 * RESULT_CITY_NAME  — để hiển thị lên card
 * RESULT_IATA_CODE  — để hiển thị lên card
 * RESULT_AIRPORT_NAME — để hiển thị lên card
 * RESULT_MODE       — "from" hoặc "to"
 */
public class SearchCityActivity extends AppCompatActivity {

    // Extra keys
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_FROM = "from";
    public static final String MODE_TO = "to";

    // Result keys
    public static final String RESULT_CITY_ID = "result_city_id";
    public static final String RESULT_CITY_NAME = "result_city_name";
    public static final String RESULT_IATA_CODE = "result_iata_code";
    public static final String RESULT_AIRPORT_NAME = "result_airport_name";
    public static final String RESULT_MODE = "result_mode";

    // Views
    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private RecyclerView rvCities;
    private TextView tvEmpty;

    // MVVM
    private CityViewModel cityViewModel;
    private CityAdapter adapter;

    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_city);

        String extraMode = getIntent().getStringExtra(EXTRA_MODE);
        mode = extraMode != null ? extraMode : MODE_FROM;

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupSearch();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbarCity);
        etSearch = findViewById(R.id.et_name);
        rvCities = findViewById(R.id.rv_cities);
        //tvEmpty  = findViewById(R.id.tv_empty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(MODE_FROM.equals(mode) ? "Chọn điểm đi" : "Chọn điểm đến");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        //
        adapter = new CityAdapter(city -> onCitySelected(city));
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(adapter);
    }

    private void setupViewModel() {
        cityViewModel = new ViewModelProvider(this).get(CityViewModel.class);
        cityViewModel.getCityList().observe(this, cities -> {
            adapter.submitList(cities);
//            if (tvEmpty != null) {
//                tvEmpty.setVisibility(cities.isEmpty() ? View.VISIBLE : View.GONE);
//            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                cityViewModel.search(s.toString());
            }
        });
    }

    private void onCitySelected(City city) {
        Intent result = new Intent();
        result.putExtra(RESULT_CITY_ID, city.getCityId());
        result.putExtra(RESULT_CITY_NAME, city.getCityName());
        result.putExtra(RESULT_IATA_CODE, city.getIataCode());
        result.putExtra(RESULT_AIRPORT_NAME, city.getAirportName());
        result.putExtra(RESULT_MODE, mode);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}