package repository;

import model.Librarian;

/** Lưu tài khoản thủ thư (accounts.dat). */
public class AccountRepository extends BaseRepository<Librarian> {

    public AccountRepository(String filePath) {
        super(filePath);
    }

    /** @return thủ thư theo tên đăng nhập, hoặc null nếu không có. */
    public Librarian findByUsername(String username) {
        for (Librarian l : getAll()) {
            if (l.getUsername().equals(username)) {
                return l;
            }
        }
        return null;
    }
}
