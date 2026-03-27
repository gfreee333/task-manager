package com.taskmanager.contoller.projects;

import com.taskmanager.dto.request.projects.ProjectRequestDTO;
import com.taskmanager.dto.response.projects.ProjectResponseDTO;
import com.taskmanager.dto.response.projects.ProjectResponseParticipantDTO;
import com.taskmanager.dto.response.projects.ProjectResponseSummaryDTO;
import com.taskmanager.service.projects.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/manager/api/projects")
@Validated
public class ProjectsController {
    private final ProjectService projectService;
    public ProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }
    //Todo 1: Создание проекта с указанным владельцем // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    // POST /manager/api/projects
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }
    //Todo 2: Получение проекта по id // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    // GET /manager/api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@Positive(message = "id проекта не может быть отрицательным") @PathVariable Long id){
        return ResponseEntity.ok(projectService.getProjectById(id));
    }
    //Todo 3: 3.1 Получение списка всех проектов  // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //        3.2 Получение проекта по статусу 3.3
    // GET /manager/api/projects
    @GetMapping
    public ResponseEntity<List<ProjectResponseSummaryDTO>> getProjects(@Pattern(regexp = "ACTIVE|COMPLETED|ARCHIVED",
            message = "Статус проекта должен быть ACTIVE, COMPLETED или ARCHIVED") @RequestParam(required = false) String status){
        // Получение проекта по статусу
        if(status != null) {
            return ResponseEntity.ok(projectService.getProjectByStatus(status));
        }
        return ResponseEntity.ok(projectService.getAllProjects());
    }
    // Todo 4: Получение проектов где пользователь владелец // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET  /manager/api/projects/owner/{id}
    @GetMapping("/owner/{id}")
    public ResponseEntity<List<ProjectResponseSummaryDTO>> getProjectByOwnerId(@Positive(message = "id владельце не может быть отрицательным")@PathVariable Long id){
        return ResponseEntity.ok(projectService.getProjectByOwnerId(id));
    }
    // Todo 5: Получение проектов где пользователь участник // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET /manager/api/projects/participant/{id}
    @GetMapping("/participant/{id}")
    public ResponseEntity<List<ProjectResponseSummaryDTO>> getProjectByParticipant(@Positive(message = "id пользователя не может быть отрицательным")@PathVariable Long id){
        return ResponseEntity.ok(projectService.getProjectsByParticipant(id));
    }
    // Todo 6: Обновление проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  PUT /manager/api/projects/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProjectById(@Positive(message = "id проекта не может быть отрицательным") @PathVariable Long id, @Valid @RequestBody ProjectRequestDTO request){
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }
    // Todo 7: Удаление проекта по id // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  DELETE /manager/api/projects/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectById(@Positive(message = "id проекта не может быть отрицательным")@PathVariable Long id){
        projectService.deleteProjectById(id);
        return ResponseEntity.noContent().build();
    }
    // Todo 8: Добавление пользователя в проект // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  PUT  /manager/api/projects/{id}/users/{userId}
    @PutMapping("/{id}/users/{userId}")
    public ResponseEntity<ProjectResponseDTO> addUserToProject(@Positive(message = "id проекта не может быть отрицательным")@PathVariable Long id, @Positive(message = "id пользователя не может быть отрицательным")@PathVariable Long userId){
        return ResponseEntity.ok(projectService.addUserToProject(id, userId));
    }
    // Todo 9: Удаление пользователя из проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  DELETE  /manager/api/projects/{id}/users/{userId}
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<Void> deleteUserFromProject(@Positive(message = "id проекта не может быть отрицательным")@PathVariable Long id, @Positive(message = "id пользователя не может быть отрицательным") @PathVariable Long userId){
        projectService.deleteUserFromProject(id, userId);
        return ResponseEntity.noContent().build();
    }
    // Todo 10: Проверка является ли пользователь участником проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET /manager/api/projects/{id}/users/{userId}
    @GetMapping("/{id}/users/{userId}")
    public ResponseEntity<Boolean> isUserInProject(@Positive(message = "id проекта не может быть отрицательным")@PathVariable Long id, @Positive(message = "id пользователя не может быть отрицательным") @PathVariable Long userId){
        return ResponseEntity.ok(projectService.isUserInProject(id, userId));
    }
    //Todo 11: Получение списка участников проекта // Готово
    // GET /manager/api/projects/{id}/participants
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ProjectResponseParticipantDTO>> getProjectParticipants(@Positive(message = "id проекта не может быть отрицательным")@PathVariable Long id){
        return ResponseEntity.ok(projectService.getProjectParticipants(id));
    }
    //Todo 12: Получение списка проектов пользователя // Готово
    // GET /manager/api/projects/users/{usersId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ProjectResponseSummaryDTO>> getUserProjects(@Positive(message = "id пользователя не может быть отрицательным") @PathVariable Long userId){
        return ResponseEntity.ok(projectService.getUserProjects(userId));
    }

}
