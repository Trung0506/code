package main;

import exception.InvalidDataException;
import exception.LibraryException;
import model.Librarian;
import service.AuthService;
import utils.InputHelper;

/**
 * Màn hình tạo tài khoản thủ thư (mở từ màn hình đăng nhập).
 * Chỉ nhập liệu và hiển thị; mọi kiểm tra hợp lệ nằm ở AuthService.register().
 */
public class RegisterMenu extends BaseMenu {

    public RegisterMenu(AppContext ctx) {
        super(ctx);
    }

    @Override
    public void run() {
        printHeader("TẠO TÀI KHOẢN THỦ THƯ");
        while (true) {
            String fullName = InputHelper.readLine("Họ tên (Enter để hủy): ");
            if (fullName.isEmpty()) {
                System.out.println("Đã hủy tạo tài khoản.");
                return;
            }
            String phone = InputHelper.readLine("Số điện thoại: ");
            String username = InputHelper.readLine("Tên đăng nhập (ít nhất "
                    + AuthService.MIN_USERNAME_LENGTH + " ký tự, không có khoảng trắng): ");
            String password = InputHelper.readPassword("Mật khẩu (ít nhất "
                    + AuthService.MIN_PASSWORD_LENGTH + " ký tự): ");
            String confirm = InputHelper.readPassword("Nhập lại mật khẩu: ");
            try {
                if (!password.equals(confirm)) {
                    throw new InvalidDataException("Mật khẩu nhập lại không khớp");
                }
                Librarian created = ctx.getAuthService().register(fullName, phone, username, password);
                showOk("Đã tạo tài khoản '" + created.getUsername() + "' (mã " + created.getId()
                        + "). Hãy chọn Đăng nhập để sử dụng.");
                return;
            } catch (LibraryException e) {
                showError(e);
                if (!InputHelper.confirm("Nhập lại thông tin?")) {
                    return;
                }
            }
        }
    }
}
