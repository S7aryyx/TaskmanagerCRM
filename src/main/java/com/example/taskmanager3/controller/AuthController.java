package com.example.taskmanager3.controller;

import com.example.taskmanager3.dto.LoginRequest;
import com.example.taskmanager3.dto.SignupRequest;
import com.example.taskmanager3.model.User;
import com.example.taskmanager3.repository.UserRepository;
import com.example.taskmanager3.service.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Ошибка: Пользователь с таким именем уже существует!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // Назначаем роль пользователя по умолчанию
        user.setRoles(Collections.singleton("ROLE_USER"));

        userRepository.save(user);

        return ResponseEntity.ok("Пользователь успешно зарегистрирован!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Ошибка: Неверный логин или пароль!"));
        }

        String role = "ROLE_USER"; // Роль по умолчанию
        if (!user.getRoles().isEmpty()) {
            role = user.getRoles().iterator().next(); // Берем роль пользователя
        }

        // Генерация токена с ID пользователя, username и ролью
        String jwt = tokenProvider.generateToken(String.valueOf(user.getId()), user.getUsername(), role);

        System.out.println(jwt);
        System.out.println(role);
        System.out.println(user);
        System.out.println(passwordEncoder.encode(user.getPassword()));

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("role", role);
        if ("ROLE_ADMIN".equals(role)) {
            response.put("redirect", "/admin.html");  // Админ — редирект на /admin.html
        } else {
            response.put("redirect", "/tasks.html");  // Пользователи — редирект на /tasks.html
        }

        return ResponseEntity.ok(response);
    }

    // Новый эндпоинт для валидации токена
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Токен не предоставлен или имеет неверный формат");
        }
        String token = bearerToken.substring(7); // удаляем "Bearer "

        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Токен невалиден");
        }

        String role = tokenProvider.getRoleFromJWT(token);
        Map<String, Object> response = new HashMap<>();
        response.put("role", role);
        return ResponseEntity.ok(response);
    }
}
