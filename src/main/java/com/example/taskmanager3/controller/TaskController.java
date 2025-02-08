package com.example.taskmanager3.controller;

import com.example.taskmanager3.model.Comment;
import com.example.taskmanager3.model.Task;
import com.example.taskmanager3.model.User;
import com.example.taskmanager3.repository.CommentRepository;
import com.example.taskmanager3.repository.TaskRepository;
import com.example.taskmanager3.repository.UserRepository;
import com.example.taskmanager3.service.JwtTokenProvider;
import com.example.taskmanager3.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private TaskService taskService;

    // Убираем префикс "Bearer " из заголовка
    private String extractToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    // Проверка валидности токена
    private boolean isTokenValid(String token) {
        return token != null && tokenProvider.validateToken(token);
    }

    // ========================================
    // Эндпоинты для обычных пользователей
    // ========================================

    /**
     * Получение задач текущего пользователя (с опциональной фильтрацией по статусу)
     */
    @GetMapping
    public ResponseEntity<List<Task>> getUserTasks(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "status", required = false) String status) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String username = tokenProvider.getUsernameFromJWT(extractedToken);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = userOpt.get();
        List<Task> tasks = (status != null && !status.isEmpty())
                ? taskRepository.findByUserAndStatus(user, status)
                : taskRepository.findByUser(user);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Удаление задачи текущего пользователя (пользователь может удалять только свои задачи)
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long taskId) {

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
        // Проверяем, что задача принадлежит текущему пользователю
        if (!taskOpt.isPresent() || !taskOpt.get().getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Создание новой задачи (для обычных пользователей)
     */
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestHeader("Authorization") String token,
            @RequestBody Task task) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = tokenProvider.getUsernameFromJWT(extractedToken);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Task savedTask = taskService.createTask(task, userOpt.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /**
     * Обновление задачи текущего пользователя (может обновлять только свои задачи)
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateUserTask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long taskId,
            @RequestBody Task updatedTask) {

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
        if (!taskOpt.isPresent() || !taskOpt.get().getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Task task = taskOpt.get();
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setDeadline(updatedTask.getDeadline());
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    // ========================================
    // Админские эндпоинты (доступ только для администраторов)
    // ========================================

    /**
     * Получение задач для конкретного пользователя (администратор)
     */
    @GetMapping("/admin/{username}")
    public ResponseEntity<List<Task>> getUserTasksAdmin(
            @RequestHeader("Authorization") String token,
            @PathVariable String username) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken) || !tokenProvider.isAdmin(extractedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<Task> tasks = taskRepository.findByUser(userOpt.get());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Обновление задачи (администратор может обновлять любую задачу)
     */
    @PutMapping("/admin/{taskId}")
    public ResponseEntity<Task> updateTask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long taskId,
            @RequestBody Task updatedTask) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken) || !tokenProvider.isAdmin(extractedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Task task = taskOpt.get();
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setDeadline(updatedTask.getDeadline());
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    /**
     * Удаление задачи (администратор)
     */
    @DeleteMapping("/admin/{taskId}")
    public ResponseEntity<Void> deleteTaskAdmin(
            @RequestHeader("Authorization") String token,
            @PathVariable Long taskId) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken) || !tokenProvider.isAdmin(extractedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Администратор может удалять любую задачу без проверки владельца
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Создание новой задачи (администратор)
     * Принимает параметр запроса "username", который указывает, для какого пользователя создаётся задача
     */
    @PostMapping("/admin")
    public ResponseEntity<Task> createTaskAdmin(
            @RequestHeader("Authorization") String token,
            @RequestParam("username") String targetUsername,
            @RequestBody Task task) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken) || !tokenProvider.isAdmin(extractedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<User> userOpt = userRepository.findByUsername(targetUsername);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Task savedTask = taskService.createTask(task, userOpt.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /**
     * Получение всех задач (администратор)
     * Этот эндпоинт позволяет администратору просматривать задачи всех пользователей.
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<Task>> getAllTasksAdmin(
            @RequestHeader("Authorization") String token) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken) || !tokenProvider.isAdmin(extractedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        List<Task> tasks = taskRepository.findAll();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Добавление комментария к задаче (администратор)
     */
    @PostMapping("/admin/{taskId}/comments")
    public ResponseEntity<Comment> addComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long taskId,
            @RequestBody Comment commentRequest) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken) || !tokenProvider.isAdmin(extractedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Task task = taskOpt.get();
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setContent(commentRequest.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }

    /**
     * Получение комментариев к задаче
     */
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<Comment> comments = commentRepository.findByTask(taskOpt.get());
        return ResponseEntity.ok(comments);
    }

    /**
     * Экспорт задач (CSV/PDF) для текущего пользователя
     * Фильтрация по статусу необязательна
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTasks(
            @RequestHeader("Authorization") String token,
            @RequestParam String format,
            @RequestParam(value = "status", required = false) String status) {

        String extractedToken = extractToken(token);
        if (!isTokenValid(extractedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = tokenProvider.getUsernameFromJWT(extractedToken);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOpt.get();
        byte[] fileContent = taskService.exportTasks(user, format, status);
        String fileName = "tasks." + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());
        headers.setContentType(format.equalsIgnoreCase("csv") ? MediaType.valueOf("text/csv") : MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
}
