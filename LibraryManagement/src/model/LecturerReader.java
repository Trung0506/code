package model;

import service.LecturerFinePolicy;
import service.FinePolicy;

/** Bạn đọc loại "Giảng viên": mượn tối đa 10 cuốn, 30 ngày. */
public class LecturerReader extends Reader {
    private static final long serialVersionUID = 1L;

    private static final int MAX_BOOKS = 10;
    private static final int BORROW_DAYS = 30;

    public LecturerReader(String id, String fullName, String phone) {
        super(id, fullName, phone);
    }

    @Override
    public String getReaderType() {
        return "Giảng viên";
    }

    @Override
    public int getMaxBooks() {
        return MAX_BOOKS;
    }

    @Override
    public int getBorrowDays() {
        return BORROW_DAYS;
    }

    @Override
    public FinePolicy getFinePolicy() {
        return new LecturerFinePolicy();
    }
}
