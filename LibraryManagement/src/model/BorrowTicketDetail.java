package model;

import java.io.Serializable;

/** Chi tiết một đầu sách trong phiếu mượn (lưu mã sách + số lượng). */
public class BorrowTicketDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookId;
    private int quantity;

    public BorrowTicketDetail(String bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return bookId + " x" + quantity;
    }
}
