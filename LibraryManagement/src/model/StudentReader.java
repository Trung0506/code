package model;

import service.NormalStudentFinePolicy;
import service.FinePolicy;

/** Bạn đọc loại "Sinh viên thường": mượn tối đa 3 cuốn, 14 ngày. */
public class StudentReader extends Reader {
    private static final long serialVersionUID = 1L;

    private static final int MAX_BOOKS = 3;
    private static final int BORROW_DAYS = 14;

    public StudentReader(String id, String fullName, String phone) {
        super(id, fullName, phone);
    }

    @Override
    public String getReaderType() {
        return "Sinh viên thường";
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
        return new NormalStudentFinePolicy();
    }
}
