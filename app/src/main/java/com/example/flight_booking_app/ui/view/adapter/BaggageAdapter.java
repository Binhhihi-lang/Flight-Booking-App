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

/**
 * BaggageAdapter – Hiển thị danh sách gói hành lý ký gửi.
 *
 * Tái sử dụng được cho cả lượt đi (rv_baggage_outbound) và lượt về (rv_baggage_return).
 *
 * Khi người dùng chọn một gói:
 *   - Gói được chọn sẽ có background = bg_baggage_selecting (drawable selector)
 *   - Các gói còn lại trở về background mặc định
 *   - Callback onBaggageSelected trả về BaggageOption được chọn cho Activity/Fragment
 */
public class BaggageAdapter extends RecyclerView.Adapter<BaggageAdapter.BaggageViewHolder> {

    // ── Interface callback ────────────────────────────────────────────────
    public interface OnBaggageSelectedListener {
        void onBaggageSelected(BaggageOption selected);
    }

    // ── Data ──────────────────────────────────────────────────────────────
    private final List<BaggageOption> options;
    private int selectedPosition = 0; // Mặc định chọn gói đầu tiên
    private final OnBaggageSelectedListener listener;

    public BaggageAdapter(List<BaggageOption> options, OnBaggageSelectedListener listener) {
        this.options = options;
        this.listener = listener;

        // Tự động đánh dấu gói miễn phí đầu tiên là được chọn mặc định
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).isFree()) {
                selectedPosition = i;
                options.get(i).setSelected(true);
                break;
            }
        }
    }


    @NonNull
    @Override
    public BaggageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_baggage, parent, false);
        return new BaggageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaggageViewHolder holder, int position) {
        BaggageOption option = options.get(position);

        // Hiển thị số kg (ẩn nếu = 0, tức là "Không thêm")
        if (option.getWeightKg() > 0) {
            holder.tvWeight.setVisibility(View.VISIBLE);
            holder.tvWeight.setText(option.getWeightKg() + " kg");
        } else {
            holder.tvWeight.setVisibility(View.GONE);
        }

        // Hiển thị giá: "Miễn phí" nếu isFree, ngược lại định dạng tiền VND
        if (option.isFree() || option.getWeightKg() == 0) {
            holder.tvPrice.setText("Không thêm");
        } else {
            holder.tvPrice.setText(PriceFormatter.formatPrice(option.getPriceVnd()));
        }

        // 2. Áp dụng background theo trạng thái chọn
        holder.itemView.setSelected(position == selectedPosition);

        // 3. Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_ID) return;

            int previous = selectedPosition;
            selectedPosition = adapterPosition;

            // Cập nhật trạng thái isSelected trong data
            for (BaggageOption opt : options) opt.setSelected(false);
            options.get(adapterPosition).setSelected(true);

            // Chỉ vẽ lại 2 item bị thay đổi → tránh blink toàn list
            notifyItemChanged(previous);
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

    // ── Helper ────────────────────────────────────────────────────────────

    /** Lấy gói đang được chọn (dùng để lưu vào Passenger khi submit) */
    public BaggageOption getSelectedOption() {
        if (options == null || options.isEmpty()) return null;
        return options.get(selectedPosition);
    }

    /** Khôi phục selection khi mở lại màn hình (khớp theo baggageId) */
    public void restoreSelection(String baggageId) {
        if (baggageId == null) return;
        for (int i = 0; i < options.size(); i++) {
            if (baggageId.equals(options.get(i).getBaggageId())) {
                int previous = selectedPosition;
                selectedPosition = i;
                options.get(i).setSelected(true);
                if (previous != i) {
                    options.get(previous).setSelected(false);
                    notifyItemChanged(previous);
                }
                notifyItemChanged(i);
                return;
            }
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    static class BaggageViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeight;
        TextView tvPrice;

        BaggageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeight = itemView.findViewById(R.id.tv_baggage_weight);
            tvPrice  = itemView.findViewById(R.id.tv_baggage_price);
        }
    }
}