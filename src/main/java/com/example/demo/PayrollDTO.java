package com.example.demo;

import java.util.List;

public class PayrollDTO {
    private double pendingPayout;
    private double totalDisbursed;
    private String nextCycleMonth;

    private double activeTotalGross;
    private double activeTotalDeduction;
    private double activeNetPayout;

    private List<Object[]> monthlyRecords;
    private List<Object[]> activeBreakdown;

    // No-Argument Constructor
    public PayrollDTO() {}

    // Getters and Setters
    public double getPendingPayout() { return pendingPayout; }
    public void setPendingPayout(double pendingPayout) { this.pendingPayout = pendingPayout; }

    public double getTotalDisbursed() { return totalDisbursed; }
    public void setTotalDisbursed(double totalDisbursed) { this.totalDisbursed = totalDisbursed; }

    public String getNextCycleMonth() { return nextCycleMonth; }
    public void setNextCycleMonth(String nextCycleMonth) { this.nextCycleMonth = nextCycleMonth; }

    public double getActiveTotalGross() { return activeTotalGross; }
    public void setActiveTotalGross(double activeTotalGross) { this.activeTotalGross = activeTotalGross; }

    public double getActiveTotalDeduction() { return activeTotalDeduction; }
    public void setActiveTotalDeduction(double activeTotalDeduction) { this.activeTotalDeduction = activeTotalDeduction; }

    public double getActiveNetPayout() { return activeNetPayout; }
    public void setActiveNetPayout(double activeNetPayout) { this.activeNetPayout = activeNetPayout; }

    public List<Object[]> getMonthlyRecords() { return monthlyRecords; }
    public void setMonthlyRecords(List<Object[]> monthlyRecords) { this.monthlyRecords = monthlyRecords; }

    public List<Object[]> getActiveBreakdown() { return activeBreakdown; }
    public void setActiveBreakdown(List<Object[]> activeBreakdown) { this.activeBreakdown = activeBreakdown; }
}