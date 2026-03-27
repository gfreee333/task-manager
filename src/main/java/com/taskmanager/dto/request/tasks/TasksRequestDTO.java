package com.taskmanager.dto.request.tasks;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TasksRequestDTO(
        @NotBlank(message = "Название задачи не должно быть пустым")
        @Size(min = 4, max = 200, message = "Название задачи должно содержать от 4 до 200 символов")
        String title,
        String description,
        @NotBlank(message = "Приоритет не должен быть пустым")
        @Pattern(regexp = "LOW|MED|HIGH",
                message = "Приоритет должен быть: LOW, MED или HIGH")
        String priority,
        @NotBlank(message = "Статус не должен быть пустым")
        @Pattern(regexp = "TODO|IN_PROGRESS|DONE", message = "Статус должен быть TODO, IN_PROGRESS, DONE")
        String status,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate dueDate,
        @NotNull(message = "id исполнителя обязательно")
        @Positive(message = "Id исполнителя не может быть отрицательным")
        Long assigneeId, // id исполнитель
        @NotNull(message = "id проекта обязательно")
        @Positive(message = "id проекта не может быть отрицательным")
        Long projectId, // id проекта
        @NotNull(message = "id создателя задачи обязательно")
        @Positive(message = "id создателя не может быть отрицательным")
        Long createdById // Создатель задачи по id
){}
