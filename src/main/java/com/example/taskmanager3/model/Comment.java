package com.example.taskmanager3.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // This will automatically be set when the comment is created

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Comment() {}

    public Comment(String content, Task task, User user) {
        this.content = content;
        this.task = task;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreatedAt(LocalDateTime now) {
        this.createdAt = now;
    }

    // Remove the unnecessary setter for createdAt
}
