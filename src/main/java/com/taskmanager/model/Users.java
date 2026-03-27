package com.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Email(message = "Не правильный формат ввода Email")
    @NotNull(message = "Поле email не должно быть пустым")
    private String email;
    @NotNull(message = "Поле password не должно быть пустым")
    @Size(min = 10, max = 255, message = "Password должен содержать от 10 до 255 символов")
    private String password;
    @NotNull(message = "Поле first_name не должно быть пустым")
    @Size(min = 3, max = 50, message = "Поле first_name должно содержать от 3 до 50 символов")
    private String firstName;
    @NotNull(message = "Поле last_name не должно быть пустым")
    @Size(min = 5, max = 50, message = "Поле last_name должно содержать от 5 до 50 символов")
    private String lastName;
    @Size(min = 4, max = 20, message = "Поле role должно содержать от 4 до 20 символов")
    @Pattern(regexp = "ADMIN|USER|MANAGER",
            message = "Роль должна быть: ADMIN, USER или MANAGER")
    private String role;
    @CreationTimestamp
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Tasks> assigneeTasks = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Tasks> createdTasks = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Projects> projects = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    private List<Projects> participatedProjects = new ArrayList<>();

}
