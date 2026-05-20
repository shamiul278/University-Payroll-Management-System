package com.example.demo;

import java.sql.Date;

public class EmployeeDTO {

    private String empId;
    private String fullName;
    private String designation;
    private String department;
    private String email;
    private String phone;
    private Date joinDate;
    private String initials; // নামের প্রথম অক্ষর দেখানোর জন্য (যেমন: Adrian Sterling -> AS)

    // No-Argument Constructor
    public EmployeeDTO() {
    }

    // All-Argument Constructor
    public EmployeeDTO(String empId, String fullName, String designation, String department, String email, String phone, Date joinDate) {
        this.empId = empId;
        this.fullName = fullName;
        this.designation = designation;
        this.department = department;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
        this.initials = generateInitials(fullName);
    }

    // --- Getters and Setters ---

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.initials = generateInitials(fullName); // নাম সেট করলেই অটো ইনিশিয়াল সেট হয়ে যাবে
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getInitials() {
        return initials;
    }

    // --- Helper Method ---
    // এই মেথডটি নামের প্রথম অক্ষরগুলো বের করবে (যেমন "Elena Kovic" থেকে "EK" বানাবে)
    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "NA";
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder init = new StringBuilder();
        if (words.length > 0) {
            init.append(words[0].charAt(0));
            if (words.length > 1) {
                init.append(words[words.length - 1].charAt(0));
            }
        }
        return init.toString().toUpperCase();
    }
}