package model;

import service.PriorityStudentFinePolicy;
import service.FinePolicy;

/** Bạn đọc loại "Sinh viên ưu tiên": mượn tối đa 5 cuốn, 21 ngày. */
public class PriorityStudentReader extends Reader {
    private static final long serialVersionUID = 1L;

    private static final int MAX_BOOKS = 5;
    private static final int BORROW_DAYS = 21;

    public PriorityStudentReader(String id, String fullName, String phone) {
        super(id, fullName, phone);
    }

    @Override
    public String getReaderType() {
        return "Sinh viên ưu tiên";
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
        return new PriorityStudentFinePolicy();
    }
}
