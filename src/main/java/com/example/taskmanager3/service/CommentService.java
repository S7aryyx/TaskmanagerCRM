package com.example.taskmanager3.service;

import com.example.taskmanager3.model.Comment;
import com.example.taskmanager3.model.Task;
import com.example.taskmanager3.model.User;
import com.example.taskmanager3.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // Метод добавления комментария
    public Comment addComment(Task task, User user, String content) {
        Comment comment = new Comment(content, task, user);
        return commentRepository.save(comment);
    }

    // Получение всех комментариев по задаче
    public List<Comment> getCommentsByTask(Task task) {
        return commentRepository.findByTask(task);
    }
}
