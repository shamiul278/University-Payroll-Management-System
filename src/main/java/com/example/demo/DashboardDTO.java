package com.example.demo;

import java.util.List;

public class DashboardDTO {
    private double totalMonthlyPayout;
    private long pendingPayrollsCount;
    private double attendanceCompletion;
    private long recentDisbursementsCount;
    private List<Object[]> recentActivities;

    // Getters and Setters
    public double getTotalMonthlyPayout() { return totalMonthlyPayout; }
    public void setTotalMonthlyPayout(double totalMonthlyPayout) { this.totalMonthlyPayout = totalMonthlyPayout; }

    public long getPendingPayrollsCount() { return pendingPayrollsCount; }
    public void setPendingPayrollsCount(long pendingPayrollsCount) { this.pendingPayrollsCount = pendingPayrollsCount; }

    public double getAttendanceCompletion() { return attendanceCompletion; }
    public void setAttendanceCompletion(double attendanceCompletion) { this.attendanceCompletion = attendanceCompletion; }

    public long getRecentDisbursementsCount() { return recentDisbursementsCount; }
    public void setRecentDisbursementsCount(long recentDisbursementsCount) { this.recentDisbursementsCount = recentDisbursementsCount; }

    public List<Object[]> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<Object[]> recentActivities ) { this.recentActivities = recentActivities; }
}