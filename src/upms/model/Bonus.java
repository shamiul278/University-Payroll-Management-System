package upms.model;

public class Bonus {
    private String bonusId, type;
    private double amount;

    public Bonus() {}

    public Bonus(String bonusId, double amount, String type) {
        this.bonusId = bonusId; this.amount = amount; this.type = type;
    }

    public String getBonusId() { return bonusId; }
    public double getAmount()  { return amount; }
    public String getType()    { return type; }

    public void setBonusId(String v) { bonusId = v; }
    public void setAmount(double v)  { amount = v; }
    public void setType(String v)    { type = v; }

    @Override public String toString() { return bonusId + " - " + type + " (" + amount + ")"; }
}
