package model;

/** Trạng thái phiếu mượn. */
public enum TicketStatus {
    DANG_MUON("Đang mượn"),
    DA_TRA("Đã trả");

    private final String label;

    TicketStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
