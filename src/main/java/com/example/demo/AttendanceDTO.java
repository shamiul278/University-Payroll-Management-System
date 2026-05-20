package com.example.demo;

import java.util.List;

public class AttendanceDTO {
    private long totalEmployees;
    private long loggedToday;
    private long onLeave;
    private int progressPercentage;
    private List<Object[]> attendanceRows;

    // No-Argument Constructor
    public AttendanceDTO() {}

    // Getters and Setters
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getLoggedToday() { return loggedToday; }
    public void setLoggedToday(long loggedToday) { this.loggedToday = loggedToday; }

    public long getOnLeave() { return onLeave; }
    public void setOnLeave(long onLeave) { this.onLeave = onLeave; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public List<Object[]> getAttendanceRows() { return attendanceRows; }
    public void setAttendanceRows(List<Object[]> attendanceRows) { this.attendanceRows = attendanceRows; }
}