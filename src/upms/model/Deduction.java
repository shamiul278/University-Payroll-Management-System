package upms.model;

public class Deduction {
    private String deductionId, reason;
    private double amount;

    public Deduction() {}

    public Deduction(String deductionId, double amount, String reason) {
        this.deductionId = deductionId; this.amount = amount; this.reason = reason;
    }

    public String getDeductionId() { return deductionId; }
    public double getAmount()      { return amount; }
    public String getReason()      { return reason; }

    public void setDeductionId(String v) { deductionId = v; }
    public void setAmount(double v)      { amount = v; }
    public void setReason(String v)      { reason = v; }

    @Override public String toString() { return deductionId + " - " + reason + " (" + amount + ")"; }
}
