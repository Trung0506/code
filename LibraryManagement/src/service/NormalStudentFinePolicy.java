package service;

/** Phí phạt sinh viên thường: 5.000đ/ngày trễ. */
public class NormalStudentFinePolicy implements FinePolicy {

    private static final double RATE_PER_DAY = 5000;

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
