package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    // স্যালারি পেজ দেখানোর রুট
    @GetMapping("/salary-config")
    public String showSalaryConfig(Model model) {
        SalaryDTO salaryData = salaryService.getSalaryDashboardData();
        model.addAttribute("salaryData", salaryData);
        return "salary"; // এটি templates/salary.html ফাইলকে ওপেন করবে
    }

    // ফর্ম সাবমিট ক্যাচ করার রুট
    @PostMapping("/finalize-salary")
    public String finalizeConfiguration(
            @RequestParam String empId,
            @RequestParam double basicSalary,
            @RequestParam double allowance,
            @RequestParam String effectiveDate,
            RedirectAttributes redirectAttributes) {

        try {
            salaryService.saveSalaryConfiguration(empId, basicSalary, allowance, effectiveDate);
            redirectAttributes.addFlashAttribute("successMessage", "Salary configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Please check if the Employee ID exists.");
        }

        return "redirect:/salary-config";
    }
}