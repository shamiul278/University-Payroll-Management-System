package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @GetMapping("/payroll")
    public String showPayrollEngine(Model model) {
        PayrollDTO payrollData = payrollService.getPayrollDashboardData();
        model.addAttribute("payrollData", payrollData);
        return "payroll"; // templates/payroll.html ওপেন করবে
    }

    @PostMapping("/execute-payroll")
    public String executePayroll(
            @RequestParam String monthYear,
            @RequestParam double totalGross,
            @RequestParam double totalDeduction,
            @RequestParam double netPayout,
            RedirectAttributes redirectAttributes) {

        try {
            payrollService.executePayrollRun(monthYear, totalGross, totalDeduction, netPayout);
            redirectAttributes.addFlashAttribute("successMessage", "Payroll successfully disbursed for " + monthYear);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error executing payroll. Please check logs.");
        }

        return "redirect:/payroll";
    }
}