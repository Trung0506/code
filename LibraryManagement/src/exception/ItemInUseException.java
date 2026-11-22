package exception;

/** Không thể sửa/xóa vì đối tượng đang được sử dụng (ví dụ: bạn đọc đang mượn sách). */
public class ItemInUseException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public ItemInUseException(String message) {
        super(message);
    }
}
