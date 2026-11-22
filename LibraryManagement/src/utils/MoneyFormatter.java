package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Định dạng tiền kiểu Việt Nam (15.000đ), không phụ thuộc locale của máy chạy chương trình. */
public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols).format(amount) + "đ";
    }
}
