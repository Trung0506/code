package service;

import exception.AuthenticationException;
import exception.InvalidDataException;
import exception.LibraryException;
import model.Librarian;
import repository.AccountRepository;
import utils.IdGenerator;
import utils.Validator;

/** Xác thực đăng nhập và tạo tài khoản thủ thư. */
public class AuthService {

    /** Độ dài tối thiểu của mật khẩu và của tên đăng nhập khi tạo tài khoản. */
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_USERNAME_LENGTH = 3;

    private final AccountRepository accountRepo;

    public AuthService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    /**
     * @return thủ thư đăng nhập thành công
     * @throws AuthenticationException nếu sai tên đăng nhập hoặc mật khẩu
     *         (cố ý không nói rõ sai cái nào để tránh dò tài khoản)
     */
    public Librarian login(String username, String password) throws AuthenticationException {
        Librarian librarian = (username == null) ? null : accountRepo.findByUsername(username.trim());
        if (librarian == null || !librarian.checkPassword(password)) {
            throw new AuthenticationException("Sai tên đăng nhập hoặc mật khẩu");
        }
        return librarian;
    }

    /**
     * Tạo tài khoản thủ thư mới (kiểm tra hết rồi mới ghi dữ liệu).
     *
     * @return tài khoản vừa tạo (mật khẩu được băm SHA-256, không lưu dạng chữ thường)
     * @throws InvalidDataException nếu họ tên/SĐT/tên đăng nhập/mật khẩu không hợp lệ
     *         hoặc tên đăng nhập đã tồn tại (không phân biệt hoa thường)
     */
    public Librarian register(String fullName, String phone, String username, String password)
            throws LibraryException {
        Validator.requireNotBlank(fullName, "Họ tên");
        Validator.requireNotBlank(username, "Tên đăng nhập");
        Validator.requireValidPhone(phone);

        String name = username.trim();
        if (name.length() < MIN_USERNAME_LENGTH || name.matches(".*\\s.*")) {
            throw new InvalidDataException("Tên đăng nhập phải có ít nhất " + MIN_USERNAME_LENGTH
                    + " ký tự và không chứa khoảng trắng");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidDataException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
        }
        for (Librarian existing : accountRepo.getAll()) {
            if (existing.getUsername().equalsIgnoreCase(name)) {
                throw new InvalidDataException("Tên đăng nhập '" + name + "' đã tồn tại");
            }
        }

        Librarian librarian = new Librarian(IdGenerator.nextAccountId(accountRepo.getAll()),
                fullName.trim(), phone.trim(), name, password);
        accountRepo.add(librarian);
        accountRepo.save();
        return librarian;
    }
}
