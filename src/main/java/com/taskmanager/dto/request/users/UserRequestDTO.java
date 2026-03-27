package com.taskmanager.dto.request.users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
// Данные от клиента
public record UserRequestDTO(
        @Email(message = "Не правильный формат ввода Email")
        @NotNull(message = "Поле email не должно быть пустым")
        String email,
        @NotNull(message = "Поле password не должно быть пустым")
        @Size(min = 10, max = 255, message = "Password должен содержать от 10 до 255 символов")
        String password,
        @NotNull(message = "Поле first_name не должно быть пустым")
        @Size(min = 3, max = 50, message = "Поле first_name должно содержать от 3 до 50 символов")
        String firstName,
        @NotNull(message = "Поле last_name не должно быть пустым")
        @Size(min = 5, max = 50, message = "Поле last_name должно содержать от 5 до 50 символов")
        String lastName,
        @Pattern(regexp = "ADMIN|USER|MANAGER",
                message = "Роль должна быть: ADMIN, USER или MANAGER")
        @Size(min = 4, max = 20, message = "Поле role должно содержать от 4 до 20 символов")
        @NotNull
        String role
){}


