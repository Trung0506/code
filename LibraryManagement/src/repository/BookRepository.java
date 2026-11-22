package repository;

import model.Book;

/** Lưu danh sách sách (books.dat). */
public class BookRepository extends BaseRepository<Book> {

    public BookRepository(String filePath) {
        super(filePath);
    }

    /** @return sách theo mã, hoặc null nếu không có. */
    public Book findById(String bookId) {
        for (Book b : getAll()) {
            if (b.getBookId().equalsIgnoreCase(bookId)) {
                return b;
            }
        }
        return null;
    }
}
