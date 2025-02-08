package com.example.taskmanager3.controller;

import com.example.taskmanager3.model.Task;
import com.example.taskmanager3.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private TaskRepository taskRepository;

    // Подсчет количества задач по статусам
    @GetMapping("/status-count")
    public ResponseEntity<Map<String, Long>> getStatusCount() {
        List<Task> tasks = taskRepository.findAll();
        Map<String, Long> countByStatus = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        return ResponseEntity.ok(countByStatus);
    }

    // Просроченные задачи
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskRepository.findAll().stream()
                .filter(task -> task.getDeadline() != null && task.getDeadline().isBefore(now))
                .collect(Collectors.toList());
        return ResponseEntity.ok(overdueTasks);
    }

    // Экспорт задач в CSV (простой пример)
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportTasksToCSV() {
        List<Task> tasks = taskRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Description,Status,Deadline\n");
        for (Task task : tasks) {
            sb.append(task.getId()).append(",")
                    .append(task.getTitle()).append(",")
                    .append(task.getDescription()).append(",")
                    .append(task.getStatus()).append(",")
                    .append(task.getDeadline()).append("\n");
        }
        return ResponseEntity.ok(sb.toString());
    }
}
