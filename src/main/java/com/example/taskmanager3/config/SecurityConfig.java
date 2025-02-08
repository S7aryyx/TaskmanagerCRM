package com.example.taskmanager3.config;

import com.example.taskmanager3.service.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // Отключаем CSRF, так как используем JWT
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless, так как мы используем JWT
                .and()
                .authorizeRequests()
                // Разрешаем доступ к статическим ресурсам
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .antMatchers("/tasks.html").permitAll() // Разрешаем доступ к страницам задач
                .antMatchers("/", "/login", "/register", "/api/auth/**").permitAll() // Разрешаем доступ к публичным эндпоинтам
                // Доступ для обычных пользователей (роль ROLE_USER)
                .antMatchers("/task", "/tasks/**").authenticated()
                // Доступ для администраторов (роль ROLE_ADMIN)
                .antMatchers("/admin.html").permitAll() // Ожидает роль "ROLE_ADMIN", но без префикса "ROLE_"
                .anyRequest().authenticated()  // Все остальные запросы требуют аутентификации
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // Добавляем фильтр JWT

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider);
    }
}