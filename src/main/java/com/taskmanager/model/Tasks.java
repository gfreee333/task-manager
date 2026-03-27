package com.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Поле title не должно быть пустым")
    @Size(min = 20, max = 200, message = "Поле title должно содержать от 20 до 200 символов")
    private String title;
    @NotNull(message = "Описание задачи не должно быть пустым")
    private String description;
    @NotNull(message = "Поле status не должно быть пустым")
    @Size(min = 4, max = 20, message = "Поле status должно содержать от 5 до 20 символов")
    @Pattern(regexp = "TODO|IN_PROGRESS|DONE")
    private String status;
    @NotNull(message = "Поле priority не должно быть пустым")
    @Pattern(regexp = "LOW|MED|HIGH",
            message = "Приоритет должен быть: LOW, MED или HIGH")
    private String priority;
    @DateTimeFormat
    private Date dueDate;
    @CreationTimestamp
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id") // Соединение по created_id
    private Users createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id") // Соединение по assignee_id
    private Users assignee;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id") // Соединение по project_id
    private Projects project;
}
