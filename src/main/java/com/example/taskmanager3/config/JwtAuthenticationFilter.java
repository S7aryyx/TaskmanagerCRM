package com.example.taskmanager3.config;

import com.example.taskmanager3.service.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromJWT(token);
            String role = tokenProvider.getRoleFromJWT(token);  // Извлекаем роль из токена

            // Добавляем префикс "ROLE_" к роли, если его нет
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;  // Добавляем префикс "ROLE_" к роли
            }

            // Создаем объект аутентификации с ролью пользователя
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority(role))  // Роль с префиксом
                    );

            // Устанавливаем аутентификацию в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Продолжаем выполнение фильтра
        filterChain.doFilter(request, response);
    }

    // Метод для извлечения JWT токена из заголовка запроса
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Извлекаем токен после "Bearer "
        }
        return null;  // Если токена нет
    }
}