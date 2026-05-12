package upms.model;

public class Salary {
    private String salaryId, empId;
    private double basicSalary, allowance;

    public Salary() {}

    public Salary(String salaryId, double basicSalary, double allowance, String empId) {
        this.salaryId = salaryId; this.basicSalary = basicSalary;
        this.allowance = allowance; this.empId = empId;
    }

    public String getSalaryId()    { return salaryId; }
    public double getBasicSalary() { return basicSalary; }
    public double getAllowance()    { return allowance; }
    public String getEmpId()       { return empId; }
    public double getGross()       { return basicSalary + allowance; }

    public void setSalaryId(String v)    { salaryId = v; }
    public void setBasicSalary(double v) { basicSalary = v; }
    public void setAllowance(double v)   { allowance = v; }
    public void setEmpId(String v)       { empId = v; }
}
