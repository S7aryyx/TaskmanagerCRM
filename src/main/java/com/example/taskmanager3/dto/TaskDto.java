package com.example.taskmanager3.dto;

import java.time.LocalDateTime;

public class TaskDto {
    private String title;
    private String description;
    private String status;
    private LocalDateTime deadline;

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}