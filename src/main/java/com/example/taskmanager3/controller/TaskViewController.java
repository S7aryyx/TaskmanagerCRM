package com.example.taskmanager3.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskViewController {
    @GetMapping("/task")
    public String showTask() {
        return "task";
    }
}
