package upms.model;

import java.util.Date;

public class Employee {
    private String empId, name, designation, email, phone, employmentType, deptId;
    private Date joinDate;

    public Employee() {}

    public Employee(String empId, String name, String designation, String email,
                    String phone, Date joinDate, String employmentType, String deptId) {
        this.empId = empId; this.name = name; this.designation = designation;
        this.email = email; this.phone = phone; this.joinDate = joinDate;
        this.employmentType = employmentType; this.deptId = deptId;
    }

    public String getEmpId()          { return empId; }
    public String getName()           { return name; }
    public String getDesignation()    { return designation; }
    public String getEmail()          { return email; }
    public String getPhone()          { return phone; }
    public Date   getJoinDate()       { return joinDate; }
    public String getEmploymentType() { return employmentType; }
    public String getDeptId()         { return deptId; }

    public void setEmpId(String v)          { empId = v; }
    public void setName(String v)           { name = v; }
    public void setDesignation(String v)    { designation = v; }
    public void setEmail(String v)          { email = v; }
    public void setPhone(String v)          { phone = v; }
    public void setJoinDate(Date v)         { joinDate = v; }
    public void setEmploymentType(String v) { employmentType = v; }
    public void setDeptId(String v)         { deptId = v; }

    @Override public String toString() { return name + " (" + empId + ")"; }
}
