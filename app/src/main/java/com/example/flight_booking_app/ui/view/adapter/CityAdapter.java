package com.example.flight_booking_app.ui.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.City;

/**
 * Adapter cho danh sách thành phố / sân bay.
 * Dùng ListAdapter + DiffUtil để update list hiệu quả khi lọc.
 */
public class CityAdapter extends ListAdapter<City, CityAdapter.CityViewHolder> {

    private final OnCityClickListener listener;

    // interface cho sk click item
    public interface OnCityClickListener {
        void onCityClick(City city);
    }

    public CityAdapter(OnCityClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    //  DiffUtil để thay thế notifyDataSetChanged.
    // Class này tìm sự khác nhau giữa 2 lists và cung cấp danh sách mới dưới dạng output.
    // Lớp này được sử dụng để thông báo cấp nhập cho RecyclerView Adapter.
    private static final DiffUtil.ItemCallback<City> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<City>() {
                // Nó quyết định xem 2 đối tượng có cùng Items hay là không
                @Override
                public boolean areItemsTheSame(@NonNull City a, @NonNull City b) {
                    return a.getIataCode().equals(b.getIataCode());
                }

                // Nó quyết định xem 2 Items có cùng dữ liệu hay là không.
                // Phương thức này chỉ được gọi khi areItemsTheSame() trả về true.
                @Override
                public boolean areContentsTheSame(@NonNull City a, @NonNull City b) {
                    return a.getCityName().equals(b.getCityName())
                            && a.getAirportName().equals(b.getAirportName());
                }
            };

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(v);
    }

    // đổ dữ liệu lên dòng đó
    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // ViewHolder định nghĩa các phần trong item city lên
    static class CityViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIata;
        private final TextView tvCityName;
        private final TextView tvAirportName;

        CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIata = itemView.findViewById(R.id.tv_airport_code);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvAirportName = itemView.findViewById(R.id.tv_airport_name);
        }

        void bind(City city, OnCityClickListener listener) {
            tvIata.setText(city.getIataCode());
            tvCityName.setText(city.getCityName());
            tvAirportName.setText(city.getAirportName());
            itemView.setOnClickListener(v -> listener.onCityClick(city));

        }
    }
}
