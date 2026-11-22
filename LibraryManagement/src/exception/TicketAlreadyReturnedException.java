package exception;

/** Phiếu mượn đã ở trạng thái Đã trả, không được trả lại lần nữa. */
public class TicketAlreadyReturnedException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public TicketAlreadyReturnedException(String ticketId) {
        super("Phiếu mượn " + ticketId + " đã ở trạng thái \"Đã trả\", không thể trả lại.");
    }
}
