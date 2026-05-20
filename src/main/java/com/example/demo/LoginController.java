package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password) {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // ওড়াকল ডাটাবেজ থেকে লাইভ ডিপেন্ডেন্ট ডেটা আনা হচ্ছে
        DashboardDTO dashboardData = dashboardService.getDashboardData();

        // Thymeleaf পেজে ডেটা পাস করা হচ্ছে
        model.addAttribute("data", dashboardData);
        return "dashboard";
    }
}