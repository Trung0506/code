package service;

import exception.BookNotFoundException;
import exception.BorrowLimitExceededException;
import exception.InvalidDateException;
import exception.InvalidQuantityException;
import exception.LibraryException;
import exception.OutOfStockException;
import exception.ReaderNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import model.Book;
import model.BorrowTicket;
import model.BorrowTicketDetail;
import model.Reader;
import model.TicketStatus;
import repository.BookRepository;
import repository.ReaderRepository;
import repository.TicketRepository;
import utils.IdGenerator;

/**
 * Nghiệp vụ MƯỢN SÁCH.
 * Nguyên tắc: kiểm tra HẾT mọi điều kiện trước, chỉ khi hợp lệ mới thay đổi dữ liệu
 * (tránh trường hợp mượn dở dang: trừ kho một phần rồi mới báo lỗi).
 */
public class BorrowService {

    private final BookRepository bookRepo;
    private final ReaderRepository readerRepo;
    private final TicketRepository ticketRepo;

    public BorrowService(BookRepository bookRepo, ReaderRepository readerRepo, TicketRepository ticketRepo) {
        this.bookRepo = bookRepo;
        this.readerRepo = readerRepo;
        this.ticketRepo = ticketRepo;
    }

    /** Mượn một đầu sách (tiện dụng cho menu). */
    public BorrowTicket borrowBook(String readerId, String bookId, int quantity, LocalDate borrowDate)
            throws LibraryException {
        return borrowBooks(readerId, Collections.singletonMap(bookId, quantity), borrowDate);
    }

    /**
     * Mượn nhiều đầu sách trong một phiếu.
     *
     * @param requests  mã sách -> số lượng muốn mượn
     * @param borrowDate ngày mượn
     * @return phiếu mượn vừa tạo (trạng thái "Đang mượn")
     * @throws LibraryException với lý do lỗi cụ thể nếu vi phạm business rule
     */
    public BorrowTicket borrowBooks(String readerId, Map<String, Integer> requests, LocalDate borrowDate)
            throws LibraryException {

        // ---- 1. Bạn đọc phải tồn tại ----
        Reader reader = readerRepo.findById(readerId);
        if (reader == null) {
            throw new ReaderNotFoundException(readerId);
        }
        if (borrowDate == null) {
            throw new InvalidDateException("Ngày mượn không được để trống");
        }
        if (requests == null || requests.isEmpty()) {
            throw new InvalidQuantityException("Phiếu mượn phải có ít nhất một cuốn sách");
        }

        // ---- 2. Kiểm tra từng đầu sách: số lượng > 0, sách tồn tại, còn hàng ----
        int totalRequested = 0;
        for (Map.Entry<String, Integer> entry : requests.entrySet()) {
            String bookId = entry.getKey();
            int qty = entry.getValue();

            if (qty <= 0) {
                throw new InvalidQuantityException(
                        "Số lượng mượn của sách " + bookId + " phải lớn hơn 0 (nhập: " + qty + ")");
            }
            Book book = bookRepo.findById(bookId);
            if (book == null) {
                throw new BookNotFoundException(bookId);
            }
            if (!book.isAvailable(qty)) {
                if (book.getQuantity() == 0) {
                    throw new OutOfStockException(
                            "Sách \"" + book.getTitle() + "\" (" + bookId + ") đã hết, không thể cho mượn");
                }
                throw new OutOfStockException(
                        "Sách \"" + book.getTitle() + "\" (" + bookId + ") chỉ còn " + book.getQuantity()
                                + " cuốn, không đủ " + qty + " cuốn theo yêu cầu");
            }
            totalRequested += qty;
        }

        // ---- 3. Không được vượt số lượng tối đa của loại bạn đọc: TỔNG số cuốn đang giữ + số cuốn muốn mượn thêm ----
        //         (tính cộng dồn qua mọi phiếu "Đang mượn", không chỉ riêng phiếu đang tạo)
        int borrowing = countBorrowingBooks(reader.getId());
        if (borrowing + totalRequested > reader.getMaxBooks()) {
            throw new BorrowLimitExceededException(
                    reader.getFullName(), reader.getMaxBooks(), borrowing, totalRequested);
        }

        // ---- 4. Hợp lệ: tạo phiếu, giảm kho, lưu file ----
        BorrowTicket ticket = new BorrowTicket(
                IdGenerator.nextTicketId(ticketRepo.getAll()),
                reader.getId(),
                borrowDate,
                borrowDate.plusDays(reader.getBorrowDays()));   // ngày hẹn trả theo loại bạn đọc (đa hình)

        for (Map.Entry<String, Integer> entry : requests.entrySet()) {
            Book book = bookRepo.findById(entry.getKey());
            book.decrease(entry.getValue());
            ticket.addDetail(new BorrowTicketDetail(book.getBookId(), entry.getValue()));
        }
        ticketRepo.add(ticket);
        bookRepo.save();
        ticketRepo.save();
        return ticket;
    }

    /** @return tổng số cuốn bạn đọc đang mượn (các phiếu còn ở trạng thái "Đang mượn"). */
    public int countBorrowingBooks(String readerId) {
        int total = 0;
        for (BorrowTicket t : ticketRepo.getAll()) {
            if (t.getReaderId().equalsIgnoreCase(readerId) && t.getStatus() == TicketStatus.DANG_MUON) {
                total += t.getTotalQuantity();
            }
        }
        return total;
    }
}
