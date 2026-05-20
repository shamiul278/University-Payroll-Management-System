package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String showLoginPage() {
        return "login"; // এটি আপনার templates ফোল্ডারের login.html ফাইলটিকে কল করবে
    }
}