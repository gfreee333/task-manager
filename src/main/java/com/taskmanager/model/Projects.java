package com.taskmanager.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Projects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Поле name не должно быть пустым")
    @Size(min = 4, max = 100, message = "Поле name должно содержать от 4 до 100 символов")
    private String name;
    private String description;
    @NotNull(message = "Поле status не должно быть пустым")
    @Pattern(regexp = "ACTIVE|COMPLETED|ARCHIVED",
            message = "Статус проекта должен быть ACTIVE, COMPLETED или ARCHIVED")
    private String status;
    @CreationTimestamp
    @NotNull(message = "Поле updated_at не должно быть пустым")
    private Timestamp createdAt;
    @UpdateTimestamp
    @NotNull(message = "Поле updated_at не должно быть пустым")
    private Timestamp updatedAt;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private ArrayList<Tasks> tasks = new ArrayList<>(); // Соединение с tasks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Users owner;
    @ManyToMany
    @JoinTable(name = "user_projects", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<Users> users = new ArrayList<>();
}
