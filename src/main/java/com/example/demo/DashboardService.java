package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    public DashboardDTO getDashboardData() {
        DashboardDTO dto = new DashboardDTO();

        // 1. Total Monthly Payout
        try {
            Object totalPayout = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(amount), 0) FROM payroll_history WHERE status = 'COMPLETED'").getSingleResult();
            if (totalPayout instanceof BigDecimal) {
                dto.setTotalMonthlyPayout(((BigDecimal) totalPayout).doubleValue());
            } else {
                dto.setTotalMonthlyPayout(((Number) totalPayout).doubleValue());
            }
        } catch (Exception e) {
            dto.setTotalMonthlyPayout(0.0);
        }

        // 2. Pending Payrolls Count
        try {
            Object pendingCount = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM payroll_history WHERE status = 'PENDING'").getSingleResult();
            if (pendingCount instanceof BigDecimal) {
                dto.setPendingPayrollsCount(((BigDecimal) pendingCount).longValue());
            } else {
                dto.setPendingPayrollsCount(((Number) pendingCount).longValue());
            }
        } catch (Exception e) {
            dto.setPendingPayrollsCount(0L);
        }

        // 3. Average Attendance Completion
        try {
            Object avgAttendance = entityManager.createNativeQuery(
                    "SELECT COALESCE(AVG(attendance_pct), 0) FROM employees").getSingleResult();
            if (avgAttendance instanceof BigDecimal) {
                dto.setAttendanceCompletion(((BigDecimal) avgAttendance).doubleValue());
            } else {
                dto.setAttendanceCompletion(((Number) avgAttendance).doubleValue());
            }
        } catch (Exception e) {
            dto.setAttendanceCompletion(0.0);
        }

        // 4. Recent Disbursements
        try {
            Object recentStaff = entityManager.createNativeQuery(
                    "SELECT COUNT(DISTINCT emp_id) FROM payroll_history WHERE status = 'COMPLETED'").getSingleResult();
            if (recentStaff instanceof BigDecimal) {
                dto.setRecentDisbursementsCount(((BigDecimal) recentStaff).longValue());
            } else {
                dto.setRecentDisbursementsCount(((Number) recentStaff).longValue());
            }
        } catch (Exception e) {
            dto.setRecentDisbursementsCount(0L);
        }

        // 5. Recent Activity Table
        try {
            List<Object[]> activities = entityManager.createNativeQuery(
                    "SELECT name, department, base_salary, 'COMPLETED' FROM employees").getResultList();
            dto.setRecentActivities(activities);
        } catch (Exception e) {
            dto.setRecentActivities(new ArrayList<>());
        }

        return dto;
    }
}