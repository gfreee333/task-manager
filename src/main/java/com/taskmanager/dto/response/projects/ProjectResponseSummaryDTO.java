package com.taskmanager.dto.response.projects;

import java.sql.Timestamp;

// Для отправки информации о проекте, без пользователей
public record ProjectResponseSummaryDTO(
        Long id,
        String name,
        String description,
        String status,
        Timestamp createdAt,
        Timestamp updatedAt
) { }
