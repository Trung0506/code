package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import utils.DateFormatter;

/** Phiếu mượn sách: một bạn đọc, nhiều đầu sách (BorrowTicketDetail). */
public class BorrowTicket implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ticketId;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;                         // ngày hẹn trả
    private TicketStatus status;
    private ArrayList<BorrowTicketDetail> details;
    private ReturnRecord returnRecord;                 // null nếu chưa trả

    public BorrowTicket(String ticketId, String readerId, LocalDate borrowDate, LocalDate dueDate) {
        this.ticketId = ticketId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = TicketStatus.DANG_MUON;
        this.details = new ArrayList<>();
    }

    /** @return tổng số cuốn sách trong phiếu. */
    public int getTotalQuantity() {
        int total = 0;
        for (BorrowTicketDetail d : details) {
            total += d.getQuantity();
        }
        return total;
    }

    /** Đánh dấu phiếu đã trả: lưu thông tin trả và đổi trạng thái. */
    public void markReturned(ReturnRecord record) {
        this.returnRecord = record;
        this.status = TicketStatus.DA_TRA;
    }

    public String getTicketId() { return ticketId; }
    public String getReaderId() { return readerId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public ArrayList<BorrowTicketDetail> getDetails() { return details; }
    public ReturnRecord getReturnRecord() { return returnRecord; }

    public void addDetail(BorrowTicketDetail detail) {
        details.add(detail);
    }

    @Override
    public String toString() {
        return String.format("%-8s | Bạn đọc: %-6s | Mượn: %s | Hẹn trả: %s | %-9s | %s",
                ticketId, readerId, DateFormatter.format(borrowDate), DateFormatter.format(dueDate),
                status.getLabel(), details);
    }
}
