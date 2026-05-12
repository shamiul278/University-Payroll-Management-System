package upms.model;

public class Department {
    private String deptId, deptName, building, contactNo;

    public Department() {}

    public Department(String deptId, String deptName, String building, String contactNo) {
        this.deptId = deptId; this.deptName = deptName;
        this.building = building; this.contactNo = contactNo;
    }

    public String getDeptId()    { return deptId; }
    public String getDeptName()  { return deptName; }
    public String getBuilding()  { return building; }
    public String getContactNo() { return contactNo; }

    public void setDeptId(String v)    { deptId = v; }
    public void setDeptName(String v)  { deptName = v; }
    public void setBuilding(String v)  { building = v; }
    public void setContactNo(String v) { contactNo = v; }

    @Override public String toString() { return deptName; }
}
