package utils;

import exception.InvalidDataException;
import java.util.regex.Pattern;

/** Kiểm tra dữ liệu nhập; ném InvalidDataException kèm lý do cụ thể. */
public final class Validator {

    /** Số điện thoại Việt Nam: bắt đầu bằng 0, tổng 10-11 chữ số. */
    private static final Pattern PHONE = Pattern.compile("^0\\d{9,10}$");

    private Validator() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    public static void requireNotBlank(String value, String fieldName) throws InvalidDataException {
        if (isBlank(value)) {
            throw new InvalidDataException(fieldName + " không được để trống");
        }
    }

    public static void requireValidPhone(String phone) throws InvalidDataException {
        if (!isValidPhone(phone)) {
            throw new InvalidDataException(
                    "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10-11 chữ số): " + phone);
        }
    }

    public static void requireNonNegative(double value, String fieldName) throws InvalidDataException {
        if (value < 0) {
            throw new InvalidDataException(fieldName + " không được âm");
        }
    }
}
