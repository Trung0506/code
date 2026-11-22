package service;

import exception.BookNotFoundException;
import exception.InvalidDataException;
import exception.ItemInUseException;
import exception.LibraryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Book;
import repository.BookRepository;
import repository.TicketRepository;
import utils.IdGenerator;
import utils.TextUtils;
import utils.Validator;

/** Nghiệp vụ quản lý sách: thêm, sửa, xóa, tìm kiếm, sắp xếp, kiểm tra số lượng. */
public class BookService {

    private final BookRepository bookRepo;
    private final TicketRepository ticketRepo;

    public BookService(BookRepository bookRepo, TicketRepository ticketRepo) {
        this.bookRepo = bookRepo;
        this.ticketRepo = ticketRepo;
    }

    /** Thêm sách mới, mã sách tự sinh (S001, S002...). */
    public Book addBook(String title, String author, String category, int quantity, double price)
            throws LibraryException {
        validate(title, author, category, quantity, price);
        Book book = new Book(IdGenerator.nextBookId(bookRepo.getAll()),
                title.trim(), author.trim(), category.trim(), quantity, price);
        bookRepo.add(book);
        bookRepo.save();
        return book;
    }

    /** Sửa thông tin sách (không đổi mã sách). */
    public Book updateBook(String bookId, String title, String author, String category, int quantity, double price)
            throws LibraryException {
        Book book = getById(bookId);
        validate(title, author, category, quantity, price);
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setCategory(category.trim());
        book.setQuantity(quantity);
        book.setPrice(price);
        bookRepo.save();
        return book;
    }

    /** Xóa sách; không cho xóa nếu sách đang nằm trong phiếu mượn chưa trả. */
    public void deleteBook(String bookId) throws LibraryException {
        Book book = getById(bookId);
        if (ticketRepo.hasActiveTicketWithBook(book.getBookId())) {
            throw new ItemInUseException("Không thể xóa sách " + book.getBookId()
                    + " vì đang có phiếu mượn chưa trả chứa sách này");
        }
        bookRepo.remove(book);
        bookRepo.save();
    }

    public Book getById(String bookId) throws BookNotFoundException {
        Book book = bookRepo.findById(bookId);
        if (book == null) {
            throw new BookNotFoundException(bookId);
        }
        return book;
    }

    /** @return bản sao danh sách sách (sắp xếp trên bản sao không làm đổi thứ tự lưu trữ). */
    public List<Book> getAll() {
        return new ArrayList<Book>(bookRepo.getAll());
    }

    /** Tìm theo mã sách hoặc tên sách, không phân biệt hoa thường và dấu. */
    public List<Book> search(String keyword) {
        List<Book> result = new ArrayList<Book>();
        if (Validator.isBlank(keyword)) {
            return result;
        }
        for (Book b : bookRepo.getAll()) {
            if (TextUtils.containsIgnoreAccent(b.getBookId(), keyword)
                    || TextUtils.containsIgnoreAccent(b.getTitle(), keyword)) {
                result.add(b);
            }
        }
        return result;
    }

    /** Kiểm tra sách còn đủ số lượng để mượn hay không. */
    public boolean isAvailable(String bookId, int quantity) throws BookNotFoundException {
        return getById(bookId).isAvailable(quantity);
    }

    /** Sắp xếp theo tên A-Z: dùng Comparable (Book.compareTo). */
    public List<Book> sortByTitle() {
        List<Book> list = getAll();
        Collections.sort(list);
        return list;
    }

    /** Sắp xếp theo số lượng giảm dần: dùng Comparator. */
    public List<Book> sortByQuantityDesc() {
        List<Book> list = getAll();
        list.sort((a, b) -> {
            int c = Integer.compare(b.getQuantity(), a.getQuantity());
            return c != 0 ? c : a.compareTo(b);
        });
        return list;
    }

    /** Sắp xếp theo giá trị tăng dần: dùng Comparator. */
    public List<Book> sortByPriceAsc() {
        List<Book> list = getAll();
        list.sort((a, b) -> {
            int c = Double.compare(a.getPrice(), b.getPrice());
            return c != 0 ? c : a.compareTo(b);
        });
        return list;
    }

    private void validate(String title, String author, String category, int quantity, double price)
            throws InvalidDataException {
        Validator.requireNotBlank(title, "Tên sách");
        Validator.requireNotBlank(author, "Tác giả");
        Validator.requireNotBlank(category, "Thể loại");
        if (quantity < 0) {
            throw new InvalidDataException("Số lượng sách không được âm");
        }
        Validator.requireNonNegative(price, "Giá trị sách");
    }
}
