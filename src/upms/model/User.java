package upms.model;

public class User {
    private String userId, username, password, role, empId;

    public User() {}

    public User(String userId, String username, String password, String role, String empId) {
        this.userId = userId; this.username = username; this.password = password;
        this.role = role; this.empId = empId;
    }

    public String getUserId()   { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }
    public String getEmpId()    { return empId; }

    public void setUserId(String v)   { userId = v; }
    public void setUsername(String v) { username = v; }
    public void setPassword(String v) { password = v; }
    public void setRole(String v)     { role = v; }
    public void setEmpId(String v)    { empId = v; }

    @Override public String toString() { return username + " [" + role + "]"; }
}
