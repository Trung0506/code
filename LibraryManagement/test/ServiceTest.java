import exception.BookNotFoundException;
import exception.BorrowLimitExceededException;
import exception.InvalidDateException;
import exception.InvalidQuantityException;
import exception.OutOfStockException;
import exception.ReaderNotFoundException;
import exception.TicketAlreadyReturnedException;
import exception.TicketNotFoundException;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import model.Book;
import model.BorrowTicket;
import model.LecturerReader;
import model.PriorityStudentReader;
import model.Reader;
import model.ReturnRecord;
import model.StudentReader;
import model.TicketStatus;
import model.User;
import repository.BookRepository;
import repository.ReaderRepository;
import repository.TicketRepository;
import service.BorrowService;
import service.ReaderInfoService;
import service.ReturnService;

/**
 * Bộ kiểm thử nghiệp vụ mượn - trả - phí phạt (không cần JUnit, chạy bằng main).
 * Mỗi nhóm test dùng một thư mục dữ liệu tạm riêng nên không ảnh hưởng file .dat thật.
 */
public class ServiceTest {

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

    /** Hành động phải ném đúng loại lỗi và (tuỳ chọn) thông báo chứa messagePart. */
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

    /** Dữ liệu mẫu + service dùng chung cho mỗi nhóm test. */
    static class Fixture {
        final File dir;
        final BookRepository books;
        final ReaderRepository readers;
        final TicketRepository tickets;
        final BorrowService borrow;
        final ReturnService ret;

        Fixture() throws Exception {
            dir = Files.createTempDirectory("library-test").toFile();
            books = new BookRepository(new File(dir, "books.dat").getPath());
            readers = new ReaderRepository(new File(dir, "readers.dat").getPath());
            tickets = new TicketRepository(new File(dir, "tickets.dat").getPath());

            // Upcasting: Reader r = new StudentReader(...) được thêm vào danh sách Reader
            books.add(new Book("S001", "Lập trình Java", "Cay Horstmann", "CNTT", 10, 150000));
            books.add(new Book("S002", "Cấu trúc dữ liệu", "Nguyễn Văn B", "CNTT", 1, 90000));
            books.add(new Book("S003", "Cơ sở dữ liệu", "Trần Văn C", "CNTT", 0, 120000));
            books.add(new Book("S004", "Mạng máy tính", "Phạm Quốc Bảo", "CNTT", 10, 140000));
            readers.add(new StudentReader("SV01", "Nguyễn Văn An", "0901000001"));
            readers.add(new PriorityStudentReader("UT01", "Trần Thị Bình", "0901000002"));
            readers.add(new LecturerReader("GV01", "Lê Văn Cường", "0901000003"));

            borrow = new BorrowService(books, readers, tickets);
            ret = new ReturnService(books, readers, tickets);
        }
    }

    public static void main(String[] args) throws Exception {
        final LocalDate d0 = LocalDate.of(2026, 11, 1);

        // ------------------------------------------------------------------
        section("TEST 1: Mượn sách còn số lượng thành công");
        final Fixture f = new Fixture();
        BorrowTicket t1 = f.borrow.borrowBook("SV01", "S001", 2, d0);
        check("phiếu có mã PM0001", t1.getTicketId().equals("PM0001"));
        check("trạng thái phiếu = Đang mượn", t1.getStatus() == TicketStatus.DANG_MUON);
        check("kho S001 giảm 10 -> 8", f.books.findById("S001").getQuantity() == 8);
        check("ngày mượn được lưu", t1.getBorrowDate().equals(d0));
        check("ngày hẹn trả = ngày mượn + 14 (sinh viên thường)", t1.getDueDate().equals(d0.plusDays(14)));

        // ------------------------------------------------------------------
        section("TEST 2: Các trường hợp mượn sách bị từ chối");
        expectError("Mượn sách đã hết (S003 = 0)", OutOfStockException.class, "đã hết",
                () -> f.borrow.borrowBook("GV01", "S003", 1, d0));
        expectError("Mượn nhiều hơn số còn lại (S002 còn 1, mượn 2)", OutOfStockException.class, "chỉ còn 1",
                () -> f.borrow.borrowBook("GV01", "S002", 2, d0));
        expectError("Sinh viên thường đang giữ 2, mượn thêm 2 => tổng 4 vượt tối đa 3", BorrowLimitExceededException.class, "tối đa 3",
                () -> f.borrow.borrowBook("SV01", "S001", 2, d0));
        check("kho S001 không đổi sau lần mượn bị từ chối (vẫn 8)", f.books.findById("S001").getQuantity() == 8);
        expectError("Số lượng mượn = 0", InvalidQuantityException.class, "lớn hơn 0",
                () -> f.borrow.borrowBook("GV01", "S001", 0, d0));
        expectError("Số lượng mượn < 0", InvalidQuantityException.class, "lớn hơn 0",
                () -> f.borrow.borrowBook("GV01", "S001", -1, d0));
        expectError("Sách không tồn tại", BookNotFoundException.class, "S999",
                () -> f.borrow.borrowBook("GV01", "S999", 1, d0));
        expectError("Bạn đọc không tồn tại", ReaderNotFoundException.class, "XX99",
                () -> f.borrow.borrowBook("XX99", "S001", 1, d0));

        final Map<String, Integer> mixed = new LinkedHashMap<String, Integer>();
        mixed.put("S001", 1);
        mixed.put("S003", 1);   // S003 đã hết -> cả phiếu phải bị từ chối, S001 không bị trừ
        expectError("Phiếu nhiều sách có 1 sách hết hàng", OutOfStockException.class, "đã hết",
                () -> f.borrow.borrowBooks("GV01", mixed, d0));
        check("Không trừ kho dở dang (S001 vẫn 8)", f.books.findById("S001").getQuantity() == 8);
        check("Không tạo phiếu thừa (vẫn 1 phiếu)", f.tickets.getAll().size() == 1);

        // ------------------------------------------------------------------
        section("TEST 3: Trả sách đúng hạn");
        ReturnRecord r1 = f.ret.returnTicket("PM0001", d0.plusDays(14));   // đúng ngày hẹn trả
        check("không trễ, phí phạt = 0", r1.getLateDays() == 0 && r1.getFine() == 0);
        check("kho S001 tăng lại 8 -> 10", f.books.findById("S001").getQuantity() == 10);
        check("trạng thái phiếu = Đã trả", f.tickets.findById("PM0001").getStatus() == TicketStatus.DA_TRA);
        check("ngày trả thực tế được lưu", f.tickets.findById("PM0001").getReturnRecord().getReturnDate().equals(d0.plusDays(14)));

        // ------------------------------------------------------------------
        section("TEST 4: Trả sách sai quy định");
        expectError("Trả lại phiếu đã ở trạng thái Đã trả", TicketAlreadyReturnedException.class, "Đã trả",
                () -> f.ret.returnTicket("PM0001", d0.plusDays(22)));
        check("kho không bị cộng lần 2 (vẫn 10)", f.books.findById("S001").getQuantity() == 10);
        expectError("Phiếu mượn không tồn tại", TicketNotFoundException.class, "PM9999",
                () -> f.ret.returnTicket("PM9999", d0));
        BorrowTicket t2 = f.borrow.borrowBook("SV01", "S001", 1, d0);
        expectError("Ngày trả trước ngày mượn", InvalidDateException.class, "trước ngày mượn",
                () -> f.ret.returnTicket("PM0002", d0.minusDays(1)));
        check("phiếu vẫn Đang mượn sau lần trả lỗi", t2.getStatus() == TicketStatus.DANG_MUON);

        // ------------------------------------------------------------------
        section("TEST 5: Trả quá hạn 3 ngày - phí phạt theo từng loại bạn đọc");
        ReturnRecord r2 = f.ret.returnTicket("PM0002", t2.getDueDate().plusDays(3));
        check("Sinh viên thường: 3 ngày x 5.000 = 15.000đ", r2.getLateDays() == 3 && r2.getFine() == 15000);

        BorrowTicket t3 = f.borrow.borrowBook("UT01", "S001", 1, d0);
        check("Sinh viên ưu tiên: hạn mượn 21 ngày", t3.getDueDate().equals(d0.plusDays(21)));
        ReturnRecord r3 = f.ret.returnTicket(t3.getTicketId(), t3.getDueDate().plusDays(3));
        check("Sinh viên ưu tiên: 3 ngày x 3.000 = 9.000đ", r3.getFine() == 9000);

        BorrowTicket t4 = f.borrow.borrowBook("GV01", "S001", 1, d0);
        check("Giảng viên: hạn mượn 30 ngày", t4.getDueDate().equals(d0.plusDays(30)));
        ReturnRecord r4 = f.ret.returnTicket(t4.getTicketId(), t4.getDueDate().plusDays(3));
        check("Giảng viên: 3 ngày x 2.000 = 6.000đ", r4.getFine() == 6000);

        BorrowTicket t5 = f.borrow.borrowBook("GV01", "S001", 1, d0);
        ReturnRecord r5 = f.ret.returnTicket(t5.getTicketId(), t5.getDueDate().plusDays(1));
        check("Trễ đúng 1 ngày vẫn bị phạt (2.000đ)", r5.getLateDays() == 1 && r5.getFine() == 2000);
        System.out.println("         " + r2);

        // ------------------------------------------------------------------
        section("TEST 6: Giới hạn số cuốn và hạn mượn theo loại bạn đọc (tính theo TỔNG số cuốn đang giữ)");
        final Fixture g = new Fixture();
        Reader rSv = g.readers.findById("SV01");
        Reader rUt = g.readers.findById("UT01");
        Reader rGv = g.readers.findById("GV01");
        check("Sinh viên thường: tối đa 3 cuốn / 14 ngày", rSv.getMaxBooks() == 3 && rSv.getBorrowDays() == 14);
        check("Sinh viên ưu tiên: tối đa 5 cuốn / 21 ngày", rUt.getMaxBooks() == 5 && rUt.getBorrowDays() == 21);
        check("Giảng viên: tối đa 10 cuốn / 30 ngày", rGv.getMaxBooks() == 10 && rGv.getBorrowDays() == 30);

        g.borrow.borrowBook("SV01", "S001", 3, d0);   // đúng bằng giới hạn 3 -> được phép
        check("SV thường mượn đúng bằng giới hạn (3/3) thành công", g.borrow.countBorrowingBooks("SV01") == 3);
        expectError("SV thường đã giữ 3/3, mượn thêm 1 cuốn (sách khác) bị từ chối", BorrowLimitExceededException.class, "vượt 1 cuốn",
                () -> g.borrow.borrowBook("SV01", "S004", 1, d0));
        check("Kho S004 không đổi sau lần bị từ chối (vẫn 10)", g.books.findById("S004").getQuantity() == 10);
        expectError("SV ưu tiên mượn 6 cuốn bị từ chối (tối đa 5)", BorrowLimitExceededException.class, "tối đa 5",
                () -> g.borrow.borrowBook("UT01", "S004", 6, d0));

        final Map<String, Integer> eleven = new LinkedHashMap<String, Integer>();
        eleven.put("S001", 6);
        eleven.put("S004", 5);
        expectError("Giảng viên mượn 11 cuốn trong một phiếu bị từ chối (tối đa 10)", BorrowLimitExceededException.class, "tối đa 10",
                () -> g.borrow.borrowBooks("GV01", eleven, d0));
        final Map<String, Integer> ten = new LinkedHashMap<String, Integer>();
        ten.put("S001", 5);
        ten.put("S004", 5);
        BorrowTicket gv = g.borrow.borrowBooks("GV01", ten, d0);
        check("Giảng viên mượn đúng 10 cuốn thành công, hạn trả sau 30 ngày",
                gv.getTotalQuantity() == 10 && gv.getDueDate().equals(d0.plusDays(30)));
        BorrowTicket ut = g.borrow.borrowBook("UT01", "S004", 5, d0);
        check("SV ưu tiên mượn đúng 5 cuốn thành công, hạn trả sau 21 ngày",
                ut.getTotalQuantity() == 5 && ut.getDueDate().equals(d0.plusDays(21)));
        g.ret.returnTicket("PM0001", d0.plusDays(1));
        check("Sau khi trả, số đang giữ về 0 và mượn lại được",
                g.borrow.countBorrowingBooks("SV01") == 0 && g.borrow.borrowBook("SV01", "S001", 1, d0) != null);

        // Cách hiểu "tối đa N cuốn": cộng dồn qua MỌI phiếu đang mượn, không chỉ riêng một phiếu
        final Fixture h = new Fixture();
        h.borrow.borrowBook("UT01", "S001", 3, d0);   // phiếu thứ nhất: 3 cuốn (SV ưu tiên tối đa 5)
        expectError("Phiếu thứ hai 3 cuốn: tổng 3 + 3 = 6 > 5 bị từ chối (cộng dồn nhiều phiếu)",
                BorrowLimitExceededException.class, "đang mượn 3, muốn mượn thêm 3 => vượt 1 cuốn",
                () -> h.borrow.borrowBook("UT01", "S004", 3, d0));
        final Map<String, Integer> fourInOne = new LinkedHashMap<String, Integer>();
        fourInOne.put("S001", 2);
        fourInOne.put("S004", 2);
        expectError("SV thường: một phiếu gồm 2 đầu sách, tổng 4 cuốn cũng bị từ chối (tối đa 3)",
                BorrowLimitExceededException.class, "tối đa 3",
                () -> h.borrow.borrowBooks("SV01", fourInOne, d0));
        h.borrow.borrowBook("UT01", "S004", 2, d0);   // 3 + 2 = 5 -> được
        check("Hai phiếu 3 + 2 = 5 cuốn thì được phép (SV ưu tiên)", h.borrow.countBorrowingBooks("UT01") == 5);

        // ------------------------------------------------------------------
        section("TEST 7: Ghi / đọc file nhị phân .dat");
        f.readers.save();   // danh sách bạn đọc do chức năng CRUD (Bạn B) lưu; ở đây ghi thủ công để kiểm tra
        BookRepository books2 = new BookRepository(new File(f.dir, "books.dat").getPath());
        ReaderRepository readers2 = new ReaderRepository(new File(f.dir, "readers.dat").getPath());
        TicketRepository tickets2 = new TicketRepository(new File(f.dir, "tickets.dat").getPath());
        books2.load();
        readers2.load();
        tickets2.load();
        check("đọc lại 4 sách", books2.getAll().size() == 4);
        check("đọc lại 5 phiếu mượn", tickets2.getAll().size() == 5);
        check("số lượng S001 sau đọc lại = 10", books2.findById("S001").getQuantity() == 10);
        check("phiếu PM0002 còn nguyên phí phạt 15.000đ", tickets2.findById("PM0002").getReturnRecord().getFine() == 15000);
        Reader gv2 = readers2.findById("GV01");
        check("đọc lại đúng kiểu con LecturerReader (đa hình được giữ)", gv2 instanceof LecturerReader);
        check("FinePolicy của giảng viên sau đọc lại = 2.000đ/ngày", gv2.getFinePolicy().getRatePerDay() == 2000);
        File missing = new File(f.dir, "chua-co.dat");
        BookRepository empty = new BookRepository(missing.getPath());
        empty.load();
        check("file chưa tồn tại -> danh sách rỗng, không lỗi", empty.getAll().isEmpty());

        // ------------------------------------------------------------------
        section("TEST 8: instanceof + ép kiểu (ReaderInfoService)");
        ReaderInfoService info = new ReaderInfoService();
        User asUser = new LecturerReader("GV09", "Phạm Giảng", "0");     // upcasting LecturerReader -> User
        String text = info.describe(asUser);
        System.out.println("         " + text);
        check("Nhận ra giảng viên: 10 cuốn / 30 ngày / 2.000đ", text.contains("10 cuốn") && text.contains("30 ngày") && text.contains("2.000"));
        User admin = new User("AD01", "Thủ thư Admin", "") {
            private static final long serialVersionUID = 1L;
            @Override
            public String getRoleName() {
                return "Thủ thư";
            }
        };
        check("User không phải Reader được nhận biết", info.describe(admin).contains("không phải bạn đọc"));

        // ------------------------------------------------------------------
        System.out.println("\n==============================");
        System.out.println("KẾT QUẢ: " + passed + " đạt, " + failed + " lỗi");
        System.out.println("==============================");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
