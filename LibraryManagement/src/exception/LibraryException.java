package exception;

/**
 * Lớp cha của mọi lỗi nghiệp vụ trong hệ thống thư viện.
 * Là checked exception nên nơi gọi (menu) bắt buộc phải xử lý và hiển thị lý do lỗi.
 */
public class LibraryException extends Exception {
    private static final long serialVersionUID = 1L;

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
