package service;

import model.LecturerReader;
import model.PriorityStudentReader;
import model.Reader;
import model.StudentReader;
import model.User;
import utils.MoneyFormatter;

/**
 * Hiển thị thông tin quy định của người dùng.
 * Ví dụ UPCASTING / DOWNCASTING và instanceof theo yêu cầu đề bài.
 */
public class ReaderInfoService {

    /**
     * @param user tham số kiểu User (upcasting: StudentReader/LecturerReader... đều truyền vào được)
     * @return mô tả quy định mượn - trả của người dùng
     */
    public String describe(User user) {
        // Downcasting an toàn: kiểm tra instanceof rồi mới ép kiểu User -> Reader
        if (!(user instanceof Reader)) {
            return user.getFullName() + " (" + user.getRoleName() + ") không phải bạn đọc, không có quy định mượn sách.";
        }
        Reader reader = (Reader) user;

        // instanceof để thêm ghi chú riêng cho từng loại cụ thể
        String note;
        if (reader instanceof LecturerReader) {
            note = "Giảng viên: hạn mượn dài nhất, phạt thấp nhất.";
        } else if (reader instanceof PriorityStudentReader) {
            note = "Sinh viên ưu tiên: hạn mượn và mức phạt ưu đãi hơn sinh viên thường.";
        } else if (reader instanceof StudentReader) {
            note = "Sinh viên thường: áp dụng quy định chuẩn.";
        } else {
            note = "Loại bạn đọc khác.";
        }

        return String.format("%s - %s | Tối đa %d cuốn | Hạn mượn %d ngày | Phạt %s/ngày trễ | %s",
                reader.getFullName(), reader.getReaderType(), reader.getMaxBooks(),
                reader.getBorrowDays(), MoneyFormatter.format(reader.getFinePolicy().getRatePerDay()), note);
    }
}
