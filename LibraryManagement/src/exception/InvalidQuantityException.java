package exception;

/** Số lượng mượn không hợp lệ (nhỏ hơn hoặc bằng 0, hoặc phiếu không có sách nào). */
public class InvalidQuantityException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public InvalidQuantityException(String message) {
        super(message);
    }
}
