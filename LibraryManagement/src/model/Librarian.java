package model;

import utils.PasswordUtil;

/**
 * Thủ thư - tài khoản đăng nhập hệ thống. Kế thừa User nhưng KHÔNG phải Reader
 * (nên ReaderInfoService.describe() nhận biết được bằng instanceof).
 */
public class Librarian extends User {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;   // chỉ lưu mã băm SHA-256

    public Librarian(String id, String fullName, String phone, String username, String rawPassword) {
        super(id, fullName, phone);
        this.username = username;
        this.passwordHash = PasswordUtil.sha256(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        return rawPassword != null && passwordHash.equals(PasswordUtil.sha256(rawPassword));
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getRoleName() {
        return "Thủ thư";
    }
}
