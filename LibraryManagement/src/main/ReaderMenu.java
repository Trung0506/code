package main;

import exception.LibraryException;
import java.util.List;
import model.Reader;
import service.ReaderService;
import utils.InputHelper;

/** Menu quản lý bạn đọc. */
public class ReaderMenu extends BaseMenu {

    public ReaderMenu(AppContext ctx) {
        super(ctx);
    }

    @Override
    public void run() {
        while (true) {
            printHeader("QUẢN LÝ BẠN ĐỌC");
            System.out.println("1. Hiển thị danh sách bạn đọc");
            System.out.println("2. Thêm bạn đọc");
            System.out.println("3. Sửa bạn đọc");
            System.out.println("4. Xóa bạn đọc");
            System.out.println("5. Tìm kiếm (theo mã hoặc họ tên)");
            System.out.println("6. Sắp xếp danh sách");
            System.out.println("7. Xem quy định mượn - trả của một bạn đọc");
            System.out.println("0. Quay lại");
            int choice = InputHelper.readInt("Chọn chức năng: ", 0, 7);
            try {
                switch (choice) {
                    case 0:
                        return;
                    case 1:
                        printReaders(ctx.getReaderService().getAll());
                        break;
                    case 2:
                        addReader();
                        break;
                    case 3:
                        editReader();
                        break;
                    case 4:
                        deleteReader();
                        break;
                    case 5:
                        searchReader();
                        break;
                    case 6:
                        sortReaders();
                        break;
                    default:
                        showPolicy();
                        break;
                }
            } catch (LibraryException e) {
                showError(e);
            }
        }
    }

    private void printReaders(List<Reader> readers) {
        if (readers.isEmpty()) {
            System.out.println("(Không có bạn đọc nào)");
            return;
        }
        System.out.println(String.format("%-8s | %-22s | %-12s | %-17s | %s",
                "Mã", "Họ tên", "SĐT", "Loại bạn đọc", "Đang mượn"));
        System.out.println("---------+------------------------+--------------+-------------------+----------");
        for (Reader r : readers) {
            int borrowing = ctx.getBorrowService().countBorrowingBooks(r.getId());
            System.out.println(r + " | " + borrowing + "/" + r.getMaxBooks() + " cuốn");
        }
        System.out.println("Tổng: " + readers.size() + " bạn đọc");
    }

    private int chooseType() {
        System.out.println("  1. Sinh viên thường");
        System.out.println("  2. Sinh viên ưu tiên");
        System.out.println("  3. Giảng viên");
        return InputHelper.readInt("  Chọn loại bạn đọc: ", 1, 3);
    }

    private void addReader() throws LibraryException {
        System.out.println("--- Thêm bạn đọc mới ---");
        String name = InputHelper.readRequired("Họ tên: ");
        String phone = InputHelper.readRequired("Số điện thoại: ");
        int type = chooseType();
        Reader reader = ctx.getReaderService().addReader(type, name, phone);
        showOk("Đã thêm bạn đọc mã " + reader.getId() + " (" + reader.getReaderType() + ")");
    }

    private void editReader() throws LibraryException {
        String id = InputHelper.readRequired("Mã bạn đọc cần sửa: ");
        Reader reader = ctx.getReaderService().getById(id);
        System.out.println("Thông tin hiện tại: " + reader);
        System.out.println("(Nhấn Enter để giữ nguyên họ tên / SĐT)");
        String name = InputHelper.readStringOrKeep("Họ tên", reader.getFullName());
        String phone = InputHelper.readStringOrKeep("Số điện thoại", reader.getPhone());
        int type = ctx.getReaderService().typeOf(reader);
        if (InputHelper.confirm("  Đổi loại bạn đọc? (hiện tại: " + reader.getReaderType() + ")")) {
            type = chooseType();
        }
        Reader updated = ctx.getReaderService().updateReader(reader.getId(), type, name, phone);
        showOk("Đã cập nhật bạn đọc " + updated.getId() + " (" + updated.getReaderType() + ")");
    }

    private void deleteReader() throws LibraryException {
        String id = InputHelper.readRequired("Mã bạn đọc cần xóa: ");
        Reader reader = ctx.getReaderService().getById(id);
        System.out.println(reader);
        if (InputHelper.confirm("Xác nhận xóa bạn đọc này?")) {
            ctx.getReaderService().deleteReader(reader.getId());
            showOk("Đã xóa bạn đọc " + reader.getId());
        } else {
            System.out.println("Đã hủy.");
        }
    }

    private void searchReader() {
        String keyword = InputHelper.readRequired("Nhập mã hoặc họ tên cần tìm: ");
        List<Reader> result = ctx.getReaderService().search(keyword);
        if (result.isEmpty()) {
            System.out.println("Không tìm thấy bạn đọc nào khớp với \"" + keyword + "\".");
        } else {
            printReaders(result);
        }
    }

    private void sortReaders() {
        System.out.println("1. Theo tên (A-Z)");
        System.out.println("2. Theo mã bạn đọc");
        int choice = InputHelper.readInt("Chọn tiêu chí: ", 1, 2);
        if (choice == 1) {
            printReaders(ctx.getReaderService().sortByName());
        } else {
            printReaders(ctx.getReaderService().sortById());
        }
    }

    private void showPolicy() throws LibraryException {
        String id = InputHelper.readRequired("Mã bạn đọc: ");
        Reader reader = ctx.getReaderService().getById(id);
        // Truyền Reader vào tham số kiểu User (upcasting); describe() tự instanceof + ép kiểu
        System.out.println(ctx.getReaderInfoService().describe(reader));
        System.out.println("(Gợi ý: loại bạn đọc trong hệ thống: "
                + ReaderService.TYPE_STUDENT + " = SV thường, "
                + ReaderService.TYPE_PRIORITY + " = SV ưu tiên, "
                + ReaderService.TYPE_LECTURER + " = Giảng viên)");
    }
}
