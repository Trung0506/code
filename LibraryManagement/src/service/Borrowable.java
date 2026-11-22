package service;

/** Hành vi của một đối tượng có thể được cho mượn (ví dụ: sách). */
public interface Borrowable {

    /** @return true nếu còn đủ số lượng cho yêu cầu mượn qty (qty phải > 0). */
    boolean isAvailable(int qty);

    /** Giảm số lượng hiện có khi cho mượn. */
    void decrease(int qty);

    /** Tăng lại số lượng hiện có khi trả. */
    void increase(int qty);
}
