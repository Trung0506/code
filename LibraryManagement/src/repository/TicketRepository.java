package repository;

import model.BorrowTicket;
import model.BorrowTicketDetail;
import model.TicketStatus;

/** Lưu danh sách phiếu mượn (tickets.dat), bao gồm cả thông tin trả sách. */
public class TicketRepository extends BaseRepository<BorrowTicket> {

    public TicketRepository(String filePath) {
        super(filePath);
    }

    /** @return phiếu mượn theo mã, hoặc null nếu không có. */
    public BorrowTicket findById(String ticketId) {
        for (BorrowTicket t : getAll()) {
            if (t.getTicketId().equalsIgnoreCase(ticketId)) {
                return t;
            }
        }
        return null;
    }

    /** @return true nếu bạn đọc còn phiếu "Đang mượn". */
    public boolean hasActiveTicketOfReader(String readerId) {
        for (BorrowTicket t : getAll()) {
            if (t.getStatus() == TicketStatus.DANG_MUON && t.getReaderId().equalsIgnoreCase(readerId)) {
                return true;
            }
        }
        return false;
    }

    /** @return true nếu có phiếu "Đang mượn" chứa sách này. */
    public boolean hasActiveTicketWithBook(String bookId) {
        for (BorrowTicket t : getAll()) {
            if (t.getStatus() != TicketStatus.DANG_MUON) {
                continue;
            }
            for (BorrowTicketDetail d : t.getDetails()) {
                if (d.getBookId().equalsIgnoreCase(bookId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
