package com.example.taskmanager3.repository;

import com.example.taskmanager3.model.Comment;
import com.example.taskmanager3.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Получить все комментарии для конкретной задачи
    List<Comment> findByTask(Task task);

    // Получить комментарии по идентификатору задачи
    List<Comment> findByTaskId(Long taskId);
}
