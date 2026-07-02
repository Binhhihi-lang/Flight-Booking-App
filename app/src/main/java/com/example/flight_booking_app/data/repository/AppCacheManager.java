package com.example.flight_booking_app.data.repository;

import com.example.flight_booking_app.data.model.FareClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton Cache Manager
 * Quản lý bộ nhớ đệm toàn cục cho ứng dụng, giải quyết vấn đề vòng đời
 */
public class AppCacheManager {

    private static AppCacheManager instance;

    // Kho lưu trữ Hạng vé
    private final Map<String, FareClass> fareClassCache = new HashMap<>();

    // Lưu lại thời điểm cuối cùng cập nhật dữ liệu từ mạng
    private long lastFetchTime = 0;

    // Đặt hạn sử dụng của Cache là 30 phút (Tính bằng milliseconds)
    private static final long CACHE_TTL_MS = 30 * 60 * 1000;

    // Private constructor ngăn chặn việc tạo đối tượng mới bằng từ khóa 'new'
    public  AppCacheManager() {}

    // Lấy instance duy nhất của ứng dụng
    public static synchronized AppCacheManager getInstance() {
        if (instance == null) {
            instance = new AppCacheManager();
        }
        return instance;
    }

    /**
     * Kiểm tra xem bộ đệm còn dùng được không.
     * Hợp lệ nếu: Có dữ liệu VÀ chưa vượt quá 30 phút kể từ lần tải cuối.
     */
    public boolean isCacheValid() {
        return !fareClassCache.isEmpty() && (System.currentTimeMillis() - lastFetchTime) < CACHE_TTL_MS;
    }

    /**
     * Lưu dữ liệu mới lấy từ Firestore vào kho và chốt thời gian.
     */
    public void saveAllFareClasses(List<FareClass> list) {
        if (list == null || list.isEmpty()) return;

        for (FareClass fc : list) {
            fareClassCache.put(fc.getFareClassId(), fc);
        }

        // Chỉ chốt mốc thời gian một lần duy nhất cho cả danh sách
        lastFetchTime = System.currentTimeMillis();
    }

    /**
     * Trích xuất Hạng vé bằng ID.
     */
    public FareClass getFareClass(String fareClassId) {
        return fareClassCache.get(fareClassId);
    }

    /**
     * Xóa bộ nhớ (Dùng khi người dùng Đăng xuất hoặc app cần giải phóng RAM).
     */
    public void clearCache() {
        fareClassCache.clear();
        lastFetchTime = 0;
    }
}