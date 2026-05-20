package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/employees")
    public String showEmployeeManagement(Model model) {
        model.addAttribute("totalStaff", employeeService.getTotalStaff());
        model.addAttribute("totalDepts", employeeService.getTotalDepartments());
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees"; // employees.html কে ডাকবে
    }

    @PostMapping("/add-employee")
    public String addEmployee(
            @RequestParam String fullName,
            @RequestParam String designation,
            @RequestParam String department,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String joinDate,
            RedirectAttributes redirectAttributes) {

        try {
            employeeService.saveEmployee(fullName, designation, department, email, phone, joinDate);
            redirectAttributes.addFlashAttribute("successMessage", "Employee Added Successfully!");
        } catch (Exception e) {
            // যদি ইমেইল মিলে যায় (Unique Constraint)
            redirectAttributes.addFlashAttribute("errorMessage", "Unique constraint: This email is already registered.");
        }

        return "redirect:/employees";
    }
    // EmployeeController.java ফাইলের ভেতরে নিচের মেথডটি যোগ করুন
    @PostMapping("/delete-employee")
    public String deleteEmployee(@RequestParam String empId, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(empId);
            redirectAttributes.addFlashAttribute("successMessage", "Employee record deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting employee. They might have active records.");
        }
        return "redirect:/employees";
    }
}