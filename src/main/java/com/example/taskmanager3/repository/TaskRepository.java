package com.example.taskmanager3.repository;

import com.example.taskmanager3.model.Task;
import com.example.taskmanager3.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(String status);
    List<Task> findByUser(User user);
    List<Task> findByUserAndStatus(User user, String status);  // Новый метод для фильтрации по статусу

    List<Task> findByUserId(Long id);
}