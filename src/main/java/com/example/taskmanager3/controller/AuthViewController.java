package com.example.taskmanager3.controller;

import com.example.taskmanager3.model.User;
import com.example.taskmanager3.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    private UserService userService;

    @GetMapping("/register")
    public String showSignupForm() {
        return "register"; // просто через таймлиф делаешь пишешь тут крч нгазвание html и все
    }
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    @GetMapping("/admin")
    public String showAdminForm() {
        User user = userService.getCurrentUser();
        if(user.getRoles().contains("ROLE_ADMIN")) {
            return "admin";
        }
        return "redirect:/admin.html";

    }



}
