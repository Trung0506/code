package exception;

/** Bạn đọc mượn vượt quá số lượng sách tối đa cho phép. */
public class BorrowLimitExceededException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public BorrowLimitExceededException(String readerName, int maxBooks, int borrowing, int requested) {
        super(String.format(
            "Bạn đọc %s chỉ được mượn tối đa %d cuốn (đang mượn %d, muốn mượn thêm %d => vượt %d cuốn).",
            readerName, maxBooks, borrowing, requested, borrowing + requested - maxBooks));
    }
}
