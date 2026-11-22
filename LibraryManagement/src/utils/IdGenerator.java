package utils;

import java.util.List;
import model.Book;
import model.BorrowTicket;
import model.Librarian;
import model.Reader;

/** Sinh mã tự động: phiếu mượn PM0001, sách S001, bạn đọc SV01 / UT01 / GV01, tài khoản TT01. */
public final class IdGenerator {

    private IdGenerator() {
    }

    /** Mã phiếu mượn: PM + 4 chữ số (lớn nhất hiện có + 1). */
    public static String nextTicketId(List<BorrowTicket> tickets) {
        int max = 0;
        for (BorrowTicket t : tickets) {
            max = Math.max(max, numberAfter(t.getTicketId(), "PM"));
        }
        return String.format("PM%04d", max + 1);
    }

    /** Mã sách: S + 3 chữ số. */
    public static String nextBookId(List<Book> books) {
        int max = 0;
        for (Book b : books) {
            max = Math.max(max, numberAfter(b.getBookId(), "S"));
        }
        return String.format("S%03d", max + 1);
    }

    /** Mã tài khoản thủ thư: TT + 2 chữ số (lớn nhất hiện có + 1). */
    public static String nextAccountId(List<Librarian> accounts) {
        int max = 0;
        for (Librarian l : accounts) {
            max = Math.max(max, numberAfter(l.getId(), "TT"));
        }
        return String.format("TT%02d", max + 1);
    }

    /** Mã bạn đọc theo tiền tố loại (SV, UT, GV) + 2 chữ số. */
    public static String nextReaderId(List<Reader> readers, String prefix) {
        int max = 0;
        for (Reader r : readers) {
            max = Math.max(max, numberAfter(r.getId(), prefix));
        }
        return String.format("%s%02d", prefix, max + 1);
    }

    /** Lấy phần số sau tiền tố; trả 0 nếu mã không đúng định dạng. */
    private static int numberAfter(String id, String prefix) {
        if (id == null || !id.toUpperCase().startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
