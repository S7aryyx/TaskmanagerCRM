package com.example.taskmanager3.service;

import com.example.taskmanager3.model.Task;
import com.example.taskmanager3.model.User;
import com.example.taskmanager3.repository.TaskRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Экспорт в CSV
    public byte[] exportTasksToCSV(List<Task> tasks) {
        StringBuilder sb = new StringBuilder("Заголовок,Описание,Дедлайн,Статус\n");
        for (Task task : tasks) {
            sb.append(task.getTitle()).append(",")
                    .append(task.getDescription()).append(",")
                    .append(task.getDeadline()).append(",")
                    .append(task.getStatus()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Удаление задачи
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    // Создание задачи
    public Task createTask(Task task, User user) {
        task.setUser(user); // Устанавливаем владельца задачи
        return taskRepository.save(task);
    }

    // Экспорт в PDF
    public byte[] exportTasksToPDF(List<Task> tasks) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            for (Task task : tasks) {
                document.add(new Paragraph("Заголовок: " + task.getTitle(), titleFont));
                document.add(new Paragraph("Описание: " + task.getDescription(), textFont));
                document.add(new Paragraph("Дедлайн: " + task.getDeadline(), textFont));
                document.add(new Paragraph("Статус: " + task.getStatus(), textFont));
                document.add(new Paragraph("\n"));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании PDF", e);
        }
    }

    // Метод для экспорта задач с фильтрацией по статусу
    public byte[] exportTasks(User user, String format, String status) {
        // Получаем задачи для пользователя с учетом статуса, если он передан
        List<Task> tasks = (status != null && !status.isEmpty())
                ? taskRepository.findByUserAndStatus(user, status)
                : taskRepository.findByUser(user); // Если статус не передан, берем все задачи

        switch (format.toLowerCase()) {
            case "csv":
                return exportTasksToCSV(tasks);
            case "pdf":
                return exportTasksToPDF(tasks);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}
