package model;

import java.io.Serializable;
import java.time.LocalDate;
import utils.DateFormatter;
import utils.MoneyFormatter;

/** Thông tin trả sách: ngày trả thực tế, số ngày trễ, phí phạt. */
public class ReturnRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ticketId;
    private LocalDate returnDate;
    private int lateDays;
    private double fine;

    public ReturnRecord(String ticketId, LocalDate returnDate, int lateDays, double fine) {
        this.ticketId = ticketId;
        this.returnDate = returnDate;
        this.lateDays = lateDays;
        this.fine = fine;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public int getLateDays() { return lateDays; }
    public void setLateDays(int lateDays) { this.lateDays = lateDays; }

    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    @Override
    public String toString() {
        return String.format("Phiếu %s | Trả ngày %s | Trễ %d ngày | Phạt %s",
                ticketId, DateFormatter.format(returnDate), lateDays, MoneyFormatter.format(fine));
    }
}
