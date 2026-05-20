package com.example.demo;

import java.util.List;

public class SalaryDTO {
    private double totalAllocated;
    private long pendingUpdates;
    private List<Object[]> recentLogs;

    // No-Argument Constructor
    public SalaryDTO() {}

    // Getters and Setters
    public double getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(double totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public long getPendingUpdates() {
        return pendingUpdates;
    }

    public void setPendingUpdates(long pendingUpdates) {
        this.pendingUpdates = pendingUpdates;
    }

    public List<Object[]> getRecentLogs() {
        return recentLogs;
    }

    public void setRecentLogs(List<Object[]> recentLogs) {
        this.recentLogs = recentLogs;
    }
}