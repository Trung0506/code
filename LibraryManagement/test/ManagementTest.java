import exception.AuthenticationException;
import exception.BookNotFoundException;
import exception.InvalidDataException;
import exception.ItemInUseException;
import exception.ReaderNotFoundException;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import main.AppContext;
import model.Book;
import model.BorrowTicket;
import model.LecturerReader;
import model.Librarian;
import model.PriorityStudentReader;
import model.Reader;
import model.TicketStatus;
import service.BookService;
import service.ReaderService;
import service.StatisticService;
import utils.DateFormatter;
import utils.IdGenerator;
import utils.MoneyFormatter;
import utils.PasswordUtil;
import utils.TextUtils;
import utils.Validator;

/**
 * Kiểm thử phần quản lý sách / bạn đọc / thống kê / đăng nhập / tiện ích / dữ liệu mẫu.
 * Mỗi nhóm test dùng thư mục dữ liệu tạm riêng (có dữ liệu mẫu), không đụng tới thư mục data/ thật.
 */
public class ManagementTest {

    private static int passed = 0;
    private static int failed = 0;

    interface Action {
        void run() throws Exception;
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }

    static void expectError(String name, Class<? extends Exception> type, String messagePart, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("  [FAIL] " + name + " -> không ném lỗi");
        } catch (Exception e) {
            boolean ok = type.isInstance(e) && (messagePart == null || e.getMessage().contains(messagePart));
            if (ok) {
                passed++;
                System.out.println("  [PASS] " + name + "\n         lỗi hiển thị: " + e.getMessage());
            } else {
                failed++;
                System.out.println("  [FAIL] " + name + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    static File newDir() throws Exception {
        return Files.createTempDirectory("library-mgmt-test").toFile();
    }

    public static void main(String[] args) throws Exception {

        // ------------------------------------------------------------------
        section("TEST 1: Dữ liệu mẫu tự tạo lần chạy đầu và ghi file .dat");
        File dir = newDir();
        AppContext ctx = new AppContext(dir.getPath());
        check("12 đầu sách mẫu", ctx.getBookRepo().getAll().size() == 12);
        check("7 bạn đọc mẫu", ctx.getReaderRepo().getAll().size() == 7);
        check("4 phiếu mượn mẫu", ctx.getTicketRepo().getAll().size() == 4);
        check("4 file .dat được tạo",
                new File(dir, "books.dat").exists() && new File(dir, "readers.dat").exists()
                        && new File(dir, "tickets.dat").exists() && new File(dir, "accounts.dat").exists());
        check("có sách hết hàng để demo (S012 = 0)", ctx.getBookRepo().findById("S012").getQuantity() == 0);

        // ------------------------------------------------------------------
        section("TEST 2: Đăng nhập");
        Librarian admin = ctx.getAuthService().login("admin", "admin123");
        check("admin / admin123 đăng nhập được, vai trò Thủ thư", admin.getRoleName().equals("Thủ thư"));
        expectError("Sai mật khẩu", AuthenticationException.class, "Sai tên đăng nhập hoặc mật khẩu",
                () -> ctx.getAuthService().login("admin", "sai"));
        expectError("Tài khoản không tồn tại (cùng thông báo, không lộ tài khoản nào có thật)",
                AuthenticationException.class, "Sai tên đăng nhập hoặc mật khẩu",
                () -> ctx.getAuthService().login("khongco", "123456"));
        expectError("Tên đăng nhập null", AuthenticationException.class, null,
                () -> ctx.getAuthService().login(null, null));
        check("SHA-256 đúng chuẩn (vector kiểm thử của 'abc')",
                PasswordUtil.sha256("abc").equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));

        // ------------------------------------------------------------------
        section("TEST 2b: Tạo tài khoản thủ thư (AuthService.register)");
        File regDir = newDir();
        final AppContext rc = new AppContext(regDir.getPath());
        Librarian created = rc.getAuthService().register("Trần Văn Mới", "0912345678", "thumoi", "matkhau1");
        check("tạo thành công, mã TT03 (đã có TT01, TT02), vai trò Thủ thư",
                created.getId().equals("TT03") && created.getRoleName().equals("Thủ thư"));
        check("đăng nhập được bằng tài khoản vừa tạo",
                rc.getAuthService().login("thumoi", "matkhau1").getFullName().equals("Trần Văn Mới"));
        expectError("Tên đăng nhập đã tồn tại", InvalidDataException.class, "đã tồn tại",
                () -> rc.getAuthService().register("Ai Đó", "0912345679", "thumoi", "matkhau2"));
        expectError("Tên đăng nhập trùng khác hoa/thường (THUTHU ~ thuthu)", InvalidDataException.class, "đã tồn tại",
                () -> rc.getAuthService().register("Ai Đó", "0912345679", "THUTHU", "matkhau2"));
        expectError("Tên đăng nhập quá ngắn", InvalidDataException.class, "ít nhất 3 ký tự",
                () -> rc.getAuthService().register("Ai Đó", "0912345679", "ab", "matkhau2"));
        expectError("Tên đăng nhập có khoảng trắng", InvalidDataException.class, "không chứa khoảng trắng",
                () -> rc.getAuthService().register("Ai Đó", "0912345679", "a b c", "matkhau2"));
        expectError("Mật khẩu quá ngắn", InvalidDataException.class, "Mật khẩu phải có ít nhất 6",
                () -> rc.getAuthService().register("Ai Đó", "0912345679", "nguoimoi", "12345"));
        expectError("Số điện thoại sai", InvalidDataException.class, "Số điện thoại",
                () -> rc.getAuthService().register("Ai Đó", "912345678", "nguoimoi", "matkhau2"));
        expectError("Họ tên để trống", InvalidDataException.class, "Họ tên",
                () -> rc.getAuthService().register("   ", "0912345679", "nguoimoi", "matkhau2"));
        check("các lần tạo thất bại không làm thêm tài khoản (vẫn 3 tài khoản)",
                rc.getAccountRepo().getAll().size() == 3);
        AppContext rc2 = new AppContext(regDir.getPath());
        check("mở lại chương trình: tài khoản mới vẫn còn (lưu trong accounts.dat), không bị tạo lại dữ liệu mẫu",
                rc2.getAccountRepo().getAll().size() == 3
                        && rc2.getAuthService().login("thumoi", "matkhau1") != null);

        // ------------------------------------------------------------------
        section("TEST 3: Sách - tìm kiếm và sắp xếp (Comparable / Comparator)");
        final BookService books = ctx.getBookService();
        check("Tìm 'lap trinh' (không dấu) ra 'Lập trình Java cơ bản'",
                books.search("lap trinh").size() == 1 && books.search("lap trinh").get(0).getBookId().equals("S001"));
        check("Tìm theo mã 's005' (không phân biệt hoa thường)",
                books.search("s005").size() == 1 && books.search("s005").get(0).getTitle().equals("Giải tích 1"));
        check("Tìm 'dữ liệu' ra 2 sách (có dấu)", books.search("dữ liệu").size() == 2);
        check("Từ khóa không khớp -> rỗng", books.search("xyz123").isEmpty());
        check("Từ khóa rỗng -> rỗng", books.search("   ").isEmpty());

        List<Book> byTitle = books.sortByTitle();
        boolean titleOk = true;
        for (int i = 1; i < byTitle.size(); i++) {
            if (byTitle.get(i - 1).compareTo(byTitle.get(i)) > 0) {
                titleOk = false;
            }
        }
        check("Sắp xếp theo tên (Comparable) đúng thứ tự, đầu tiên là 'Cấu trúc dữ liệu...'",
                titleOk && byTitle.get(0).getBookId().equals("S002"));
        List<Book> byQty = books.sortByQuantityDesc();
        boolean qtyOk = true;
        for (int i = 1; i < byQty.size(); i++) {
            if (byQty.get(i - 1).getQuantity() < byQty.get(i).getQuantity()) {
                qtyOk = false;
            }
        }
        check("Sắp xếp theo số lượng giảm dần (Comparator), đầu tiên là S010 (8 cuốn)",
                qtyOk && byQty.get(0).getBookId().equals("S010"));
        List<Book> byPrice = books.sortByPriceAsc();
        boolean priceOk = true;
        for (int i = 1; i < byPrice.size(); i++) {
            if (byPrice.get(i - 1).getPrice() > byPrice.get(i).getPrice()) {
                priceOk = false;
            }
        }
        check("Sắp xếp theo giá tăng dần, rẻ nhất là S010 (60.000đ)", priceOk && byPrice.get(0).getBookId().equals("S010"));
        check("Sắp xếp trên bản sao, không đổi thứ tự lưu trữ", ctx.getBookRepo().getAll().get(0).getBookId().equals("S001"));

        // ------------------------------------------------------------------
        section("TEST 4: Sách - thêm / sửa / xóa / kiểm tra số lượng");
        Book added = books.addBook("  Nhập môn Python ", "Nguyễn Văn Python", "CNTT", 4, 99000);
        check("Thêm sách: mã tự sinh S013, tên được trim", added.getBookId().equals("S013") && added.getTitle().equals("Nhập môn Python"));
        expectError("Thêm sách tên rỗng", InvalidDataException.class, "Tên sách",
                () -> books.addBook("  ", "A", "B", 1, 1));
        expectError("Thêm sách số lượng âm", InvalidDataException.class, "không được âm",
                () -> books.addBook("X", "A", "B", -1, 1));
        expectError("Thêm sách giá trị âm", InvalidDataException.class, "Giá trị sách",
                () -> books.addBook("X", "A", "B", 1, -5));
        books.updateBook("S013", "Python nâng cao", "Nguyễn Văn Python", "CNTT", 7, 120000);
        check("Sửa sách cập nhật đúng", books.getById("S013").getTitle().equals("Python nâng cao")
                && books.getById("S013").getQuantity() == 7);
        expectError("Sửa sách không tồn tại", BookNotFoundException.class, "S999",
                () -> books.updateBook("S999", "A", "B", "C", 1, 1));
        expectError("Xóa sách đang có phiếu mượn chưa trả (S001)", ItemInUseException.class, "đang có phiếu mượn",
                () -> books.deleteBook("S001"));
        books.deleteBook("S005");
        check("Xóa sách không còn phiếu mượn (S005) thành công", ctx.getBookRepo().findById("S005") == null);
        expectError("Xóa sách không tồn tại", BookNotFoundException.class, "S005",
                () -> books.deleteBook("S005"));
        check("Kiểm tra số lượng: S012 (hết) không đủ", !books.isAvailable("S012", 1));
        check("Kiểm tra số lượng: S010 còn 8, mượn 8 đủ", books.isAvailable("S010", 8));
        check("Kiểm tra số lượng: S010 còn 8, mượn 9 không đủ", !books.isAvailable("S010", 9));

        // ------------------------------------------------------------------
        section("TEST 5: Bạn đọc - thêm / sửa / xóa / tìm / sắp xếp");
        final ReaderService readers = ctx.getReaderService();
        Reader ut = readers.addReader(ReaderService.TYPE_PRIORITY, "Bùi Văn Em", "0904000001");
        check("Thêm SV ưu tiên: mã UT03, đúng lớp con, tối đa 5 cuốn",
                ut.getId().equals("UT03") && ut instanceof PriorityStudentReader && ut.getMaxBooks() == 5);
        check("Thêm giảng viên: mã GV03, đúng lớp con",
                readers.addReader(ReaderService.TYPE_LECTURER, "Cao Thị Phương", "0904000002") instanceof LecturerReader);
        expectError("Thêm bạn đọc SĐT sai định dạng", InvalidDataException.class, "Số điện thoại",
                () -> readers.addReader(ReaderService.TYPE_STUDENT, "Ai Đó", "123"));
        expectError("Thêm bạn đọc họ tên rỗng", InvalidDataException.class, "Họ tên",
                () -> readers.addReader(ReaderService.TYPE_STUDENT, " ", "0904000009"));
        expectError("Thêm bạn đọc loại không hợp lệ", InvalidDataException.class, "Loại bạn đọc",
                () -> readers.addReader(9, "Ai Đó", "0904000009"));
        check("Tìm 'binh' (không dấu) ra 'Trần Thị Bình'",
                readers.search("binh").size() == 1 && readers.search("binh").get(0).getId().equals("SV02"));
        check("Tìm 'gv' ra 3 giảng viên", readers.search("gv").size() == 3);

        List<Reader> byName = readers.sortByName();
        boolean nameOk = true;
        for (int i = 1; i < byName.size(); i++) {
            String a = TextUtils.removeAccents(TextUtils.lastWord(byName.get(i - 1).getFullName()));
            String b = TextUtils.removeAccents(TextUtils.lastWord(byName.get(i).getFullName()));
            if (a.compareToIgnoreCase(b) > 0) {
                nameOk = false;
            }
        }
        check("Sắp xếp bạn đọc theo tên gọi đúng thứ tự, đầu tiên là 'An'",
                nameOk && byName.get(0).getFullName().equals("Nguyễn Văn An"));
        check("Sắp xếp theo mã: GV01 đứng đầu", readers.sortById().get(0).getId().equals("GV01"));

        Reader renamed = readers.updateReader("SV03", ReaderService.TYPE_STUDENT, "Lê Minh Châu B", "0901000099");
        check("Sửa họ tên / SĐT cùng loại", renamed.getFullName().equals("Lê Minh Châu B") && renamed.getPhone().equals("0901000099"));
        Reader promoted = readers.updateReader("SV03", ReaderService.TYPE_LECTURER, "Lê Minh Châu B", "0901000099");
        check("Đổi loại SV03 -> giảng viên: giữ mã, đổi lớp con, tối đa 10 cuốn",
                promoted.getId().equals("SV03") && promoted instanceof LecturerReader && promoted.getMaxBooks() == 10
                        && ctx.getReaderRepo().findById("SV03") instanceof LecturerReader);
        expectError("Đổi loại bạn đọc đang mượn sách (SV01)", ItemInUseException.class, "đang mượn sách",
                () -> readers.updateReader("SV01", ReaderService.TYPE_LECTURER, "Nguyễn Văn An", "0901000001"));
        expectError("Xóa bạn đọc đang mượn sách (SV01)", ItemInUseException.class, "đang mượn sách",
                () -> readers.deleteReader("SV01"));
        readers.deleteReader("SV03");
        check("Xóa bạn đọc không còn mượn sách (SV03) thành công", ctx.getReaderRepo().findById("SV03") == null);
        expectError("Xóa bạn đọc không tồn tại", ReaderNotFoundException.class, "SV03",
                () -> readers.deleteReader("SV03"));

        // ------------------------------------------------------------------
        section("TEST 6: Mượn - trả bằng AppContext rồi kiểm tra thống kê");
        File dir2 = newDir();
        AppContext c2 = new AppContext(dir2.getPath());
        StatisticService st = c2.getStatisticService();
        check("Số đầu sách = 12, số cuốn trong kho = 41", st.countBookTitles() == 12 && st.countCopiesInStock() == 41);
        check("Số cuốn đang mượn = 3 (1 + 2)", st.countCopiesBorrowing() == 3);
        check("2 phiếu đang mượn, 2 phiếu đã trả",
                st.countTickets(TicketStatus.DANG_MUON) == 2 && st.countTickets(TicketStatus.DA_TRA) == 2);
        check("Tổng phí phạt = 30.000đ (20.000 + 10.000)", st.totalFines() == 30000);
        System.out.println("         tổng phạt hiển thị: " + MoneyFormatter.format(st.totalFines()));
        List<BorrowTicket> overdue = st.overdueTickets(LocalDate.now());
        check("1 phiếu quá hạn hôm nay (PM0001)", overdue.size() == 1 && overdue.get(0).getTicketId().equals("PM0001"));
        check("Phiếu mẫu PM0001 (SV thường) có hạn 14 ngày và quá hạn đúng 6 ngày",
                overdue.get(0).getDueDate().equals(overdue.get(0).getBorrowDate().plusDays(14))
                        && java.time.temporal.ChronoUnit.DAYS.between(overdue.get(0).getDueDate(), LocalDate.now()) == 6);
        check("Tính quá hạn với ngày giả định cách đây 40 ngày: chưa có phiếu nào quá hạn",
                st.overdueTickets(LocalDate.now().minusDays(40)).isEmpty());
        List<Map.Entry<String, Integer>> top = st.topBorrowedBooks(2);
        check("Top sách mượn nhiều: S001 (2 cuốn) đứng đầu, giới hạn 2 phần tử",
                top.size() == 2 && top.get(0).getKey().equals("S001") && top.get(0).getValue() == 2);
        check("Bạn đọc theo loại: 3 / 2 / 2",
                st.countReadersByType().get("Sinh viên thường") == 3
                        && st.countReadersByType().get("Sinh viên ưu tiên") == 2
                        && st.countReadersByType().get("Giảng viên") == 2);
        BorrowTicket nt = c2.getBorrowService().borrowBook("SV03", "S004", 1, LocalDate.now());
        check("Sau khi mượn thêm: đang mượn 4 cuốn, kho 40, mã phiếu PM0005",
                st.countCopiesBorrowing() == 4 && st.countCopiesInStock() == 40 && nt.getTicketId().equals("PM0005"));
        c2.getReturnService().returnTicket("PM0001", LocalDate.now());
        check("Trả PM0001 (quá hạn 6 ngày, SV thường): phí phạt tổng tăng thêm 30.000đ => 60.000đ",
                st.totalFines() == 60000);
        check("Không còn phiếu quá hạn sau khi trả", st.overdueTickets(LocalDate.now()).isEmpty());

        // ------------------------------------------------------------------
        section("TEST 7: Khởi động lại - đọc file .dat, KHÔNG ghi đè bằng dữ liệu mẫu");
        AppContext c3 = new AppContext(dir2.getPath());
        check("Số phiếu mượn sau khi mở lại = 5 (không bị reset về 4)", c3.getTicketRepo().getAll().size() == 5);
        check("Phiếu PM0001 vẫn ở trạng thái Đã trả với phí phạt 30.000đ",
                c3.getTicketRepo().findById("PM0001").getStatus() == TicketStatus.DA_TRA
                        && c3.getTicketRepo().findById("PM0001").getReturnRecord().getFine() == 30000);
        check("Đăng nhập vẫn dùng được sau khi mở lại", c3.getAuthService().login("thuthu", "123456") != null);
        AppContext c4 = new AppContext(dir.getPath());
        check("Dữ liệu đã sửa ở TEST 4/5 được lưu: không còn S005, có S013, có GV03",
                c4.getBookRepo().findById("S005") == null && c4.getBookRepo().findById("S013") != null
                        && c4.getReaderRepo().findById("GV03") instanceof LecturerReader);

        // ------------------------------------------------------------------
        section("TEST 8: Tiện ích (Validator, TextUtils, DateFormatter, IdGenerator)");
        check("SĐT hợp lệ 0901234567", Validator.isValidPhone("0901234567"));
        check("SĐT sai: thiếu số / không bắt đầu bằng 0 / có chữ",
                !Validator.isValidPhone("09012345") && !Validator.isValidPhone("1901234567")
                        && !Validator.isValidPhone("09012x4567") && !Validator.isValidPhone(null));
        check("removeAccents: 'Đại số tuyến tính' -> 'Dai so tuyen tinh'",
                TextUtils.removeAccents("Đại số tuyến tính").equals("Dai so tuyen tinh"));
        check("lastWord: 'Nguyễn Văn An' -> 'An'", TextUtils.lastWord("Nguyễn Văn An").equals("An"));
        check("Parse ngày 1/11/2026 và hiển thị 01/11/2026",
                DateFormatter.format(DateFormatter.parse("1/11/2026")).equals("01/11/2026"));
        expectError("Ngày không có thật 31/02/2026", DateTimeParseException.class, null,
                () -> DateFormatter.parse("31/02/2026"));
        check("MoneyFormatter 1234567 -> 1.234.567đ", MoneyFormatter.format(1234567).equals("1.234.567đ"));
        check("IdGenerator: danh sách rỗng -> PM0001 / S001 / SV01",
                IdGenerator.nextTicketId(new java.util.ArrayList<BorrowTicket>()).equals("PM0001")
                        && IdGenerator.nextBookId(new java.util.ArrayList<Book>()).equals("S001")
                        && IdGenerator.nextReaderId(new java.util.ArrayList<Reader>(), "SV").equals("SV01"));

        System.out.println("\n==============================");
        System.out.println("KẾT QUẢ: " + passed + " đạt, " + failed + " lỗi");
        System.out.println("==============================");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
