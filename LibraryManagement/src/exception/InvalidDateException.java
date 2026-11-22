package exception;

/** Ngày mượn / ngày trả không hợp lệ. */
public class InvalidDateException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public InvalidDateException(String message) {
        super(message);
    }
}
