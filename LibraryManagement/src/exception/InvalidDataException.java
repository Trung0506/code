package exception;

/** Dữ liệu nhập vào không hợp lệ (để trống, sai định dạng, số âm...). */
public class InvalidDataException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }
}
