package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayrollService {

    @PersistenceContext
    private EntityManager entityManager;

    public PayrollDTO getPayrollDashboardData() {
        PayrollDTO dto = new PayrollDTO();

        // 1. Next Cycle Month Name
        LocalDate currentData = LocalDate.now();
        dto.setNextCycleMonth(currentData.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // 2. Pending Payout
        try {
            Object pending = entityManager.createNativeQuery("SELECT COALESCE(SUM(net_payout), 0) FROM payroll_cycles WHERE status = 'DRAFT'").getSingleResult();
            dto.setPendingPayout(pending instanceof BigDecimal ? ((BigDecimal) pending).doubleValue() : ((Number) pending).doubleValue());
        } catch (Exception e) { dto.setPendingPayout(0.0); }

        // 3. Total Disbursed
        try {
            Object disbursed = entityManager.createNativeQuery("SELECT COALESCE(SUM(net_payout), 0) FROM payroll_cycles WHERE status = 'DISBURSED'").getSingleResult();
            dto.setTotalDisbursed(disbursed instanceof BigDecimal ? ((BigDecimal) disbursed).doubleValue() : ((Number) disbursed).doubleValue());
        } catch (Exception e) { dto.setTotalDisbursed(0.0); }

        // 4. Monthly Records Table
        try {
            List<Object[]> records = entityManager.createNativeQuery("SELECT month_year, created_date, net_payout, status FROM payroll_cycles ORDER BY cycle_id DESC").getResultList();
            dto.setMonthlyRecords(records);
        } catch (Exception e) { dto.setMonthlyRecords(new ArrayList<>()); }

        // 5. Active Employee Breakdown (CRASH-PROOF LOGIC)
        try {
            String breakdownSql =
                    "SELECT e.emp_id, e.full_name, e.designation, " +
                            "COALESCE(s.total_package, 0) as base_allowance, " +
                            "500 as default_bonus, " +
                            "(COALESCE(s.total_package, 0)/30) * COALESCE((SELECT COUNT(*) FROM attendance_logs a WHERE a.emp_id = e.emp_id AND a.status = 'ABSENT'), 0) as penalty " +
                            "FROM employees e JOIN salary_configurations s ON e.emp_id = s.emp_id WHERE s.status = 'ACTIVE'";

            List<Object[]> breakdown = entityManager.createNativeQuery(breakdownSql).getResultList();
            dto.setActiveBreakdown(breakdown);

            // Safe Calculation avoiding Null Pointers
            double totalGross = 0;
            double totalPenalty = 0;

            for (Object[] row : breakdown) {
                double base = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                double bonus = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                double penalty = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;

                totalGross += (base + bonus);
                totalPenalty += penalty;
            }

            dto.setActiveTotalGross(totalGross);
            dto.setActiveTotalDeduction(totalPenalty);
            dto.setActiveNetPayout(totalGross - totalPenalty);

        } catch (Exception e) {
            System.out.println("Payroll Engine Error: " + e.getMessage()); // কনসোলে এরর প্রিন্ট করবে
            dto.setActiveBreakdown(new ArrayList<>());
        }

        return dto;
    }

    @Transactional
    public void executePayrollRun(String monthYear, double gross, double deductions, double net) {
        entityManager.createNativeQuery("INSERT INTO payroll_cycles (cycle_id, month_year, total_gross, total_deductions, net_payout, status) VALUES (cycle_seq.NEXTVAL, ?, ?, ?, ?, 'DISBURSED')")
                .setParameter(1, monthYear)
                .setParameter(2, gross)
                .setParameter(3, deductions)
                .setParameter(4, net)
                .executeUpdate();
    }
}