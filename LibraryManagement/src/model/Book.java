package model;

import java.io.Serializable;
import service.Borrowable;
import utils.MoneyFormatter;
import utils.TextUtils;

/** Sách trong thư viện. Implements Borrowable để biết kiểm tra / tăng / giảm số lượng. */
public class Book implements Borrowable, Comparable<Book>, Serializable {
    private static final long serialVersionUID = 1L;

    private String bookId;
    private String title;
    private String author;
    private String category;
    private int quantity;   // số lượng hiện có trong kho
    private double price;   // giá trị sách (đồng)

    public Book(String bookId, String title, String author, String category, int quantity, double price) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        setQuantity(quantity);
        this.price = price;
    }

    @Override
    public boolean isAvailable(int qty) {
        return qty > 0 && quantity >= qty;
    }

    @Override
    public void decrease(int qty) {
        if (!isAvailable(qty)) {
            throw new IllegalStateException("Không đủ số lượng sách để giảm: " + bookId);
        }
        quantity -= qty;
    }

    @Override
    public void increase(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Số lượng tăng phải lớn hơn 0");
        }
        quantity += qty;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng sách không được âm");
        }
        this.quantity = quantity;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    /** Thứ tự tự nhiên của sách: theo tên (không phân biệt dấu), trùng tên thì theo mã. */
    @Override
    public int compareTo(Book other) {
        int c = TextUtils.removeAccents(title).compareToIgnoreCase(TextUtils.removeAccents(other.title));
        return c != 0 ? c : bookId.compareTo(other.bookId);
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-34s | %-24s | %-12s | %4d | %s",
                bookId, title, author, category, quantity, MoneyFormatter.format(price));
    }
}
