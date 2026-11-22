package exception;

/** Sách đã hết hoặc không đủ số lượng để cho mượn. */
public class OutOfStockException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public OutOfStockException(String message) {
        super(message);
    }
}
