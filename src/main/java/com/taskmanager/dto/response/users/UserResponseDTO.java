package com.taskmanager.dto.response.users;
import java.sql.Timestamp;

// Ответ клиенту
public record UserResponseDTO(
        String email,
        String firstName,
        String lastName,
        String role,
        Timestamp createdAt,
        Timestamp updatedAt
) {}
