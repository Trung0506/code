package exception;

/** Không tìm thấy sách theo mã. */
public class BookNotFoundException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public BookNotFoundException(String bookId) {
        super("Không tìm thấy sách có mã: " + bookId);
    }
}
