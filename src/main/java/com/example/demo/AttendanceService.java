package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceService {

    @PersistenceContext
    private EntityManager entityManager;

    public AttendanceDTO getAttendanceDashboardData() {
        AttendanceDTO dto = new AttendanceDTO();

        // 1. Total Employees Count
        try {
            Object total = entityManager.createNativeQuery("SELECT COUNT(*) FROM employees").getSingleResult();
            dto.setTotalEmployees(total instanceof BigDecimal ? ((BigDecimal) total).longValue() : ((Number) total).longValue());
        } catch (Exception e) {
            dto.setTotalEmployees(0L);
        }

        // 2. Logged Today Count (PRESENT + ABSENT + LEAVE)
        try {
            Object logged = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM attendance_logs WHERE TRUNC(attendance_date) = TRUNC(SYSDATE) AND status IN ('PRESENT', 'ABSENT', 'LEAVE')").getSingleResult();
            dto.setLoggedToday(logged instanceof BigDecimal ? ((BigDecimal) logged).longValue() : ((Number) logged).longValue());
        } catch (Exception e) {
            dto.setLoggedToday(0L);
        }

        // 3. On Leave Count
        try {
            Object leave = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM attendance_logs WHERE TRUNC(attendance_date) = TRUNC(SYSDATE) AND status = 'LEAVE'").getSingleResult();
            dto.setOnLeave(leave instanceof BigDecimal ? ((BigDecimal) leave).longValue() : ((Number) leave).longValue());
        } catch (Exception e) {
            dto.setOnLeave(0L);
        }

        // 4. Calculate Progress Percentage
        if (dto.getTotalEmployees() > 0) {
            int progress = (int) ((dto.getLoggedToday() * 100) / dto.getTotalEmployees());
            dto.setProgressPercentage(progress);
        } else {
            dto.setProgressPercentage(0);
        }

        // 5. Fetch Attendance Rows (LEFT JOIN: সব এমপ্লয়ি আসবে, হাজিরা দেওয়া থাকলে স্ট্যাটাস আসবে, না থাকলে PENDING)
        try {
            String sql = "SELECT e.emp_id, e.full_name, e.department, COALESCE(a.status, 'PENDING') " +
                    "FROM employees e LEFT JOIN attendance_logs a " +
                    "ON e.emp_id = a.emp_id AND TRUNC(a.attendance_date) = TRUNC(SYSDATE) ORDER BY e.emp_id ASC";
            List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
            dto.setAttendanceRows(rows);
        } catch (Exception e) {
            dto.setAttendanceRows(new ArrayList<>());
        }

        return dto;
    }

    // হাজিরার স্ট্যাটাস সেভ অথবা আপডেট করার মেثড (Merge/Upsert Operation)
    @Transactional
    public void saveOrUpdateAttendance(String empId, String status) {
        // ওড়াকল ১০জি-তে আমরা প্রথমে চেক করব আজকের ডেটে এই এমপ্লয়ির রো আছে কিনা
        Object count = entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM attendance_logs WHERE emp_id = ? AND TRUNC(attendance_date) = TRUNC(SYSDATE)")
                .setParameter(1, empId)
                .getSingleResult();

        long exists = count instanceof BigDecimal ? ((BigDecimal) count).longValue() : ((Number) count).longValue();

        if (exists > 0) {
            // যদি রো থাকে, তবে স্ট্যাটাস UPDATE হবে
            entityManager.createNativeQuery(
                            "UPDATE attendance_logs SET status = ? WHERE emp_id = ? AND TRUNC(attendance_date) = TRUNC(SYSDATE)")
                    .setParameter(1, status)
                    .setParameter(2, empId)
                    .executeUpdate();
        } else {
            // যদি রো না থাকে, তবে নতুন রো INSERT হবে
            entityManager.createNativeQuery(
                            "INSERT INTO attendance_logs (attendance_id, emp_id, attendance_date, status) VALUES (attendance_seq.NEXTVAL, ?, TRUNC(SYSDATE), ?)")
                    .setParameter(1, empId)
                    .setParameter(2, status)
                    .executeUpdate();
        }
    }
}