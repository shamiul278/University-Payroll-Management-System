package upms.model;

import java.util.Date;

public class Attendance {
    private String attendanceId, status, empId;
    private Date date;

    public Attendance() {}

    public Attendance(String attendanceId, Date date, String status, String empId) {
        this.attendanceId = attendanceId; this.date = date;
        this.status = status; this.empId = empId;
    }

    public String getAttendanceId() { return attendanceId; }
    public Date   getDate()         { return date; }
    public String getStatus()       { return status; }
    public String getEmpId()        { return empId; }

    public void setAttendanceId(String v) { attendanceId = v; }
    public void setDate(Date v)           { date = v; }
    public void setStatus(String v)       { status = v; }
    public void setEmpId(String v)        { empId = v; }
}
