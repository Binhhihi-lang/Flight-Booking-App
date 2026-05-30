package com.example.flight_booking_app.ui.view.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * FlightPaginationScrollListener – Lắng nghe scroll RecyclerView để kích hoạt load trang tiếp.
 * <p>
 * Ngưỡng trigger: khi người dùng đã scroll đến item cuối cùng đang hiển thị
 * (visibleEnd >= totalItemCount) thì gọi loadMoreItems().
 * <p>
 * Điều kiện bảo vệ:
 * - isLoading() == true  → đang load rồi, không gọi thêm
 * - isLastPage() == true → hết data, không gọi thêm
 */
public abstract class FlightPaginationScrollListener extends RecyclerView.OnScrollListener {

    private final LinearLayoutManager layoutManager;

    public FlightPaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();           // số item đang hiển thị
        int totalItemCount = layoutManager.getItemCount();            // tổng item trong adapter
        int firstVisiblePos = layoutManager.findFirstVisibleItemPosition(); // vị trí item đầu tiên

        if (isLoading() || isLastPage()) return;

        boolean reachedBottom = (firstVisiblePos >= 0)
                && (visibleItemCount + firstVisiblePos) >= totalItemCount;

        if (reachedBottom) {
            loadMoreItems();
        }
    }

    /**
     * Gọi khi cần load thêm trang. Activity/Fragment override để tăng page.
     */
    public abstract void loadMoreItems();

    /**
     * Trả về true khi đang trong quá trình load ngăn gọi chồng chéo.
     */
    public abstract boolean isLoading();

    /**
     * Trả về true khi đã hết data ngừng trigger.
     */
    public abstract boolean isLastPage();
}