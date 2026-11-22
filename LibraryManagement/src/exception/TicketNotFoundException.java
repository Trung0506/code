package exception;

/** Không tìm thấy phiếu mượn theo mã. */
public class TicketNotFoundException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(String ticketId) {
        super("Không tìm thấy phiếu mượn có mã: " + ticketId);
    }
}
