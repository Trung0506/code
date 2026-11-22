package exception;

/** Không tìm thấy bạn đọc theo mã. */
public class ReaderNotFoundException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public ReaderNotFoundException(String readerId) {
        super("Không tìm thấy bạn đọc có mã: " + readerId);
    }
}
