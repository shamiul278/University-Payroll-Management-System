package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalaryService {

    @PersistenceContext
    private EntityManager entityManager;

    public SalaryDTO getSalaryDashboardData() {
        SalaryDTO dto = new SalaryDTO();

        // 1. Total Allocated Salary (ACTIVE স্যালারিগুলোর SUM)
        try {
            Object total = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(total_package), 0) FROM salary_configurations WHERE status = 'ACTIVE'").getSingleResult();
            dto.setTotalAllocated(total instanceof BigDecimal ? ((BigDecimal) total).doubleValue() : ((Number) total).doubleValue());
        } catch (Exception e) {
            dto.setTotalAllocated(0.0);
        }

        // 2. Pending Updates Count (PENDING স্যালারির COUNT)
        try {
            Object pending = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM salary_configurations WHERE status = 'PENDING'").getSingleResult();
            dto.setPendingUpdates(pending instanceof BigDecimal ? ((BigDecimal) pending).longValue() : ((Number) pending).longValue());
        } catch (Exception e) {
            dto.setPendingUpdates(0L);
        }

        // 3. Recent Configuration Logs (JOIN Query - Employee Table + Salary Table)
        try {
            List<Object[]> logs = entityManager.createNativeQuery(
                    "SELECT e.full_name, e.emp_id, 'SAL-2026-0' || s.config_id, s.total_package, s.status " +
                            "FROM salary_configurations s JOIN employees e ON s.emp_id = e.emp_id ORDER BY s.config_id DESC").getResultList();
            dto.setRecentLogs(logs);
        } catch (Exception e) {
            dto.setRecentLogs(new ArrayList<>());
        }

        return dto;
    }

    // নতুন স্যালারি ইনসার্ট করার মেথড
    @Transactional
    public void saveSalaryConfiguration(String empId, double basicSalary, double allowance, String effectiveDate) {
        String sql = "INSERT INTO salary_configurations (emp_id, basic_salary, allowance, effective_date) VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
        entityManager.createNativeQuery(sql)
                .setParameter(1, empId)
                .setParameter(2, basicSalary)
                .setParameter(3, allowance)
                .setParameter(4, effectiveDate)
                .executeUpdate();
    }
}