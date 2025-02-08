package com.example.taskmanager3.service;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Генерирует JWT для пользователя с его ролью.
     *
     * @param userId   идентификатор пользователя
     * @param username имя пользователя
     * @param role     роль пользователя (например, "ROLE_ADMIN", "ROLE_USER")
     * @return JWT токен
     */
    public String generateToken(String userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username) // Используем переданный username
                .claim("userId", userId)  // Используем переданный userId
                .claim("role", role) // Используем переданную роль
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromJWT(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Извлекает роль пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return роль пользователя
     */
    public String getRoleFromJWT(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);  // Извлекаем роль из токена
    }

    /**
     * Проверяет, является ли токен валидным.
     *
     * @param authToken JWT токен
     * @return true, если токен валидный; false, если токен невалидный
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: " + ex.getMessage(), ex);
        } catch (MalformedJwtException ex) {
            logger.error("Malformed JWT token: " + ex.getMessage(), ex);
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: " + ex.getMessage(), ex);
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: " + ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Проверяет, является ли пользователь с данным токеном администратором.
     *
     * @param extractedToken JWT токен
     * @return true, если пользователь является администратором; false в противном случае
     */
    public boolean isAdmin(String extractedToken) {
        try {
            // Извлекаем роль из токена
            String role = getRoleFromJWT(extractedToken);

            // Проверяем, если роль пользователя - "ROLE_ADMIN"
            return "ROLE_ADMIN".equals(role);
        } catch (Exception ex) {
            logger.error("Error extracting role from token: " + ex.getMessage(), ex);
            return false; // Если возникла ошибка, считаем, что пользователь не является администратором
        }
    }

}
