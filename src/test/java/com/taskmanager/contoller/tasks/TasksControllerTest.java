package com.taskmanager.contoller.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.request.tasks.TasksRequestDTO;
import com.taskmanager.dto.response.tasks.TasksResponseDTO;
import com.taskmanager.dto.response.tasks.TasksSummaryDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.service.tasks.TasksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TasksController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TasksControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    TasksService tasksService;

    // ========== ОБЩИЕ ДАННЫЕ (для всех тестов) ==========
    private final Long CORRECT_ID = 1L;
    private TasksRequestDTO testRequest;
    private TasksRequestDTO testUpdateRequest;
    private TasksResponseDTO testResponse;
    private TasksResponseDTO testUpdateResponse;
    private TasksSummaryDTO testSummary;

    @BeforeEach
    void setUp(){
        testRequest = new TasksRequestDTO(
                "Название задачи",
                "Описание задачи",
                "LOW",
                "TODO",
                LocalDate.of(2025,12,31),
                2L,
                1L,
                1L
        );
        testUpdateRequest = new TasksRequestDTO(
                "Обновленное название задачи",
                "Обновленное описание задачи",
                "LOW",
                "TODO",
                LocalDate.of(2025,12,31),
                2L,
                1L,
                1L

        );
        testUpdateResponse = new TasksResponseDTO(
                1L,
                "Обновленное название задачи",
                "Обновленное описание задачи",
                "COMPLETED",
                "LOW",
                LocalDate.of(2025,12,31),
                2L,
                "Иван",
                "Чухманов",
                1L,
                "Олег",
                "Филипов",
                1L,
                "Название проекта",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );
        testResponse = new TasksResponseDTO(
                1L,
                "Название задачи",
                "Описание задачи",
                "TODO",
                "LOW",
                LocalDate.of(2025,12,31),
                2L,
                "Иван",
                "Чухманов",
                1L,
                "Олег",
                "Филипов",
                1L,
                "Название проекта",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );
        testSummary = new TasksSummaryDTO(
                1L,
                "Название задачи",
                "Описание задачи",
                "TODO",
                "LOW",
                2L,
                "Иван",
                "Название проекта",
                LocalDate.of(2025,12,31)
        );
    }
    @Nested
    @DisplayName("Успешные сценарии http status: (2xx)")
    class SuccessTests{
        //==========================================================
        // Создание задачи с указанием
        //==========================================================
        @Test
        @DisplayName("POST /manager/api/tasks - успешное создание задачи")
        void createTask_Return201() throws Exception {
            String json = objectMapper.writeValueAsString(testRequest);
            when(tasksService.createTask(testRequest)).thenReturn(testResponse);
            mockMvc.perform(post("/manager/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.status").value("TODO"))
                    .andExpect(jsonPath("$.priority").value("LOW"));
            verify(tasksService, times(1)).createTask(testRequest);
        }
        //==========================================================
        // Получение задачи по id
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/tasks/{id} - успешное получение задачи по id")
        void getTasksById_Return200() throws Exception {
            when(tasksService.getTaskById(CORRECT_ID)).thenReturn(testResponse);
            mockMvc.perform(get("/manager/api/tasks/{id}", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CORRECT_ID))
                    .andExpect(jsonPath("$.status").value("TODO"))
                    .andExpect(jsonPath("$.priority").value("LOW"));
            verify(tasksService, times(1)).getTaskById(CORRECT_ID);
        }
        //==========================================================
        // Получение всех задач по статусу
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/tasks?status={status} - успешное получение всех задач по статусу")
        void getTasks_Status_Return200() throws Exception {
            when(tasksService.getAllTaskByStatus("TODO")).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/tasks").param("status", "TODO"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("TODO"));
            verify(tasksService, times(1)).getAllTaskByStatus("TODO");
            verify(tasksService, never()).getAllTaskByPriority(anyString());
            verify(tasksService, never()).getAllTask();
        }
        //==========================================================
        // Получение всех задач по приоритету
        //==========================================================
        @Test // GET /manager/api/tasks?priority={priority}
        @DisplayName("GET /manager/api/tasks?priority={priority} - успешное получение всех задач по приоритету")
        void getTasks_Priority_Return200() throws Exception {
            when(tasksService.getAllTaskByPriority("LOW")).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/tasks").param("priority", "LOW"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].priority").value("LOW"));
            verify(tasksService, never()).getAllTaskByStatus(anyString());
            verify(tasksService, times(1)).getAllTaskByPriority("LOW");
            verify(tasksService, never()).getAllTask();
        }
        //==========================================================
        // Получение всех задач без условий
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/tasks")
        void getTasks_AllTasks_Return200() throws Exception {
            when(tasksService.getAllTask()).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/tasks"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
            verify(tasksService, never()).getAllTaskByStatus(anyString());
            verify(tasksService, never()).getAllTaskByPriority(anyString());
            verify(tasksService, times(1)).getAllTask();
        }
        //==========================================================
        // Получение задач по проекту
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/tasks/projects/{projectId}")
        void getTasksByProjects_Return200() throws Exception {
            when(tasksService.getAllTaskByProject(1L)).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/tasks/projects/{projectId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].projectName").value("Название проекта"))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Название задачи"));
            verify(tasksService, times(1)).getAllTaskByProject(1L);
        }
        //==========================================================
        // Получение задач назначенных пользователю
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/tasks/users/assignee/{assigneeId}")
        void getTasksByAssignee_Return200() throws Exception {
            when(tasksService.getAllTaskByAssignee(2L)).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/tasks/users/assignee/{assigneeId}", 2L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].assigneeId").value(2L));
            verify(tasksService, times(1)).getAllTaskByAssignee(2L);
        }
        //==========================================================
        // Получение задач созданных пользователем
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/tasks/users/creator/{creatorId}")
        void getAllTaskByCreated_Return200() throws Exception {
            when(tasksService.getAllTaskByCreated(1L)).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/tasks/users/creator/{creatorId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].projectName").value("Название проекта"));
            verify(tasksService, times(1)).getAllTaskByCreated(1L);
        }
        //==========================================================
        // Удаление задачи
        //==========================================================
        @Test
        @DisplayName("DELETE /manager/api/tasks/{id}")
        void deleteTaskById_Return200() throws Exception {
            doNothing().when(tasksService).deleteTaskById(CORRECT_ID);
            mockMvc.perform(delete("/manager/api/tasks/{id}", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent());
            verify(tasksService, times(1)).deleteTaskById(CORRECT_ID);
        }
        //==========================================================
        // Обновление задачи
        //==========================================================
        @Test
        @DisplayName("PUT /manager/api/tasks/{id}")
        void updateTaskById_Return200() throws Exception {
            String json = objectMapper.writeValueAsString(testUpdateRequest);
            when(tasksService.updateTaskById(CORRECT_ID, testUpdateRequest)).thenReturn(testUpdateResponse);
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Обновленное название задачи"))
                    .andExpect(jsonPath("$.description").value("Обновленное описание задачи"));
            verify(tasksService, times(1)).updateTaskById(CORRECT_ID, testUpdateRequest);
        }
    }
    @Nested
    @DisplayName("Ошибка клиента http status (4xx)")
    class ClientErrorTest{

        // =============================================================================
        // ОШИБКА КЛИЕНТА СОЗДАНИЕ ЗАДАЧИ POST /manager/api/tasks
        //==========================================================
        // Возможные ошибки валидации
        // 1) Пустое название задачи
        @Test
        @DisplayName("400 - неправильный title")
        void createTask_TitleIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": null,
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 2) Нарушение размера названия задачи
        @Test
        @DisplayName("400 - Нарушение размера title")
        void createTask_InvalidTitleSize_Return400() throws Exception {
            String requestJson = """
            {
                "title": "3",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 3) Ошибка паттерна статуса, статус может быть "TODO |IN_PROGRESS|DONE")
        @Test
        @DisplayName("400 - Статус не должен быть пустым")
        void createTask_StatusIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": null,
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 4) Нарушение паттерна Status
        @Test
        @DisplayName("400 - Нарушение паттерна статуса")
        void createTask_InvalidStatus_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "HELLO,WORLD",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 5) Приоритет не должен быть пустым
        @Test
        @DisplayName("400 - Приоритет не должен быть пустым")
        void createTask_PriorityIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": null,
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 6) Нарушение паттерна приоритета
        @Test
        @DisplayName("400 - Нарушение паттерна приоритета")
        void createTask_InvalidPriority_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "HELLO",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 7) Исполнитель не должен быть null
        @Test
        @DisplayName("400 - Исполнителя не существует")
        void createTask_AssigneeIdIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": null,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 8) ID исполнителя должно быть положительным
        @Test
        @DisplayName("400 - Исполнителя с отрицательным id")
        void createTask_AssigneeIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": -1,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 9) Проект не может быть null
        @Test
        @DisplayName("400 - Проекта не существует")
        void createTask_ProjectIdIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": null,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 10) ID проекта должно быть положительным
        @Test
        @DisplayName("400 - Проект с отрицательным id")
        void createTask_ProjectIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": -1,
                "createdById": 1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 11) Создатель задачи не может быть null
        @Test
        @DisplayName("400 - Создателя задачи не существует")
        void createTask_CreatedIdIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": 1,
                "createdById": null
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 12) ID Создателя должно быть положительным
        @Test
        @DisplayName("400 - Создателя задачи с отрицательным id")
        void createTask_CreatedIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": 1,
                "createdById": -1
            }
            """;
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,never()).createTask(any(TasksRequestDTO.class));
        }
        // 13) Исполнитель не существует
        @Test
        @DisplayName("404 - Исполнитель не найден")
        void createTask_AssigneeNotFound_Return404() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 999,
                "projectId": 1,
                "createdById": 1
            }
            """;
            when(tasksService.createTask(any(TasksRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Исполнитель", 999L));
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService,times(1)).createTask(any(TasksRequestDTO.class));
        }
        // 14) Создатель не существует
        @Test
        @DisplayName("404 - Создатель не найден")
        void createTask_CreatorNotFound_Return404() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": 1,
                "createdById": 999
            }
            """;
            when(tasksService.createTask(any(TasksRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Создатель", 999L));
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService,times(1)).createTask(any(TasksRequestDTO.class));
        }
        // 15) Проект не существует
        @Test
        @DisplayName("404 - Проект не найден")
        void createTask_ProjectNotFound_Return404() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": 1,
                "createdById": 1
            }
            """;
            when(tasksService.createTask(any(TasksRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Проект", 999L));
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService,times(1)).createTask(any(TasksRequestDTO.class));
        }
        // 16) Исполнитель не является участником проекта
        @Test
        @DisplayName("400 - Пользователь не является участником проекта")
        void createTask_UserNotExecutor_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-24",
                "assigneeId": 1,
                "projectId": 1,
                "createdById": 1
            }
            """;
            when(tasksService.createTask(any(TasksRequestDTO.class)))
                    .thenThrow(new BadRequestException("Исполнитель должен быть участником проекта"));
            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService,times(1)).createTask(any(TasksRequestDTO.class));
        }
        // =============================================================================
        // НЕУДАЧНЫЕ СЦЕНАРИИ ПОЛУЧЕНИЯ ЗАДАЧИ ПО ID
        // =============================================================================
        // 1) Отрицательный id
        @Test
        @DisplayName("400 - Отрицательное значение id задачи")
        void getTaskById_IdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/tasks/{id}", -1L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).getTaskById(-1L);
        }
        // 2) Задачи с таким id не существует
        @Test
        @DisplayName("404 - Задачи с данным id не существует")
        void getTaskById_NotFound_Return404() throws Exception {
            when(tasksService.getTaskById(9999L)).thenThrow(new ResourceNotFoundException("Задача", 9999L));
            mockMvc.perform(get("/manager/api/tasks/{id}", 9999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService, times(1)).getTaskById(9999L);
        }
        // =============================================================================
        // Получение всех задач по статусу, не валидный status
        // + получение всех задач по priority не валидный приоритет
        // =============================================================================
        // 1) Нарушение паттерна статуса
        @Test
        @DisplayName("400 - Статус с нарушенным паттерном")
        void getTasks_InvalidStatus_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/tasks").param("status", "hello"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).getAllTaskByStatus("hello");
        }
        // 2) Нарушение паттерна приоритета
        @Test
        @DisplayName("400 - Приоритет с нарушенным паттерном")
        void getTask_InvalidPriority_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/tasks").param("priority", "hello"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).getAllTaskByPriority("hello");
        }
        // =============================================================================
        // ПОЛУЧЕНИЕ ЗАДАЧ ПО ПРОЕКТУ
        // =============================================================================
        // 1) Проект с отрицательным id
        @Test
        @DisplayName("400 - Проект с отрицательным id")
        void getTasksByProject_ProjectIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/tasks/projects/{projectId}", -1L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).getAllTaskByProject(-1L);
        }
        // 2) Проект с данным id не существует
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void getTasksByProject_ProjectNotFound_Return404() throws Exception {
            when(tasksService.getAllTaskByProject(999L)).thenThrow(new ResourceNotFoundException("Проект", 999L));
            mockMvc.perform(get("/manager/api/tasks/projects/{projectId}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService, times(1)).getAllTaskByProject(999L);
        }
        // =============================================================================
        // ПОЛУЧЕНИЕ ЗАДАЧ НАЗНАЧЕННЫХ ПОЛЬЗОВАТЕЛЮ
        // =============================================================================
        // 1) Отрицательное значение id исполнителя
        @Test
        @DisplayName("400 - Отрицательное значение id исполнителя")
        void getAllTasksByAssignee_AssigneeIdInvalid_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/tasks/users/assignee/{assigneeId}", -1L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).getAllTaskByAssignee(-1L);
        }
        // 2) Исполнитель с таким id не найден
        @Test
        @DisplayName("404 - Исполнителя с данным id не существует")
        void getAllTasksByAssignee_AssigneeIsNotFound_Return404() throws Exception {
            when(tasksService.getAllTaskByAssignee(999L)).thenThrow(new ResourceNotFoundException("Исполнитель", 999L));
            mockMvc.perform(get("/manager/api/tasks/users/assignee/{assigneeId}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService, times(1)).getAllTaskByAssignee(999L);
        }
        // =============================================================================
        // ПОЛУЧЕНИЕ ЗАДАЧ ПО ID СОЗДАТЕЛЯ - НЕУДАЧНЫЕ СЦЕНАРИИ
        // =============================================================================
        // 1) Id создателя является отрицательным
        @Test
        @DisplayName("400 - Отрицательное значение id создателя")
        void getAllTasksByCreator_CreatorIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/tasks/users/creator/{creatorId}",-100L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).getAllTaskByCreated(-100L);
        }
        // 2) Создателя с данным id не существует
        @Test
        @DisplayName("404 - Создатель с данным id не существует")
        void getAllTasksByCreator_CreatorNotFound_Return404() throws Exception {
            when(tasksService.getAllTaskByCreated(1000L)).thenThrow(new ResourceNotFoundException("Создатель", 1000L));
            mockMvc.perform(get("/manager/api/tasks/users/creator/{creatorId}", 1000L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService, times(1)).getAllTaskByCreated(1000L);
        }
        // =============================================================================
        // УДАЛЕНИЕ ЗАДАЧИ ПО ID - НЕУДАЧНЫЕ СЦЕНАРИИ
        // =============================================================================
        // 1) ID задачи отрицательное
        @Test
        @DisplayName("400 - Отрицательное id задачи")
        void deleteTaskById_IdIsNegative_Return400() throws Exception {
            mockMvc.perform(delete("/manager/api/tasks/{id}", -100L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).deleteTaskById(-100L);
        }
        // 2) Задачи с данным id не существует
        @Test
        @DisplayName("404 - Задачи с данным id не существует")
        void deleteTaskById_TaskNotFound_Return404() throws Exception {
            doThrow(new ResourceNotFoundException("Задача", 1000L)).when(tasksService).deleteTaskById(1000L);
            mockMvc.perform(delete("/manager/api/tasks/{id}", 1000L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService, times(1)).deleteTaskById(1000L);
        }
        // =============================================================================
        // ОБНОВЛЕНИЕ ЗАДАЧИ ПО ID - НЕУДАЧНЫЕ СЦЕНАРИИ
        // =============================================================================
        // Нарушение валидации
        // 1) Пустое название задачи
        @Test
        @DisplayName("400 - Название задачи пустое")
        void updateTaskById_TitleIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": null,
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 2) Нарушение размера названия задачи
        @Test
        @DisplayName("400 - Название задачи пустое")
        void updateTaskById_TitleSizeInvalid_Return400() throws Exception {
            String requestJson = """
            {
                "title": "222",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 3) Статус пустой
        @Test
        @DisplayName("400 - Статус задачи пустой")
        void updateTaskById_StatusIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": null,
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 4) Нарушение статуса паттерна
        @Test
        @DisplayName("400 - Нарушение статуса паттерна")
        void updateTaskById_StatusInvalid_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "efwefewfw",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 5) Приоритет пустой
        @Test
        @DisplayName("400 - Приоритет пустой")
        void updateTaskById_PriorityIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": null,
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 6) Нарушение паттерна приоритета у задачи
        @Test
        @DisplayName("400 - Нарушение паттерна приоритета")
        void updateTaskById_PriorityInvalid_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "1231312321",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 7) id исполнителя отрицательное
        @Test
        @DisplayName("400 - id исполнителя отрицательное")
        void updateTaskById_AssigneeIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": -2,
                "projectId": 1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 8) id проекта отрицательное
        @Test
        @DisplayName("400 - id проекта отрицательное")
        void updateTaskById_ProjectIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": -1,
                "createdById": 1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 9) id создателя отрицательное
        @Test
        @DisplayName("400 - id создателя отрицательное")
        void updateTaskById_CreatorIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "title": "Название задачи",
                "description": "Описание задачи",
                "status": "TODO",
                "priority": "LOW",
                "dueData": "2025-12-31",
                "assigneeId": 2,
                "projectId": 1,
                "createdById": -1
            }
            """;
            mockMvc.perform(put("/manager/api/tasks/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(tasksService, never()).updateTaskById(CORRECT_ID, eq(any(TasksRequestDTO.class)));
        }
        // 10) Попытка обновить не существующую задачу
        @Test
        @DisplayName("404 - Задача с данным id не существует")
        void updateTaskById_TaskNotFound_Return404() throws Exception{
            String json = objectMapper.writeValueAsString(testRequest);
            when(tasksService.updateTaskById(999L, testRequest)).thenThrow(new ResourceNotFoundException("Задача", 999L));
            mockMvc.perform(put("/manager/api/tasks/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(tasksService, times(1)).updateTaskById(999L, testRequest);
        }
    }
    //==========================================================
    // ПРОВЕРКА, ЧТО 5ХХ ОБРАБАТЫВАЮТСЯ
    //==========================================================
    @Nested
    @DisplayName("Ошибки сервера (5хх)")
    class ServerErrorTest{
        @Test
        @DisplayName("500 - Неожиданная ошибка при создании задачи")
        void createTask_InternalServerError_Return500() throws Exception {
            String json = objectMapper.writeValueAsString(testRequest);
            when(tasksService.createTask(any(TasksRequestDTO.class)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/manager/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Ошибка сервера"));
        }

        @Test
        @DisplayName("500 - Неожиданная ошибка при получении задачи по id")
        void getTaskById_InternalServerError_Return500() throws Exception {
            when(tasksService.getTaskById(CORRECT_ID))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(get("/manager/api/tasks/{id}", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Ошибка сервера"));
        }
    }
}
