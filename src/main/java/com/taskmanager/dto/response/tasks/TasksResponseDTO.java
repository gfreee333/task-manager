package com.taskmanager.dto.response.tasks;

import java.sql.Timestamp;
import java.time.LocalDate;

public record TasksResponseDTO (
        Long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDate dueDate,
        // Исполнитель
        Long assigneeId,
        String assigneeFirstName,
        String assigneeLastName,
        // Создатель
        Long createdById,
        String createdByFirstName,
        String createdByLastName,
        // Проект
        Long projectId,
        String projectName,
        Timestamp createdAt,
        Timestamp updatedAt
){
}
