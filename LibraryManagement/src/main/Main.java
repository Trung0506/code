package main;

import exception.LibraryException;
import model.Librarian;
import utils.InputHelper;

/**
 * Điểm khởi chạy chương trình: chỉ khởi tạo AppContext và điều hướng màn hình đăng nhập/tạo tài khoản -> menu chính.
 * Toàn bộ logic nằm ở service, giao diện nằm ở các lớp Menu.
 */
public class Main {

    public static void main(String[] args) {
        try {
            AppContext ctx = new AppContext("data");
            LoginMenu login = new LoginMenu(ctx);
            do {
                login.run();
                Librarian user = login.getLoggedIn();
                if (user == null) {
                    if (!login.isExitRequested()) {
                        System.out.println("Đăng nhập sai quá 3 lần. Thoát chương trình.");
                    }
                    break;
                }
                new MainMenu(ctx, user).run();
            } while (InputHelper.confirm("Đăng nhập bằng tài khoản khác?"));
            System.out.println("Tạm biệt!");
        } catch (LibraryException e) {
            System.out.println("[LỖI] Không khởi động được chương trình: " + e.getMessage());
        }
    }
}
