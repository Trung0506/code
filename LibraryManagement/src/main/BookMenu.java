package main;

import exception.LibraryException;
import java.util.List;
import model.Book;
import utils.InputHelper;

/** Menu quản lý sách. */
public class BookMenu extends BaseMenu {

    public BookMenu(AppContext ctx) {
        super(ctx);
    }

    @Override
    public void run() {
        while (true) {
            printHeader("QUẢN LÝ SÁCH");
            System.out.println("1. Hiển thị danh sách sách");
            System.out.println("2. Thêm sách");
            System.out.println("3. Sửa sách");
            System.out.println("4. Xóa sách");
            System.out.println("5. Tìm kiếm (theo mã hoặc tên sách)");
            System.out.println("6. Sắp xếp danh sách");
            System.out.println("7. Kiểm tra sách còn đủ số lượng để mượn");
            System.out.println("0. Quay lại");
            int choice = InputHelper.readInt("Chọn chức năng: ", 0, 7);
            try {
                switch (choice) {
                    case 0:
                        return;
                    case 1:
                        printBooks(ctx.getBookService().getAll());
                        break;
                    case 2:
                        addBook();
                        break;
                    case 3:
                        editBook();
                        break;
                    case 4:
                        deleteBook();
                        break;
                    case 5:
                        searchBook();
                        break;
                    case 6:
                        sortBooks();
                        break;
                    default:
                        checkAvailable();
                        break;
                }
            } catch (LibraryException e) {
                showError(e);
            }
        }
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("(Không có sách nào)");
            return;
        }
        System.out.println(String.format("%-6s | %-34s | %-24s | %-12s | %-4s | %s",
                "Mã", "Tên sách", "Tác giả", "Thể loại", "SL", "Giá trị"));
        System.out.println("-------+------------------------------------+--------------------------+--------------+------+----------");
        for (Book b : books) {
            System.out.println(b);
        }
        System.out.println("Tổng: " + books.size() + " đầu sách");
    }

    private void addBook() throws LibraryException {
        System.out.println("--- Thêm sách mới ---");
        String title = InputHelper.readRequired("Tên sách: ");
        String author = InputHelper.readRequired("Tác giả: ");
        String category = InputHelper.readRequired("Thể loại: ");
        int quantity = InputHelper.readInt("Số lượng: ", 0, 100000);
        double price = InputHelper.readDouble("Giá trị sách (đồng): ", 0);
        Book book = ctx.getBookService().addBook(title, author, category, quantity, price);
        showOk("Đã thêm sách mã " + book.getBookId());
    }

    private void editBook() throws LibraryException {
        String id = InputHelper.readRequired("Mã sách cần sửa: ");
        Book book = ctx.getBookService().getById(id);
        System.out.println("Thông tin hiện tại: " + book);
        System.out.println("(Nhấn Enter để giữ nguyên)");
        String title = InputHelper.readStringOrKeep("Tên sách", book.getTitle());
        String author = InputHelper.readStringOrKeep("Tác giả", book.getAuthor());
        String category = InputHelper.readStringOrKeep("Thể loại", book.getCategory());
        int quantity = InputHelper.readIntOrKeep("Số lượng", book.getQuantity(), 0, 100000);
        double price = InputHelper.readDoubleOrKeep("Giá trị", book.getPrice(), 0);
        ctx.getBookService().updateBook(book.getBookId(), title, author, category, quantity, price);
        showOk("Đã cập nhật sách " + book.getBookId());
    }

    private void deleteBook() throws LibraryException {
        String id = InputHelper.readRequired("Mã sách cần xóa: ");
        Book book = ctx.getBookService().getById(id);
        System.out.println(book);
        if (InputHelper.confirm("Xác nhận xóa sách này?")) {
            ctx.getBookService().deleteBook(book.getBookId());
            showOk("Đã xóa sách " + book.getBookId());
        } else {
            System.out.println("Đã hủy.");
        }
    }

    private void searchBook() {
        String keyword = InputHelper.readRequired("Nhập mã hoặc tên sách cần tìm: ");
        List<Book> result = ctx.getBookService().search(keyword);
        if (result.isEmpty()) {
            System.out.println("Không tìm thấy sách nào khớp với \"" + keyword + "\".");
        } else {
            printBooks(result);
        }
    }

    private void sortBooks() {
        System.out.println("1. Theo tên sách (A-Z)");
        System.out.println("2. Theo số lượng giảm dần");
        System.out.println("3. Theo giá trị tăng dần");
        int choice = InputHelper.readInt("Chọn tiêu chí: ", 1, 3);
        if (choice == 1) {
            printBooks(ctx.getBookService().sortByTitle());
        } else if (choice == 2) {
            printBooks(ctx.getBookService().sortByQuantityDesc());
        } else {
            printBooks(ctx.getBookService().sortByPriceAsc());
        }
    }

    private void checkAvailable() throws LibraryException {
        String id = InputHelper.readRequired("Mã sách: ");
        Book book = ctx.getBookService().getById(id);
        int quantity = InputHelper.readInt("Số lượng muốn mượn: ", 1, 100000);
        if (ctx.getBookService().isAvailable(book.getBookId(), quantity)) {
            showOk("Sách \"" + book.getTitle() + "\" còn đủ " + quantity + " cuốn (hiện có " + book.getQuantity() + ").");
        } else {
            System.out.println("[KHÔNG ĐỦ] Sách \"" + book.getTitle() + "\" chỉ còn " + book.getQuantity()
                    + " cuốn, không đủ " + quantity + " cuốn.");
        }
    }
}
