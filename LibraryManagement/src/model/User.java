package model;

import java.io.Serializable;

/**
 * Người dùng chung của hệ thống (abstract class).
 * Thuộc tính private (Encapsulation), lớp con kế thừa qua getter/setter.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String fullName;
    private String phone;

    public User(String id, String fullName, String phone) {
        setId(id);
        setFullName(fullName);
        setPhone(phone);
    }

    /** @return tên vai trò của người dùng, mỗi lớp con tự định nghĩa (đa hình). */
    public abstract String getRoleName();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã người dùng không được để trống");
        }
        this.id = id.trim();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        this.fullName = fullName.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone == null) ? "" : phone.trim();
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-22s | %-12s | %s", id, fullName, phone, getRoleName());
    }
}
