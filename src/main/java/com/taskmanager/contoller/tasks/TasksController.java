package com.taskmanager.contoller.tasks;

import com.taskmanager.dto.request.tasks.TasksRequestDTO;
import com.taskmanager.dto.response.tasks.TasksResponseDTO;
import com.taskmanager.dto.response.tasks.TasksSummaryDTO;
import com.taskmanager.service.tasks.TasksService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager/api/tasks")
@Validated
public class TasksController {
    private final TasksService tasksService;
    public TasksController(TasksService tasksService) {
        this.tasksService = tasksService;
    }
    // Todo 1: Создание задачи с указанием // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  POST /manager/api/tasks
    @PostMapping
    public ResponseEntity<TasksResponseDTO> createTask(@Valid @RequestBody TasksRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(tasksService.createTask(request));
    }

    // Todo 2: Получение задачи по id // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET /manager/api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TasksResponseDTO> getTasksById(@Positive @PathVariable Long id){
        return ResponseEntity.ok(tasksService.getTaskById(id));
    }

    // Todo 3: // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  3.1 Получение всех задач по статусу
    //  3.2 Получение всех задач по приоритету
    //  3.3 Получение всех задач
    @GetMapping
    public ResponseEntity<List<TasksSummaryDTO>> getTasks(@Pattern(regexp = "TODO|IN_PROGRESS|DONE", message = "Статус должен быть TODO, IN_PROGRESS, DONE") @RequestParam(required = false) String status, @Pattern(regexp = "LOW|MED|HIGH", message = "Приоритет должен быть: LOW, MED или HIGH")@RequestParam(required = false) String priority) {
        // Получение всех задач по статусу
        if(status != null){
            return ResponseEntity.ok(tasksService.getAllTaskByStatus(status));
        }
        // Получение всех задач по приоритету
        if(priority != null){
            return ResponseEntity.ok(tasksService.getAllTaskByPriority(priority));
        }
        // Получение всех задач
        return ResponseEntity.ok(tasksService.getAllTask());
    }

    // Todo 4: Получение задач по проекту // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET /manager/api/tasks/projects/{projectId}
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TasksSummaryDTO>> getTaskByProjects(@Positive(message = "Id проекта не должно быть отрицательным") @PathVariable Long projectId){
        return ResponseEntity.ok(tasksService.getAllTaskByProject(projectId));
    }

    // Todo 5: Получение задач назначенных пользователю // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET /manager/api/tasks/users/assignee/{assigneeId}
    @GetMapping("/users/assignee/{assigneeId}")
    public ResponseEntity<List<TasksSummaryDTO>> getAllTaskByAssignee(@Positive(message = "Id исполнителя не может быть отрицательным") @PathVariable Long assigneeId){
        return ResponseEntity.ok(tasksService.getAllTaskByAssignee(assigneeId));
    }

    // Todo 6: Получение задач созданных пользователем // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  GET /manager/api/tasks/users/creator/{creatorId}
    @GetMapping("/users/creator/{creatorId}")
    public ResponseEntity<List<TasksSummaryDTO>> getAllTaskByCreated(@Positive(message = "Id создателя не может быть отрицательным") @PathVariable Long creatorId){
        return ResponseEntity.ok(tasksService.getAllTaskByCreated(creatorId));
    }

    // Todo 7: Удаление задачи // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  DELETE /manager/api/tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@Positive(message = "id задачи не может быть отрицательным")@PathVariable Long id){
        tasksService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }

    // Todo 8: Обновление задачи // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  PUT /manager/api/tasks/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TasksResponseDTO> updateTaskById(@Positive(message = "id задачи не может быть отрицательным") @PathVariable Long id, @Valid @RequestBody TasksRequestDTO request){
        return ResponseEntity.ok(tasksService.updateTaskById(id, request));
    }

}
