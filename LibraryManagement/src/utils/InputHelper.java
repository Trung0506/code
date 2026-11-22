package utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Nhập liệu an toàn từ console: nhập sai thì báo lỗi và hỏi lại, không làm chương trình bị văng.
 * Khi hết dữ liệu nhập (Ctrl+D / đầu vào bị cắt) chương trình thoát nhẹ nhàng.
 */
public final class InputHelper {

    private static final Scanner SCANNER = new Scanner(System.in, "UTF-8");

    private InputHelper() {
    }

    /** Đọc một dòng (đã trim). */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println("(Hết dữ liệu nhập - thoát chương trình)");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    /** Đọc chuỗi bắt buộc, để trống thì hỏi lại. */
    public static String readRequired(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("  Không được để trống, vui lòng nhập lại.");
        }
    }

    /** Đọc số nguyên trong khoảng [min, max]. */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int value = Integer.parseInt(s);
                if (value < min || value > max) {
                    System.out.println("  Giá trị phải từ " + min + " đến " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  Vui lòng nhập một số nguyên hợp lệ.");
            }
        }
    }

    /** Đọc số nguyên không giới hạn khoảng (để service tự báo lỗi nghiệp vụ, ví dụ số lượng <= 0). */
    public static int readInt(String prompt) {
        return readInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /** Đọc số thực >= min. */
    public static double readDouble(String prompt, double min) {
        while (true) {
            String s = readLine(prompt).replace(",", "");
            try {
                double value = Double.parseDouble(s);
                if (value < min) {
                    System.out.println("  Giá trị không được nhỏ hơn " + min + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  Vui lòng nhập một số hợp lệ.");
            }
        }
    }

    /** Đọc ngày dd/MM/yyyy; bỏ trống (Enter) thì lấy giá trị mặc định. */
    public static LocalDate readDate(String label, LocalDate defaultValue) {
        while (true) {
            String s = readLine(label + " (dd/MM/yyyy, Enter = " + DateFormatter.format(defaultValue) + "): ");
            if (s.isEmpty()) {
                return defaultValue;
            }
            try {
                return DateFormatter.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  Ngày không hợp lệ, nhập theo dạng dd/MM/yyyy (ví dụ 25/12/2026).");
            }
        }
    }

    /** Hỏi có/không. */
    public static boolean confirm(String question) {
        String s = readLine(question + " (y/n): ").toLowerCase();
        return s.equals("y") || s.equals("yes") || s.equals("c") || s.equals("co") || s.equals("có");
    }

    /** Khi sửa: Enter để giữ nguyên giá trị hiện tại. */
    public static String readStringOrKeep(String label, String current) {
        String s = readLine("  " + label + " [" + current + "]: ");
        return s.isEmpty() ? current : s;
    }

    public static int readIntOrKeep(String label, int current, int min, int max) {
        while (true) {
            String s = readLine("  " + label + " [" + current + "]: ");
            if (s.isEmpty()) {
                return current;
            }
            try {
                int value = Integer.parseInt(s);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  Giá trị phải từ " + min + " đến " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Vui lòng nhập một số nguyên hợp lệ.");
            }
        }
    }

    public static double readDoubleOrKeep(String label, double current, double min) {
        while (true) {
            String s = readLine("  " + label + " [" + MoneyFormatter.format(current) + "]: ").replace(",", "");
            if (s.isEmpty()) {
                return current;
            }
            try {
                double value = Double.parseDouble(s);
                if (value >= min) {
                    return value;
                }
                System.out.println("  Giá trị không được nhỏ hơn " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Vui lòng nhập một số hợp lệ.");
            }
        }
    }

    /** Nhập mật khẩu: ẩn ký tự nếu chạy trong console thật, còn không thì đọc dòng thường. */
    public static String readPassword(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars == null ? "" : new String(chars);
        }
        return readLine(prompt);
    }
}
