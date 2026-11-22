package main;

import exception.LibraryException;
import java.io.File;
import repository.AccountRepository;
import repository.BookRepository;
import repository.ReaderRepository;
import repository.TicketRepository;
import service.AuthService;
import service.BookService;
import service.BorrowService;
import service.ReaderInfoService;
import service.ReaderService;
import service.ReturnService;
import service.SampleDataService;
import service.StatisticService;

/**
 * Khởi tạo và nối toàn bộ repository + service (một chỗ duy nhất), menu chỉ việc lấy ra dùng.
 * Nhờ vậy Main và các menu không chứa logic nghiệp vụ.
 */
public class AppContext {

    private final BookRepository bookRepo;
    private final ReaderRepository readerRepo;
    private final TicketRepository ticketRepo;
    private final AccountRepository accountRepo;

    private final BookService bookService;
    private final ReaderService readerService;
    private final BorrowService borrowService;
    private final ReturnService returnService;
    private final StatisticService statisticService;
    private final AuthService authService;
    private final ReaderInfoService readerInfoService;

    /**
     * @param dataDir thư mục chứa các file .dat (tự tạo nếu chưa có)
     * @throws LibraryException nếu không đọc được file dữ liệu
     */
    public AppContext(String dataDir) throws LibraryException {
        bookRepo = new BookRepository(new File(dataDir, "books.dat").getPath());
        readerRepo = new ReaderRepository(new File(dataDir, "readers.dat").getPath());
        ticketRepo = new TicketRepository(new File(dataDir, "tickets.dat").getPath());
        accountRepo = new AccountRepository(new File(dataDir, "accounts.dat").getPath());
        bookRepo.load();
        readerRepo.load();
        ticketRepo.load();
        accountRepo.load();

        bookService = new BookService(bookRepo, ticketRepo);
        readerService = new ReaderService(readerRepo, ticketRepo);
        borrowService = new BorrowService(bookRepo, readerRepo, ticketRepo);
        returnService = new ReturnService(bookRepo, readerRepo, ticketRepo);
        statisticService = new StatisticService(bookRepo, readerRepo, ticketRepo);
        authService = new AuthService(accountRepo);
        readerInfoService = new ReaderInfoService();

        new SampleDataService(accountRepo, bookRepo, readerRepo, ticketRepo,
                bookService, readerService, borrowService, returnService).seedIfEmpty();
    }

    public BookRepository getBookRepo() { return bookRepo; }
    public ReaderRepository getReaderRepo() { return readerRepo; }
    public TicketRepository getTicketRepo() { return ticketRepo; }
    public AccountRepository getAccountRepo() { return accountRepo; }
    public BookService getBookService() { return bookService; }
    public ReaderService getReaderService() { return readerService; }
    public BorrowService getBorrowService() { return borrowService; }
    public ReturnService getReturnService() { return returnService; }
    public StatisticService getStatisticService() { return statisticService; }
    public AuthService getAuthService() { return authService; }
    public ReaderInfoService getReaderInfoService() { return readerInfoService; }
}
