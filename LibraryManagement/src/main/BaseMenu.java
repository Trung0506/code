package main;

/** Lớp cha của các menu: giữ AppContext và các hàm in dùng chung. */
public abstract class BaseMenu {

    protected final AppContext ctx;

    protected BaseMenu(AppContext ctx) {
        this.ctx = ctx;
    }

    /** Chạy vòng lặp menu cho tới khi người dùng chọn quay lại. */
    public abstract void run();

    protected void printHeader(String title) {
        System.out.println();
        System.out.println("======== " + title + " ========");
    }

    protected void showError(Exception e) {
        System.out.println("[LỖI] " + e.getMessage());
    }

    protected void showOk(String message) {
        System.out.println("[OK] " + message);
    }
}
