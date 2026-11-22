package main;

import exception.AuthenticationException;
import model.Librarian;
import utils.InputHelper;

/**
 * Màn hình đầu tiên: chọn Đăng nhập (tối đa 3 lần thử), Tạo tài khoản mới hoặc Thoát.
 */
public class LoginMenu extends BaseMenu {

    private static final int MAX_ATTEMPTS = 3;
    private Librarian loggedIn;
    private boolean exitRequested;

    public LoginMenu(AppContext ctx) {
        super(ctx);
    }

    @Override
    public void run() {
        loggedIn = null;
        exitRequested = false;
        while (true) {
            printHeader("HỆ THỐNG QUẢN LÝ THƯ VIỆN");
            System.out.println("1. Đăng nhập");
            System.out.println("2. Tạo tài khoản mới");
            System.out.println("0. Thoát chương trình");
            int choice = InputHelper.readInt("Chọn chức năng: ", 0, 2);
            switch (choice) {
                case 1:
                    login();
                    return;           // đăng nhập thành công hoặc sai quá số lần cho phép
                case 2:
                    new RegisterMenu(ctx).run();
                    break;            // tạo xong thì quay lại màn hình chọn
                default:
                    exitRequested = true;
                    return;
            }
        }
    }

    /** Hỏi tên đăng nhập + mật khẩu, tối đa MAX_ATTEMPTS lần. */
    private void login() {
        printHeader("ĐĂNG NHẬP HỆ THỐNG QUẢN LÝ THƯ VIỆN");
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String username = InputHelper.readLine("Tên đăng nhập: ");
            String password = InputHelper.readPassword("Mật khẩu: ");
            try {
                loggedIn = ctx.getAuthService().login(username, password);
                showOk("Đăng nhập thành công. Xin chào " + loggedIn.getFullName() + "!");
                return;
            } catch (AuthenticationException e) {
                showError(e);
                System.out.println("  (Còn " + (MAX_ATTEMPTS - attempt) + " lần thử)");
            }
        }
    }

    /** @return thủ thư vừa đăng nhập, hoặc null nếu đăng nhập thất bại / người dùng chọn thoát. */
    public Librarian getLoggedIn() {
        return loggedIn;
    }

    /** @return true nếu người dùng chủ động chọn "Thoát chương trình". */
    public boolean isExitRequested() {
        return exitRequested;
    }
}
