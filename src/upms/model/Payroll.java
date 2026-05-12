package upms.model;

import java.util.Date;

public class Payroll {
    private String payrollId, month, paymentStatus, empId;
    private Date generatedDate;
    private double totalSalary;

    public Payroll() {}

    public Payroll(String payrollId, String month, Date generatedDate,
                   String paymentStatus, double totalSalary, String empId) {
        this.payrollId = payrollId; this.month = month;
        this.generatedDate = generatedDate; this.paymentStatus = paymentStatus;
        this.totalSalary = totalSalary; this.empId = empId;
    }

    public String getPayrollId()     { return payrollId; }
    public String getMonth()         { return month; }
    public Date   getGeneratedDate() { return generatedDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public double getTotalSalary()   { return totalSalary; }
    public String getEmpId()         { return empId; }

    public void setPayrollId(String v)     { payrollId = v; }
    public void setMonth(String v)         { month = v; }
    public void setGeneratedDate(Date v)   { generatedDate = v; }
    public void setPaymentStatus(String v) { paymentStatus = v; }
    public void setTotalSalary(double v)   { totalSalary = v; }
    public void setEmpId(String v)         { empId = v; }
}
