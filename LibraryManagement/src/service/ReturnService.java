package service;

import exception.BookNotFoundException;
import exception.InvalidDateException;
import exception.LibraryException;
import exception.ReaderNotFoundException;
import exception.TicketAlreadyReturnedException;
import exception.TicketNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import model.Book;
import model.BorrowTicket;
import model.BorrowTicketDetail;
import model.Reader;
import model.ReturnRecord;
import model.TicketStatus;
import repository.BookRepository;
import repository.ReaderRepository;
import repository.TicketRepository;

/** Nghiệp vụ TRẢ SÁCH và tính phí phạt. */
public class ReturnService {

    private final BookRepository bookRepo;
    private final ReaderRepository readerRepo;
    private final TicketRepository ticketRepo;

    public ReturnService(BookRepository bookRepo, ReaderRepository readerRepo, TicketRepository ticketRepo) {
        this.bookRepo = bookRepo;
        this.readerRepo = readerRepo;
        this.ticketRepo = ticketRepo;
    }

    /**
     * Trả sách theo phiếu mượn.
     *
     * @param returnDate ngày trả thực tế
     * @return thông tin trả sách (có số ngày trễ và phí phạt)
     * @throws LibraryException nếu phiếu không tồn tại, đã trả, hoặc ngày trả không hợp lệ
     */
    public ReturnRecord returnTicket(String ticketId, LocalDate returnDate) throws LibraryException {

        // ---- 1. Phiếu phải tồn tại và đang ở trạng thái "Đang mượn" ----
        BorrowTicket ticket = ticketRepo.findById(ticketId);
        if (ticket == null) {
            throw new TicketNotFoundException(ticketId);
        }
        if (ticket.getStatus() == TicketStatus.DA_TRA) {
            throw new TicketAlreadyReturnedException(ticketId);
        }
        if (returnDate == null) {
            throw new InvalidDateException("Ngày trả không được để trống");
        }
        if (returnDate.isBefore(ticket.getBorrowDate())) {
            throw new InvalidDateException("Ngày trả (" + returnDate
                    + ") không thể trước ngày mượn (" + ticket.getBorrowDate() + ")");
        }

        // ---- 2. Dữ liệu liên quan phải đầy đủ (kiểm tra trước khi sửa bất cứ thứ gì) ----
        Reader reader = readerRepo.findById(ticket.getReaderId());
        if (reader == null) {
            throw new ReaderNotFoundException(ticket.getReaderId());
        }
        for (BorrowTicketDetail d : ticket.getDetails()) {
            if (bookRepo.findById(d.getBookId()) == null) {
                throw new BookNotFoundException(d.getBookId());
            }
        }

        // ---- 3. Tính số ngày trễ và phí phạt (đa hình: mỗi loại bạn đọc một FinePolicy) ----
        int lateDays = (int) ChronoUnit.DAYS.between(ticket.getDueDate(), returnDate);
        double fine = 0;
        if (lateDays > 0) {
            fine = reader.getFinePolicy().calculateFine(lateDays);
            if (fine <= 0) {
                // Business rule: trả quá hạn BẮT BUỘC phải tính phí phạt
                throw new IllegalStateException(
                        "Chính sách phạt cấu hình sai: trễ " + lateDays + " ngày nhưng phí phạt = " + fine);
            }
        } else {
            lateDays = 0;
        }

        // ---- 4. Tăng lại số lượng sách, cập nhật phiếu, lưu file ----
        for (BorrowTicketDetail d : ticket.getDetails()) {
            Book book = bookRepo.findById(d.getBookId());
            book.increase(d.getQuantity());
        }
        ReturnRecord record = new ReturnRecord(ticket.getTicketId(), returnDate, lateDays, fine);
        ticket.markReturned(record);      // trạng thái -> "Đã trả"

        bookRepo.save();
        ticketRepo.save();
        return record;
    }
}
