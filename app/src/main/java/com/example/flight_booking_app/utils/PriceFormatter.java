package com.example.flight_booking_app.utils;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PriceFormatter {

    /**
     * Định dạng số tiền đơn lẻ: 1500000 -> "1.500.000 đ"
     */
    public static String formatPrice(double price) {
        // Sử dụng DecimalFormat để kiểm soát dấu chấm phân cách hàng nghìn theo kiểu VN
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.'); // Dấu chấm phân cách hàng nghìn

        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(price) + " đ";
    }

    /**
     * Định dạng chuỗi số lượng x đơn giá: 2, 1500000 -> "2 x 1.500.000 đ"
     */
    public static String formatCountAndPrice(int count, double singlePrice) {
        // Sử dụng DecimalFormat để format phần tiền bên trong
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return String.format(Locale.getDefault(), "%d x %s đ", count, decimalFormat.format(singlePrice));
    }

    // Format chuẩn 2 số (Ví dụ: 05/09/1998 thay vì 5/9/1998)
    public static String formatDate(int day, int month, int year) {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
    }

    /**
     * Chuyển Timestamp thành chuỗi hiển thị "HH:mm dd/MM/yyyy"
     */
    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "--:-- --/--/----";
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        return formatter.format(timestamp.toDate());
    }

    /**
     * Chuyển Timestamp thành giờ hiển thị (Ví dụ: "08:35")
     */
    public static String formatTimeOnly(Timestamp timestamp) {
        if (timestamp == null) return "--:--";
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(timestamp.toDate());
    }

    /**
     * Chuyển Timestamp thành ngày tháng hiển thị (Ví dụ: "24/06/2026")
     */
    public static String formatDateOnly(Timestamp timestamp) {
        if (timestamp == null) return "--/--/----";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(timestamp.toDate());
    }

    public static String formatDateFromMillis(Long millis) {
        if (millis == null || millis == 0L) {
            return "--/--/----"; // Trả về chuỗi rỗng mặc định nếu chưa chọn ngày
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            e.printStackTrace();
            return "--/--/----";
        }

    }
}