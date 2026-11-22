package exception;

/** Lỗi khi đọc/ghi file dữ liệu (.dat). */
public class DataStorageException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
