package com.taskmanager.dto.response.tasks;


import java.time.LocalDate;

//DTO для получения задачи и ее исполнителя
public record TasksSummaryDTO(
    Long id,
    String title,
    String description,
    String status,
    String priority,
    Long assigneeId,
    String assigneeFirstName,
    String projectName,
    LocalDate dueDate
) {}
