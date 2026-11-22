package main;

import exception.LibraryException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Book;
import model.BorrowTicket;
import model.BorrowTicketDetail;
import model.Reader;
import model.ReturnRecord;
import model.TicketStatus;
import repository.TicketRepository;
import utils.DateFormatter;
import utils.InputHelper;
import utils.MoneyFormatter;

/** Mượn sách, trả sách và xem danh sách phiếu mượn. */
public class BorrowMenu extends BaseMenu {

    public BorrowMenu(AppContext ctx) {
        super(ctx);
    }

    /** Menu xem danh sách / tra cứu phiếu mượn. */
    @Override
    public void run() {
        while (true) {
            printHeader("DANH SÁCH PHIẾU MƯỢN");
            System.out.println("1. Tất cả phiếu mượn");
            System.out.println("2. Phiếu đang mượn");
            System.out.println("3. Phiếu đã trả");
            System.out.println("4. Phiếu quá hạn (đang mượn, quá ngày hẹn trả)");
            System.out.println("5. Tra cứu phiếu theo mã");
            System.out.println("0. Quay lại");
            int choice = InputHelper.readInt("Chọn chức năng: ", 0, 5);
            TicketRepository repo = ctx.getTicketRepo();
            switch (choice) {
                case 0:
                    return;
                case 1:
                    printTickets(repo.getAll());
                    break;
                case 2:
                    printTickets(filter(TicketStatus.DANG_MUON));
                    break;
                case 3:
                    printTickets(filter(TicketStatus.DA_TRA));
                    break;
                case 4:
                    printTickets(ctx.getStatisticService().overdueTickets(LocalDate.now()));
                    break;
                default:
                    String id = InputHelper.readRequired("Mã phiếu: ");
                    BorrowTicket t = repo.findById(id);
                    if (t == null) {
                        System.out.println("[LỖI] Không tìm thấy phiếu mượn có mã: " + id);
                    } else {
                        printTicket(t);
                    }
                    break;
            }
        }
    }

    /** Chức năng mượn sách. */
    public void runBorrow() {
        printHeader("MƯỢN SÁCH");
        try {
            String readerId = InputHelper.readRequired("Mã bạn đọc: ");
            Reader reader = ctx.getReaderService().getById(readerId);
            int borrowing = ctx.getBorrowService().countBorrowingBooks(reader.getId());
            System.out.println(String.format("Bạn đọc: %s - %s | Đang mượn %d/%d cuốn | Hạn mượn %d ngày",
                    reader.getFullName(), reader.getReaderType(), borrowing,
                    reader.getMaxBooks(), reader.getBorrowDays()));

            Map<String, Integer> requests = new LinkedHashMap<String, Integer>();
            boolean more = true;
            while (more) {
                String bookId = InputHelper.readRequired("  Mã sách: ");
                try {
                    Book book = ctx.getBookService().getById(bookId);
                    System.out.println("  -> " + book.getTitle() + " (hiện có " + book.getQuantity() + " cuốn)");
                    int qty = InputHelper.readInt("  Số lượng muốn mượn: ");
                    requests.merge(book.getBookId(), qty, Integer::sum);
                } catch (LibraryException e) {
                    showError(e);
                }
                more = InputHelper.confirm("  Mượn thêm sách khác trong phiếu này?");
            }
            LocalDate borrowDate = InputHelper.readDate("Ngày mượn", LocalDate.now());

            BorrowTicket ticket = ctx.getBorrowService().borrowBooks(reader.getId(), requests, borrowDate);
            showOk("Đã tạo phiếu mượn thành công!");
            printTicket(ticket);
        } catch (LibraryException e) {
            System.out.println("[KHÔNG CHO MƯỢN] " + e.getMessage());
        }
    }

    /** Chức năng trả sách. */
    public void runReturn() {
        printHeader("TRẢ SÁCH");
        try {
            String ticketId = InputHelper.readRequired("Mã phiếu mượn: ");
            BorrowTicket ticket = ctx.getTicketRepo().findById(ticketId);
            if (ticket != null) {
                printTicket(ticket);
            }
            LocalDate returnDate = InputHelper.readDate("Ngày trả thực tế", LocalDate.now());

            ReturnRecord record = ctx.getReturnService().returnTicket(ticketId, returnDate);
            showOk("Đã trả sách thành công, phiếu chuyển sang trạng thái \"Đã trả\".");
            if (record.getLateDays() > 0) {
                System.out.println("  Trả QUÁ HẠN " + record.getLateDays() + " ngày => phí phạt: "
                        + MoneyFormatter.format(record.getFine()));
            } else {
                System.out.println("  Trả đúng hạn, không bị phạt.");
            }
        } catch (LibraryException e) {
            System.out.println("[KHÔNG TRẢ ĐƯỢC] " + e.getMessage());
        }
    }

    private List<BorrowTicket> filter(TicketStatus status) {
        List<BorrowTicket> result = new java.util.ArrayList<BorrowTicket>();
        for (BorrowTicket t : ctx.getTicketRepo().getAll()) {
            if (t.getStatus() == status) {
                result.add(t);
            }
        }
        return result;
    }

    private void printTickets(List<BorrowTicket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("(Không có phiếu mượn nào)");
            return;
        }
        for (BorrowTicket t : tickets) {
            printTicket(t);
        }
        System.out.println("Tổng: " + tickets.size() + " phiếu");
    }

    /** In chi tiết một phiếu: bạn đọc, ngày, tên sách, thông tin trả (nếu có). */
    private void printTicket(BorrowTicket t) {
        Reader reader = ctx.getReaderRepo().findById(t.getReaderId());
        String readerText = (reader == null)
                ? t.getReaderId() + " (bạn đọc đã bị xóa)"
                : t.getReaderId() + " - " + reader.getFullName() + " (" + reader.getReaderType() + ")";
        System.out.println("Phiếu " + t.getTicketId() + " | Bạn đọc: " + readerText);
        System.out.println("   Ngày mượn: " + DateFormatter.format(t.getBorrowDate())
                + " | Hẹn trả: " + DateFormatter.format(t.getDueDate())
                + " | Trạng thái: " + t.getStatus().getLabel());
        for (BorrowTicketDetail d : t.getDetails()) {
            Book book = ctx.getBookRepo().findById(d.getBookId());
            String title = (book == null) ? "(sách đã bị xóa)" : book.getTitle();
            System.out.println("   - " + d.getBookId() + " " + title + " x" + d.getQuantity());
        }
        ReturnRecord r = t.getReturnRecord();
        if (r != null) {
            System.out.println("   Đã trả ngày " + DateFormatter.format(r.getReturnDate())
                    + " | Trễ " + r.getLateDays() + " ngày | Phạt " + MoneyFormatter.format(r.getFine()));
        }
    }
}
