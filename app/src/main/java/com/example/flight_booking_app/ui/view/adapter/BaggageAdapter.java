package com.example.flight_booking_app.ui.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.data.model.BaggageOption;
import com.example.flight_booking_app.utils.PriceFormatter;

import java.util.List;
import java.util.Locale;

public class BaggageAdapter extends RecyclerView.Adapter<BaggageAdapter.BaggageViewHolder> {

    // Callback onBaggageSelected trả về BaggageOption được chọn cho Activity
    public interface OnBaggageSelectedListener {
        void onBaggageSelected(BaggageOption selected);
    }

    private final List<BaggageOption> options;
    private int selectedPosition = -1;
    private final OnBaggageSelectedListener listener;

    public BaggageAdapter(List<BaggageOption> options, OnBaggageSelectedListener listener) {
        this.options = options;
        this.listener = listener;

    }

    // nơi để chứa dữ liệu
    @NonNull
    @Override
    public BaggageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_baggage, parent, false);
        return new BaggageViewHolder(view);
    }

    // Đổ dữ liệu và bắt sự kiện click vào item
    @Override
    public void onBindViewHolder(@NonNull BaggageViewHolder holder, int position) {
        BaggageOption option = options.get(position);

        // Hiển thị giá: "Miễn phí" nếu isFree, ngược lại định dạng tiền VND
        if (option.getPriceVnd() == 0 ) {
            holder.tvWeight.setText(option.getWeightKg() + " kg");
            holder.tvPrice.setText("Miễn phí");
        } else if (option.getWeightKg() == 0) {
            holder.tvWeight.setText("0 kg");
            holder.tvPrice.setText("Không thêm");
        } else {
            holder.tvWeight.setText(option.getWeightKg() + " kg");
            holder.tvPrice.setText(PriceFormatter.formatPrice(option.getPriceVnd()));
        }

        // Áp dụng background theo trạng thái chọn
        // set trạng thái state_selected = true để giao diện hiển thị
        holder.itemView.setSelected(position == selectedPosition);

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            // lấy vị trí hiện tại
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_ID) return;

            // Nếu bấm lại vào đúng ô đang chọn thì không làm gì cả
            if (adapterPosition == selectedPosition) return;

            int previous = selectedPosition;
            // cập nhật vị trí mới
            selectedPosition = adapterPosition;

            // Cập nhật trạng thái isSelected trong Model BaggageOption
            for (BaggageOption opt : options) opt.setSelected(false);
            options.get(adapterPosition).setSelected(true);

            // Chỉ vẽ lại 2 item bị thay đổi
            // không dùng notifyDataSetChanged() để vẽ lại toàn bộ
            if (previous != -1) {
                notifyItemChanged(previous);
            }
            notifyItemChanged(adapterPosition);

            // Thông báo lên Activity
            if (listener != null) {
                listener.onBaggageSelected(options.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }


    /**
     * Lấy gói đang được chọn (dùng để lưu vào Passenger khi submit)
     */
    public BaggageOption getSelectedOption() {
        if (options == null || options.isEmpty() || selectedPosition == -1) {
            return null;
        }
        return options.get(selectedPosition);
    }

    /**
     * Khôi phục selection khi mở lại màn hình (khớp theo baggageId)
     */
    public void restoreSelection(String baggageId) {
        if (baggageId == null) return;

        // tìm hành lý
        for (int i = 0; i < options.size(); i++) {
            if (baggageId.equals(options.get(i).getBaggageId())) {
                int previous = selectedPosition;
                selectedPosition = i;
                options.get(i).setSelected(true);

                // chỉ hoán đổi trạng thái nếu vị trí cũ hợp lệ (khác -1) và khác vị trí mới
                // tránh lỗi IndexOutOfBoundsException
                if (previous != -1 && previous != i) {
                    options.get(previous).setSelected(false);
                    notifyItemChanged(previous);
                }
                notifyItemChanged(i);
                return;
            }
        }
    }


    static class BaggageViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeight;
        TextView tvPrice;

        BaggageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeight = itemView.findViewById(R.id.tv_baggage_weight);
            tvPrice = itemView.findViewById(R.id.tv_baggage_price);
        }
    }
}