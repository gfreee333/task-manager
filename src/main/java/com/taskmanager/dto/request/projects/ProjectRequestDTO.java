package com.taskmanager.dto.request.projects;

import jakarta.validation.constraints.*;

public record ProjectRequestDTO(
        @NotNull(message = "Название проекта не должно быть пустым")
        @Size(min = 4, max = 100, message = "Название проекта должно содержать от 4 до 100 символов")
        String name,
        @Size(max = 200000, message = "Максимальный размер описания проекта 200000 символов" )
        String description,
        @NotBlank(message = "Статус проекта не должен быть пустым")
        @Pattern(regexp = "ACTIVE|COMPLETED|ARCHIVED",
                message = "Статус должен быть ACTIVE, COMPLETED или ARCHIVED")
        String status,
        @NotNull(message = "id пользователя обязательно")
        @Positive(message = "id пользователя не может быть отрицательным")
        Long ownerId
) {}
