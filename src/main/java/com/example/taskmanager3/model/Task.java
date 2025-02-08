package com.example.taskmanager3.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    // Статусы: "NEW", "IN_PROGRESS", "COMPLETED"
    @Column(nullable = false)
    private String status = "NEW";  // Значение по умолчанию

    private LocalDateTime deadline;

    // Один к многим: задача может иметь несколько комментариев
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // Связь с пользователем (владелец задачи)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Геттеры, сеттеры, конструкторы
    public Task() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}