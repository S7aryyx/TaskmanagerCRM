package com.example.taskmanager3.service;

import com.example.taskmanager3.exception.ResourceNotFoundException;
import com.example.taskmanager3.model.User;
import com.example.taskmanager3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());  // Пароль должен быть захеширован

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        userRepository.delete(user);
    }
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(email).get();
        }

        return null; // Если пользователь не авторизован
    }
    }
