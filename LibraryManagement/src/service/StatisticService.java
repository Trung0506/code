package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Book;
import model.BorrowTicket;
import model.BorrowTicketDetail;
import model.Reader;
import model.TicketStatus;
import repository.BookRepository;
import repository.ReaderRepository;
import repository.TicketRepository;

/** Thống kê đơn giản: tồn kho, sách mượn nhiều, phí phạt, phiếu quá hạn, bạn đọc theo loại. */
public class StatisticService {

    private final BookRepository bookRepo;
    private final ReaderRepository readerRepo;
    private final TicketRepository ticketRepo;

    public StatisticService(BookRepository bookRepo, ReaderRepository readerRepo, TicketRepository ticketRepo) {
        this.bookRepo = bookRepo;
        this.readerRepo = readerRepo;
        this.ticketRepo = ticketRepo;
    }

    /** Số đầu sách trong thư viện. */
    public int countBookTitles() {
        return bookRepo.getAll().size();
    }

    /** Tổng số cuốn đang có sẵn trong kho. */
    public int countCopiesInStock() {
        int total = 0;
        for (Book b : bookRepo.getAll()) {
            total += b.getQuantity();
        }
        return total;
    }

    /** Tổng số cuốn đang được mượn (các phiếu "Đang mượn"). */
    public int countCopiesBorrowing() {
        int total = 0;
        for (BorrowTicket t : ticketRepo.getAll()) {
            if (t.getStatus() == TicketStatus.DANG_MUON) {
                total += t.getTotalQuantity();
            }
        }
        return total;
    }

    public int countTickets(TicketStatus status) {
        int count = 0;
        for (BorrowTicket t : ticketRepo.getAll()) {
            if (t.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /** Tổng phí phạt đã thu từ các phiếu đã trả. */
    public double totalFines() {
        double total = 0;
        for (BorrowTicket t : ticketRepo.getAll()) {
            if (t.getReturnRecord() != null) {
                total += t.getReturnRecord().getFine();
            }
        }
        return total;
    }

    /** Top sách được mượn nhiều nhất (tính cả phiếu đã trả): danh sách (mã sách, tổng số cuốn đã mượn). */
    public List<Map.Entry<String, Integer>> topBorrowedBooks(int limit) {
        Map<String, Integer> totals = new HashMap<String, Integer>();
        for (BorrowTicket t : ticketRepo.getAll()) {
            for (BorrowTicketDetail d : t.getDetails()) {
                totals.merge(d.getBookId(), d.getQuantity(), Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(totals.entrySet());
        entries.sort((a, b) -> {
            int c = b.getValue().compareTo(a.getValue());
            return c != 0 ? c : a.getKey().compareTo(b.getKey());
        });
        return new ArrayList<Map.Entry<String, Integer>>(entries.subList(0, Math.min(limit, entries.size())));
    }

    /** Các phiếu còn "Đang mượn" mà ngày hẹn trả đã qua so với ngày today. */
    public List<BorrowTicket> overdueTickets(LocalDate today) {
        List<BorrowTicket> result = new ArrayList<BorrowTicket>();
        for (BorrowTicket t : ticketRepo.getAll()) {
            if (t.getStatus() == TicketStatus.DANG_MUON && t.getDueDate().isBefore(today)) {
                result.add(t);
            }
        }
        return result;
    }

    /** Số bạn đọc theo từng loại (tên loại -> số lượng). */
    public Map<String, Integer> countReadersByType() {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Reader r : readerRepo.getAll()) {
            result.merge(r.getReaderType(), 1, Integer::sum);
        }
        return result;
    }
}
