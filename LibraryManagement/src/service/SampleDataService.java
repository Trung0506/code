package service;

import exception.LibraryException;
import java.time.LocalDate;
import model.BorrowTicket;
import model.Librarian;
import repository.AccountRepository;
import repository.BookRepository;
import repository.ReaderRepository;
import repository.TicketRepository;

/**
 * Tạo dữ liệu mẫu lần chạy đầu tiên (khi file .dat chưa có) để demo ngay.
 * Dữ liệu được tạo qua chính các service nên luôn nhất quán với business rule.
 */
public class SampleDataService {

    private final AccountRepository accountRepo;
    private final BookRepository bookRepo;
    private final ReaderRepository readerRepo;
    private final TicketRepository ticketRepo;
    private final BookService bookService;
    private final ReaderService readerService;
    private final BorrowService borrowService;
    private final ReturnService returnService;

    public SampleDataService(AccountRepository accountRepo, BookRepository bookRepo, ReaderRepository readerRepo,
                             TicketRepository ticketRepo, BookService bookService, ReaderService readerService,
                             BorrowService borrowService, ReturnService returnService) {
        this.accountRepo = accountRepo;
        this.bookRepo = bookRepo;
        this.readerRepo = readerRepo;
        this.ticketRepo = ticketRepo;
        this.bookService = bookService;
        this.readerService = readerService;
        this.borrowService = borrowService;
        this.returnService = returnService;
    }

    /** Chỉ tạo phần nào đang trống; không bao giờ ghi đè dữ liệu có sẵn. */
    public void seedIfEmpty() throws LibraryException {
        if (accountRepo.isEmpty()) {
            accountRepo.add(new Librarian("TT01", "Quản trị thư viện", "0900000001", "admin", "admin123"));
            accountRepo.add(new Librarian("TT02", "Nguyễn Thị Thủ Thư", "0900000002", "thuthu", "123456"));
            accountRepo.save();
        }
        boolean seededBooks = false;
        boolean seededReaders = false;

        if (bookRepo.isEmpty()) {
            bookService.addBook("Lập trình Java cơ bản", "Nguyễn Văn Hiệp", "CNTT", 5, 150000);
            bookService.addBook("Cấu trúc dữ liệu và giải thuật", "Trần Đức Minh", "CNTT", 4, 120000);
            bookService.addBook("Cơ sở dữ liệu", "Lê Thanh Hà", "CNTT", 3, 130000);
            bookService.addBook("Mạng máy tính", "Phạm Quốc Bảo", "CNTT", 3, 140000);
            bookService.addBook("Giải tích 1", "Nguyễn Đình Trí", "Toán", 6, 85000);
            bookService.addBook("Đại số tuyến tính", "Lê Hồng Sơn", "Toán", 4, 80000);
            bookService.addBook("Vật lý đại cương", "Lương Duyên Bình", "Vật lý", 5, 95000);
            bookService.addBook("Tiếng Anh chuyên ngành CNTT", "Hoàng Văn Vân", "Ngoại ngữ", 2, 70000);
            bookService.addBook("Kinh tế vi mô", "Nguyễn Văn Công", "Kinh tế", 3, 90000);
            bookService.addBook("Triết học Mác - Lênin", "Bộ Giáo dục và Đào tạo", "Lý luận", 8, 60000);
            bookService.addBook("Clean Code", "Robert C. Martin", "CNTT", 1, 250000);
            bookService.addBook("Thiết kế hướng đối tượng với UML", "Đặng Văn Đức", "CNTT", 0, 110000);  // hết hàng để demo
            seededBooks = true;
        }
        if (readerRepo.isEmpty()) {
            readerService.addReader(ReaderService.TYPE_STUDENT, "Nguyễn Văn An", "0901000001");
            readerService.addReader(ReaderService.TYPE_STUDENT, "Trần Thị Bình", "0901000002");
            readerService.addReader(ReaderService.TYPE_STUDENT, "Lê Minh Châu", "0901000003");
            readerService.addReader(ReaderService.TYPE_PRIORITY, "Phạm Thu Dung", "0902000001");
            readerService.addReader(ReaderService.TYPE_PRIORITY, "Hoàng Gia Huy", "0902000002");
            readerService.addReader(ReaderService.TYPE_LECTURER, "Đỗ Quang Khải", "0903000001");
            readerService.addReader(ReaderService.TYPE_LECTURER, "Vũ Mai Lan", "0903000002");
            seededReaders = true;
        }
        if (seededBooks && seededReaders && ticketRepo.isEmpty()) {
            LocalDate today = LocalDate.now();
            // Phiếu 1: sinh viên thường (hạn 14 ngày), đang mượn và ĐÃ QUÁ HẠN 6 ngày
            borrowService.borrowBook("SV01", "S001", 1, today.minusDays(20));
            // Phiếu 2: sinh viên ưu tiên, đang mượn, còn hạn
            borrowService.borrowBook("UT01", "S002", 2, today.minusDays(10));
            // Phiếu 3: sinh viên thường, đã trả trễ 4 ngày => phạt 20.000đ
            BorrowTicket t3 = borrowService.borrowBook("SV02", "S001", 1, today.minusDays(30));
            returnService.returnTicket(t3.getTicketId(), today.minusDays(12));
            // Phiếu 4: giảng viên, đã trả trễ 5 ngày => phạt 10.000đ
            BorrowTicket t4 = borrowService.borrowBook("GV01", "S003", 1, today.minusDays(50));
            returnService.returnTicket(t4.getTicketId(), today.minusDays(15));
        }
    }
}
