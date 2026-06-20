package com.example.flight_booking_app.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    public static String formatDate(int day, int month, int year){
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
    };
}