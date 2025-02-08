package com.example.taskmanager3.controller;

import com.example.taskmanager3.model.Comment;
import com.example.taskmanager3.model.Task;
import com.example.taskmanager3.model.User;
import com.example.taskmanager3.repository.TaskRepository;
import com.example.taskmanager3.repository.UserRepository;
import com.example.taskmanager3.service.CommentService;
import com.example.taskmanager3.service.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String extractToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : null;
    }

    private boolean isTokenValid(String token) {
        return token != null && tokenProvider.validateToken(token);
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<Comment> addComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long taskId,
            @RequestBody String content) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = tokenProvider.getUsernameFromJWT(extractedToken);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Comment savedComment = commentService.addComment(taskOpt.get(), userOpt.get(), content);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Comment> comments = commentService.getCommentsByTask(taskOpt.get());
        return ResponseEntity.ok(comments);
    }
}