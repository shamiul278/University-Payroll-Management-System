package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // হাজিরা পেজ লোড করার রুট
    @GetMapping("/attendance")
    public String showAttendanceGrid(Model model) {
        AttendanceDTO attendanceData = attendanceService.getAttendanceDashboardData();
        model.addAttribute("attendanceData", attendanceData);
        return "attendance"; // এটি templates/attendance.html কে কল করবে
    }

    // প্রতিটা কর্মচারীর ইন্ডিভিজুয়াল স্ট্যাটাস আপডেট করার রুট
    @PostMapping("/update-attendance")
    public String updateEmployeeAttendance(
            @RequestParam String empId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            attendanceService.saveOrUpdateAttendance(empId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating attendance record.");
        }

        return "redirect:/attendance";
    }
}