package service;

/**
 * Chính sách tính phí phạt trả sách trễ.
 * Muốn đổi chính sách / thêm loại bạn đọc mới: chỉ cần viết thêm một class implements
 * interface này, không phải sửa ReturnService (Open/Closed Principle).
 */
public interface FinePolicy {

    /**
     * @param lateDays số ngày trễ (> 0)
     * @return số tiền phạt (đồng)
     */
    double calculateFine(int lateDays);

    /** @return mức phạt mỗi ngày trễ (đồng), dùng để hiển thị. */
    double getRatePerDay();
}
