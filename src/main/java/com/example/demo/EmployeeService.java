package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {

    @PersistenceContext
    private EntityManager entityManager;

    // মোট এমপ্লয়ি সংখ্যা
    public long getTotalStaff() {
        Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM employees").getSingleResult();
        return count != null ? count.longValue() : 0;
    }

    // মোট ডিপার্টমেন্ট সংখ্যা
    public long getTotalDepartments() {
        Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(DISTINCT department) FROM employees").getSingleResult();
        return count != null ? count.longValue() : 0;
    }

    // সব এমপ্লয়িদের লিস্ট আনা
    public List<Object[]> getAllEmployees() {
        return entityManager.createNativeQuery(
                "SELECT emp_id, full_name, email, designation, department, join_date FROM employees ORDER BY emp_id DESC"
        ).getResultList();
    }

    // নতুন এমপ্লয়ি ইনসার্ট করা
    @Transactional
    public void saveEmployee(String name, String designation, String department, String email, String phone, String joinDate) {
        String sql = "INSERT INTO employees (full_name, designation, department, email, phone, join_date) VALUES (?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
        entityManager.createNativeQuery(sql)
                .setParameter(1, name)
                .setParameter(2, designation)
                .setParameter(3, department)
                .setParameter(4, email)
                .setParameter(5, phone)
                .setParameter(6, joinDate)
                .executeUpdate();
    }
    // EmployeeService.java ফাইলের ভেতরে নিচের মেথডটি যোগ করুন
    // EmployeeService.java এর ভেতরের deleteEmployee মেথডটি এটি দিয়ে রিপ্লেস করুন
    @Transactional
    public void deleteEmployee(String empId) {
        // ১. প্রথমে পেরোল রেকর্ডস টেবিল থেকে ডাটা ডিলিট করো (Child Table 1)
        entityManager.createNativeQuery("DELETE FROM payroll_records WHERE emp_id = ?")
                .setParameter(1, empId)
                .executeUpdate();

        // ২. পেরোল সাইকেল ডিলিট করার দরকার নেই কারণ সেটা মাসের হিসাব, কোনো নির্দিষ্ট এমপ্লয়ির না

        // ৩. এবার স্যালারি কনফিগারেশন টেবিল থেকে ডাটা ডিলিট করো (Child Table 2)
        entityManager.createNativeQuery("DELETE FROM salary_configurations WHERE emp_id = ?")
                .setParameter(1, empId)
                .executeUpdate();

        // ৪. এবার অ্যাটেনডেন্স লগ টেবিল থেকে ডাটা ডিলিট করো (Child Table 3)
        entityManager.createNativeQuery("DELETE FROM attendance_logs WHERE emp_id = ?")
                .setParameter(1, empId)
                .executeUpdate();

        // ৫. সব চাইল্ড ডাটা ডিলিট শেষ! এবার মেইন এমপ্লয়িকে ডিলিট করো (Parent Table)
        entityManager.createNativeQuery("DELETE FROM employees WHERE emp_id = ?")
                .setParameter(1, empId)
                .executeUpdate();
    }
}