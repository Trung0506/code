package service;

/** Phí phạt sinh viên ưu tiên: 3.000đ/ngày trễ. */
public class PriorityStudentFinePolicy implements FinePolicy {

    private static final double RATE_PER_DAY = 3000;

    @Override
    public double calculateFine(int lateDays) {
        if (lateDays <= 0) {
            return 0;
        }
        return lateDays * RATE_PER_DAY;
    }

    @Override
    public double getRatePerDay() {
        return RATE_PER_DAY;
    }
}
