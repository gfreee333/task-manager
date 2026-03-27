package com.taskmanager.dto.response.projects;

import java.sql.Timestamp;
// DTO для отправления детальной информации о проекте, включая пользователей
public record ProjectResponseDTO(
        Long id,
        String name,
        String description,
        String status,
        Long ownerId,
        String ownerFirstName,
        String ownerLastName,
        Timestamp createdAt,
        Timestamp updatedAt
) {}
