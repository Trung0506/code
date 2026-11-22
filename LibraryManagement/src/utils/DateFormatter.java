package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/** Định dạng ngày kiểu Việt Nam dd/MM/yyyy (hiển thị và nhập). */
public final class DateFormatter {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** STRICT: không chấp nhận ngày không có thật như 31/02/2026. */
    private static final DateTimeFormatter PARSE =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private DateFormatter() {
    }

    public static String format(LocalDate date) {
        return date == null ? "" : DISPLAY.format(date);
    }

    /** @throws java.time.format.DateTimeParseException nếu sai định dạng. */
    public static LocalDate parse(String text) {
        return LocalDate.parse(text.trim(), PARSE);
    }
}
