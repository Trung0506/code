package main;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import model.Book;
import model.BorrowTicket;
import model.TicketStatus;
import service.StatisticService;
import utils.DateFormatter;
import utils.InputHelper;
import utils.MoneyFormatter;

/** Menu thống kê. */
public class StatisticMenu extends BaseMenu {

    public StatisticMenu(AppContext ctx) {
        super(ctx);
    }

    @Override
    public void run() {
        StatisticService stat = ctx.getStatisticService();
        while (true) {
            printHeader("THỐNG KÊ");
            System.out.println("1. Tổng quan thư viện");
            System.out.println("2. Top 5 sách được mượn nhiều nhất");
            System.out.println("3. Tổng phí phạt đã thu");
            System.out.println("4. Phiếu mượn quá hạn chưa trả");
            System.out.println("5. Số bạn đọc theo loại");
            System.out.println("0. Quay lại");
            int choice = InputHelper.readInt("Chọn chức năng: ", 0, 5);
            switch (choice) {
                case 0:
                    return;
                case 1:
                    System.out.println("Số đầu sách            : " + stat.countBookTitles());
                    System.out.println("Số cuốn trong kho      : " + stat.countCopiesInStock());
                    System.out.println("Số cuốn đang được mượn : " + stat.countCopiesBorrowing());
                    System.out.println("Phiếu đang mượn        : " + stat.countTickets(TicketStatus.DANG_MUON));
                    System.out.println("Phiếu đã trả           : " + stat.countTickets(TicketStatus.DA_TRA));
                    System.out.println("Tổng phí phạt đã thu   : " + MoneyFormatter.format(stat.totalFines()));
                    break;
                case 2:
                    printTopBooks(stat.topBorrowedBooks(5));
                    break;
                case 3:
                    System.out.println("Tổng phí phạt đã thu: " + MoneyFormatter.format(stat.totalFines()));
                    break;
                case 4:
                    printOverdue(stat.overdueTickets(LocalDate.now()));
                    break;
                default:
                    for (Map.Entry<String, Integer> e : stat.countReadersByType().entrySet()) {
                        System.out.println(String.format("%-18s: %d", e.getKey(), e.getValue()));
                    }
                    break;
            }
        }
    }

    private void printTopBooks(List<Map.Entry<String, Integer>> top) {
        if (top.isEmpty()) {
            System.out.println("(Chưa có lượt mượn nào)");
            return;
        }
        int rank = 1;
        for (Map.Entry<String, Integer> e : top) {
            Book book = ctx.getBookRepo().findById(e.getKey());
            String title = (book == null) ? "(sách đã bị xóa)" : book.getTitle();
            System.out.println(String.format("#%d %-6s %-34s : %d lượt cuốn", rank++, e.getKey(), title, e.getValue()));
        }
    }

    private void printOverdue(List<BorrowTicket> overdue) {
        if (overdue.isEmpty()) {
            System.out.println("Không có phiếu nào quá hạn.");
            return;
        }
        LocalDate today = LocalDate.now();
        for (BorrowTicket t : overdue) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(t.getDueDate(), today);
            System.out.println(String.format("%s | Bạn đọc %s | Hẹn trả %s | Quá hạn %d ngày",
                    t.getTicketId(), t.getReaderId(), DateFormatter.format(t.getDueDate()), days));
        }
        System.out.println("Tổng: " + overdue.size() + " phiếu quá hạn");
    }
}
