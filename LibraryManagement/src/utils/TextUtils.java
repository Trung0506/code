package utils;

import java.text.Normalizer;
import java.util.Locale;

/** Xử lý chuỗi tiếng Việt: bỏ dấu để tìm kiếm / sắp xếp không phân biệt dấu. */
public final class TextUtils {

    private TextUtils() {
    }

    /** "Lập trình Java" -> "Lap trinh Java". */
    public static String removeAccents(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.replace('đ', 'd').replace('Đ', 'D');
    }

    /** Tìm keyword trong text, không phân biệt hoa thường và dấu. */
    public static boolean containsIgnoreAccent(String text, String keyword) {
        String t = removeAccents(text).toLowerCase(Locale.ROOT);
        String k = removeAccents(keyword).trim().toLowerCase(Locale.ROOT);
        return t.contains(k);
    }

    /** Từ cuối của họ tên (tên gọi): "Nguyễn Văn An" -> "An", dùng để sắp xếp theo tên. */
    public static String lastWord(String fullName) {
        String name = (fullName == null) ? "" : fullName.trim();
        int idx = name.lastIndexOf(' ');
        return idx < 0 ? name : name.substring(idx + 1);
    }
}
