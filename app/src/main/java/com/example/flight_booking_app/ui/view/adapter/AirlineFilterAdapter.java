package com.example.flight_booking_app.ui.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.Airline;

import java.util.List;


public class AirlineFilterAdapter extends RecyclerView.Adapter<AirlineFilterAdapter.FlightFilterViewHolder> {

    private final List<Airline> items;
    private final List<String> selectedNames;
    private final Runnable onChanged;


    public AirlineFilterAdapter(List<Airline> items,
                                List<String> selectedNames,
                                Runnable onChanged) {
        this.items = items;
        this.selectedNames = selectedNames;
        this.onChanged = onChanged;
    }

    public void clearSelection() {
        selectedNames.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlightFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_airline, parent, false);
        return new FlightFilterViewHolder(v);
    }

    // đổ dữ liệu lên
    @Override
    public void onBindViewHolder(@NonNull FlightFilterViewHolder holder, int position) {
        Airline item = items.get(position);
        holder.tvName.setText(item.getName());

        holder.cbCheck.setChecked(selectedNames.contains(item.getName()));

        Glide.with(holder.itemView.getContext())
                .load(item.getLogo())
                .placeholder(R.drawable.ic_airline)
                .error(R.drawable.ic_airline)
                .into(holder.imgLogo);

        // 2. Logic Click cực kỳ dễ hiểu
        View.OnClickListener toggleClick = v -> {
            if (selectedNames.isEmpty()) {
                // Đang rỗng (mặc định), bấm 1 cái -> Add đúng cái đó vào list
                selectedNames.add(item.getName());
            } else {
                // Đang có tích -> Bấm vào thì Thêm/Xóa bình thường
                if (selectedNames.contains(item.getName())) {
                    selectedNames.remove(item.getName());
                } else {
                    selectedNames.add(item.getName());
                }
            }

            notifyDataSetChanged();
            if (onChanged != null) onChanged.run();
        };

        holder.itemView.setOnClickListener(toggleClick);
        holder.cbCheck.setOnClickListener(toggleClick);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FlightFilterViewHolder extends RecyclerView.ViewHolder {
        android.widget.CheckBox cbCheck;
        android.widget.ImageView imgLogo;
        TextView tvName;

        FlightFilterViewHolder(@NonNull View v) {
            super(v);
            cbCheck = v.findViewById(R.id.cb_airline_check);
            imgLogo = v.findViewById(R.id.img_airline_logo);
            tvName = v.findViewById(R.id.tv_airline_name);
        }
    }
}
