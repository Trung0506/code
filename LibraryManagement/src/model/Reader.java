package model;

import service.FinePolicy;

/**
 * Bạn đọc thư viện (abstract class, kế thừa User).
 * Mỗi loại bạn đọc override các phương thức trừu tượng để có quy định riêng
 * (số cuốn tối đa, hạn mượn, chính sách phạt)
 * => ReturnService / BorrowService không cần if-else theo loại bạn đọc (đa hình).
 */
public abstract class Reader extends User {
    private static final long serialVersionUID = 1L;

    public Reader(String id, String fullName, String phone) {
        super(id, fullName, phone);
    }

    /** @return tên loại bạn đọc, ví dụ "Sinh viên thường". */
    public abstract String getReaderType();

    /** @return số cuốn sách tối đa được giữ cùng lúc (tổng trên mọi phiếu đang mượn). */
    public abstract int getMaxBooks();

    /** @return số ngày được mượn (để tính ngày hẹn trả). */
    public abstract int getBorrowDays();

    /** @return chính sách phạt áp dụng cho loại bạn đọc này. */
    public abstract FinePolicy getFinePolicy();

    @Override
    public String getRoleName() {
        return getReaderType();
    }
}
