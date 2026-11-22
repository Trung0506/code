package main;

import model.Librarian;
import utils.InputHelper;

/** Menu chính: điều hướng tới các menu chức năng. */
public class MainMenu extends BaseMenu {

    private final Librarian user;

    public MainMenu(AppContext ctx, Librarian user) {
        super(ctx);
        this.user = user;
    }

    @Override
    public void run() {
        while (true) {
            printHeader("MENU CHÍNH - Thủ thư: " + user.getFullName());
            System.out.println("1. Quản lý sách");
            System.out.println("2. Quản lý bạn đọc");
            System.out.println("3. Mượn sách");
            System.out.println("4. Trả sách");
            System.out.println("5. Danh sách phiếu mượn");
            System.out.println("6. Thống kê");
            System.out.println("0. Đăng xuất");
            int choice = InputHelper.readInt("Chọn chức năng: ", 0, 6);
            switch (choice) {
                case 1:
                    new BookMenu(ctx).run();
                    break;
                case 2:
                    new ReaderMenu(ctx).run();
                    break;
                case 3:
                    new BorrowMenu(ctx).runBorrow();
                    break;
                case 4:
                    new BorrowMenu(ctx).runReturn();
                    break;
                case 5:
                    new BorrowMenu(ctx).run();
                    break;
                case 6:
                    new StatisticMenu(ctx).run();
                    break;
                default:
                    System.out.println("Đã đăng xuất.");
                    return;
            }
        }
    }
}
