package com.taskmanager.contoller.projects;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.request.projects.ProjectRequestDTO;
import com.taskmanager.dto.response.projects.ProjectResponseDTO;
import com.taskmanager.dto.response.projects.ProjectResponseParticipantDTO;
import com.taskmanager.dto.response.projects.ProjectResponseSummaryDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.service.projects.ProjectService;
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
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProjectsControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ProjectService projectService;

    // ========== ОБЩИЕ ДАННЫЕ (для всех тестов) ==========
    private final Long CORRECT_ID = 1L;
    private ProjectRequestDTO testRequest;
    private ProjectRequestDTO testUpdateRequest;
    private ProjectResponseDTO testResponse;
    private ProjectResponseDTO testUpdateResponse;
    private ProjectResponseParticipantDTO testParticipant;
    private ProjectResponseSummaryDTO testSummary;

    @BeforeEach
    void setUp(){
        testRequest = new ProjectRequestDTO(
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                2L
        );
        testUpdateRequest = new ProjectRequestDTO(
                "Обновленное название проекта",
                "Обновленное описание проекта",
                "ACTIVE",
                100L
        );
        testUpdateResponse = new ProjectResponseDTO(
                1L,
                "Обновленное название проекта",
                "Обновленное описание проекта",
                "ACTIVE",
                2L,
                "Иван",
                "Чухманов",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        testResponse = new ProjectResponseDTO(
                1L,
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                2L,
                "Иван",
                "Чухманов",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );
        testParticipant = new ProjectResponseParticipantDTO(
                3L,
                "Иван",
                "Филипов",
                "test@test.com",
                "USER"
        );
        testSummary = new ProjectResponseSummaryDTO(
                1L,
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );
    }
    //==========================================================
    //                   УСПЕШНЫЕ СЦЕНАРИИ                    //
    //==========================================================
    @Nested
    @DisplayName("Успешные сценарии http status (2xx)")
    class SuccessTests {
        //==========================================================
        //  СОЗДАНИЕ ПРОЕКТА С УКАЗАННЫМ ВЛАДЕЛЬЦЕМ
        //==========================================================
        @Test
        @DisplayName("POST /manager/api/projects - Успешное создание нового проекта")
        void createProject_Return201() throws Exception {
            when(projectService.createProject(testRequest)).thenReturn(testResponse);
            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Название проекта"))
                    .andExpect(jsonPath("$.description").value("Описание проекта"))
                    .andExpect(jsonPath("$.ownerId").value(2L));
            verify(projectService, times(1)).createProject(testRequest);
        }

        //==========================================================
        // ПОЛУЧЕНИЕ ПРОЕКТА ПО ID УСПЕШНЫЙ СЦЕНАРИЙ
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects/{id} - Успешное получение информации о проекте")
        void getProjectById_Return200() throws Exception {
            when(projectService.getProjectById(CORRECT_ID)).thenReturn(testResponse);
            mockMvc.perform(get("/manager/api/projects/{id}", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Название проекта"))
                    .andExpect(jsonPath("$.description").value("Описание проекта"))
                    .andExpect(jsonPath("$.ownerId").value(2L));
            verify(projectService, times(1)).getProjectById(CORRECT_ID);
        }

        //==========================================================
        // ПОЛУЧЕНИЕ СПИСКА ВСЕХ ПРОЕКТОВ
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects - Успешное получение списка проектов")
        void getProjects_AllProject_Return200() throws Exception {
            when(projectService.getAllProjects()).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/projects"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Название проекта"))
                    .andExpect(jsonPath("$[0].description").value("Описание проекта"))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));
            verify(projectService, times(1)).getAllProjects();
        }

        //==========================================================
        // ПОЛУЧЕНИЕ ПРОЕКТА ПО СТАТУСУ
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects - Успешное получение списка проектов по статусу")
        void getProjects_AllByStatus_Return200() throws Exception {
            when(projectService.getProjectByStatus("ACTIVE")).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/projects").param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Название проекта"))
                    .andExpect(jsonPath("$[0].description").value("Описание проекта"))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));
            verify(projectService, times(1)).getProjectByStatus("ACTIVE");
        }

        //==========================================================
        // ПОЛУЧЕНИЕ ПРОЕКТА ГДЕ ПОЛЬЗОВАТЕЛЬ ВЛАДЕЛЕЦ
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects/owner/{id} - Успешное получение проектов, где пользователь владелец")
        void getProjectByOwnerId_Return200() throws Exception {
            when(projectService.getProjectByOwnerId(2L)).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/projects/owner/{id}", 2L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Название проекта"));
            verify(projectService, times(1)).getProjectByOwnerId(2L);
        }

        //==========================================================
        // ПОЛУЧЕНИЕ ПРОЕКТОВ, ГДЕ ПОЛЬЗОВАТЕЛЬ УЧАСТНИК
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects/participant/{id} - Успешное получения проектов, где пользователь участник")
        void getProjectByParticipant_Return200() throws Exception {
            when(projectService.getProjectsByParticipant(3L)).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/projects/participant/{id}", 3L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Название проекта"));
            verify(projectService, times(1)).getProjectsByParticipant(3L);
        }

        //==========================================================
        // ОБНОВЛЕНИЕ ПРОЕКТА ПО ID
        //==========================================================
        @Test
        @DisplayName("PUT /manager/api/projects/{id} - Успешное обновление проекта")
        void updateProjectById_Return200() throws Exception {
            when(projectService.updateProject(CORRECT_ID, testUpdateRequest)).thenReturn(testUpdateResponse);
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUpdateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Обновленное название проекта"))
                    .andExpect(jsonPath("$.description").value("Обновленное описание проекта"));
            verify(projectService, times(1)).updateProject(CORRECT_ID, testUpdateRequest);
        }

        //==========================================================
        // УДАЛЕНИЕ ПРОЕКТА ПО ID
        //==========================================================
        @Test
        @DisplayName("DELETE /manager/api/projects/{id} - Успешное удаление проекта")
        void deleteProjectById_Return204() throws Exception {
            doNothing().when(projectService).deleteProjectById(CORRECT_ID);
            mockMvc.perform(delete("/manager/api/projects/{id}", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent());
            verify(projectService, times(1)).deleteProjectById(CORRECT_ID);
        }

        //==========================================================
        // ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ В ПРОЕКТ
        //==========================================================
        @Test
        @DisplayName("PUT /manager/api/projects/{id}/users/{userId} - Успешное добавление пользователя в проект")
        void addUserToProject_Return200() throws Exception {
            when(projectService.addUserToProject(CORRECT_ID, 2L)).thenReturn(testResponse);
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}", CORRECT_ID, 2L))
                    .andDo(print())
                    .andExpect(status().isOk());
            verify(projectService, times(1)).addUserToProject(CORRECT_ID, 2L);
        }

        //==========================================================
        // УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ИЗ ПРОЕКТА
        //==========================================================
        @Test
        @DisplayName("DELETE  /manager/api/projects/{id}/users/{userId} - Успешное удаление пользователя из перокта")
        void deleteUserFromProject_Return200() throws Exception {
            doNothing().when(projectService).deleteUserFromProject(CORRECT_ID, 2L);
            mockMvc.perform(delete("/manager/api/projects/{id}/users/{userId}", CORRECT_ID, 2L))
                    .andDo(print())
                    .andExpect(status().isNoContent());
            verify(projectService, times(1)).deleteUserFromProject(CORRECT_ID, 2L);
        }

        //==========================================================
        // ПРОВЕРКА ЯВЛЯЕТСЯ ЛИ ПОЛЬЗОВАТЕЛЬ УЧАСТНИКОМ ПРОЕКТА
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects/{id}/users/{userId} - Успешная проверка")
        void isUserInProject_Return200() throws Exception {
            when(projectService.isUserInProject(CORRECT_ID, 2L)).thenReturn(true);
            mockMvc.perform(get("/manager/api/projects/{id}/users/{userId}", CORRECT_ID, 2L))
                    .andDo(print())
                    .andExpect(status().isOk());
            verify(projectService, times(1)).isUserInProject(CORRECT_ID, 2L);
        }

        //==========================================================
        // ПОЛУЧЕНИЕ СПИСКА УЧАСТНИКОВ ПРОЕКТА
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects/{id}/participants - Успешное получение списка")
        void getProjectParticipants_Return200() throws Exception {
            when(projectService.getProjectParticipants(CORRECT_ID)).thenReturn(List.of(testParticipant));
            mockMvc.perform(get("/manager/api/projects/{id}/participants", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].firstName").value("Иван"))
                    .andExpect(jsonPath("$[0].lastName").value("Филипов"));
            verify(projectService, times(1)).getProjectParticipants(CORRECT_ID);
        }

        //==========================================================
        // ПОЛУЧЕНИЕ СПИСКА ПРОЕКТОВ ПОЛЬЗОВАТЕЛЯ
        //==========================================================
        @Test
        @DisplayName("GET /manager/api/projects/users/{usersId} - Успешное получение списка проектов пользователя")
        void getUserProjects_Return200() throws Exception {
            when(projectService.getUserProjects(3L)).thenReturn(List.of(testSummary));
            mockMvc.perform(get("/manager/api/projects/users/{usersId}", 3L))
                    .andDo(print())
                    .andExpect(status().isOk());
            verify(projectService, times(1)).getUserProjects(3L);
        }
    }

    @Nested
    @DisplayName("Ошибка клиента http status (4xx)")
    class ClientError{
        // =======================================================================
        // 4хх - Создание проекта с указанным владельцем
        // =======================================================================
        @Test
        @DisplayName("400 - Некорректные название проекта")
        void createProject_ProjectNameIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "name": null,
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": 2
            }
            """;
            mockMvc.perform(post("/manager/api/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
        }
        @Test
        @DisplayName("400 - Нарушение размера названия проекта")
        void createProject_ProjectNameSizeInvalid_Return400() throws Exception {
            String requestJson = """
            {
                "name": "22",
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": 2
            }
            """;
            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
        }

        @Test
        @DisplayName("400 - Статус пустой")
        void createProject_StatusIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название",
                "description": "Описание проекта",
                "status": null,
                "ownerId": 2
            }
            """;
            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
        }
        @Test
        @DisplayName("400 - Статус нарушается паттерну")
        void createProject_StatusInvalid_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название",
                "description": "Описание проекта",
                "status": "HELLO",
                "ownerId": 2
            }
            """;
            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
        }
        @Test
        @DisplayName("400 - id создателя пустое")
        void createProject_OwnerIdIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название",
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": null
            }
            """;
            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
        }
        @Test
        @DisplayName("400 - id создателя не может быть отрицательным")
        void createProject_OwnerIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название",
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": -1
            }
            """;
            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
        }
        @Test
        @DisplayName("404 - Данного владельца не существует")
        void createProject_OwnerNotFound_Return404() throws Exception{
            when(projectService.createProject(testRequest)).thenThrow(new ResourceNotFoundException("Создатель", 2L));
            mockMvc.perform(post("/manager/api/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).createProject(testRequest);
        }
        // =======================================================================
        // =======================================================================
        // Провальный сценарий Получение проекта по id
        // =======================================================================
        @Test
        @DisplayName("400 - id проекта отрицательное")
        void getProjectById_ProjectIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/projects/{id}", -2L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).getProjectById(-2L);
        }
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void getProjectById_ProjectNotFound_Return404() throws Exception {
            when(projectService.getProjectById(999L)).thenThrow(new ResourceNotFoundException("Проект", 999L));
            mockMvc.perform(get("/manager/api/projects/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).getProjectById(999L);
        }
        // =======================================================================
        // ПОЛУЧЕНИЕ СПИСКА ВСЕХ ПРОЕКТОВ ПО STATUS
        // =======================================================================
        @Test
        @DisplayName("400 - Статус нарушает паттерн")
        void getProjects_StatusIsInvalid_Return400() throws Exception{
            mockMvc.perform(get("/manager/api/projects").param("status", "hello"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).getProjectByStatus("hello");
        }
        // =======================================================================
        // Получение проектов где пользователь владелец
        // =======================================================================
        @Test
        @DisplayName("400 - id владельца не может быть отрицательным")
        void getProjectByOwnerId_OwnerIdIsInvalid_Return400() throws Exception{
            mockMvc.perform(get("/manager/api/projects/owner/{id}",-20L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).getProjectByOwnerId(-20L);
        }
        @Test
        @DisplayName("404 - Владельца с данным id не существует")
        void getProjectByOwnerId_OwnerNotFound_Return404() throws Exception {
            when(projectService.getProjectByOwnerId(999L)).thenThrow(new ResourceNotFoundException("Владелец", 999L));
            mockMvc.perform(get("/manager/api/projects/owner/{id}",999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).getProjectByOwnerId(999L);
        }
        // =======================================================================
        //  Получение проектов где пользователь участник
        // =======================================================================
        @Test
        @DisplayName("400 - Участник с отрицательным id")
        void getProjectByParticipant_ParticipantIdInvalid_Return400() throws Exception{
            mockMvc.perform(get("/manager/api/projects/participant/{id}", -200L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).getProjectsByParticipant(-200L);
        }
        @Test
        @DisplayName("404 - Участник с данным id не существует")
        void getProjectByParticipant_NotFound_Return404() throws Exception{
            when(projectService.getProjectsByParticipant(200L)).thenThrow(new ResourceNotFoundException("Участник", 200L));
            mockMvc.perform(get("/manager/api/projects/participant/{id}", 200L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).getProjectsByParticipant(200L);
        }
        // =======================================================================
        // Обновление проекта нарушение валидации как с id так и с request json
        // =======================================================================
        @Test
        @DisplayName("400 - Отрицательное id проекта")
        void updateProjectById_ProjectIdNegative_Return400() throws Exception{
            mockMvc.perform(put("/manager/api/projects/{id}", -100L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(-100L, testRequest);
        }
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void updateProjectById_ProjectNotFound_Return404() throws Exception{
            when(projectService.updateProject(100L,testRequest)).thenThrow(new ResourceNotFoundException("Проект",100L));
            mockMvc.perform(put("/manager/api/projects/{id}", 100L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).updateProject(100L, testRequest);
        }
        // ПРОВЕРКА ВАЛИДАЦИИ ВХОДНЫХ ДАННЫХ REQUEST
        @Test
        @DisplayName("400 - Пустое название проекта")
        void updateProjectById_ProjectNameIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "name": null,
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": 1
            }
            """;
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(CORRECT_ID, testRequest);
        }
        @Test
        @DisplayName("400 - Нарушение названия проекта")
        void updateProjectById_ProjectNameInvalidSize_Return400() throws Exception {
            String requestJson = """
            {
                "name": "2",
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": 1
            }
            """;
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(CORRECT_ID, testRequest);
        }
        @Test
        @DisplayName("400 - Пустое значение статуса")
        void updateProjectById_StatusIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название проекта",
                "description": "Описание проекта",
                "status": null,
                "ownerId": 1
            }
            """;
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(CORRECT_ID, testRequest);
        }
        @Test
        @DisplayName("400 - Нарушение паттерна status")
        void updateProjectById_StatusInvalid_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название проекта",
                "description": "Описание проекта",
                "status": "HELLO",
                "ownerId": 1
            }
            """;
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(CORRECT_ID, testRequest);
        }
        @Test
        @DisplayName("400 - Пустое id создателя")
        void updateProjectById_OwnerIdIsNull_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название проекта",
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": null
            }
            """;
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(CORRECT_ID, testRequest);
        }
        @Test
        @DisplayName("400 - Отрицательное значение id создателя")
        void updateProjectById_OwnerIdIsNegative_Return400() throws Exception {
            String requestJson = """
            {
                "name": "Название проекта",
                "description": "Описание проекта",
                "status": "ACTIVE",
                "ownerId": -1
            }
            """;
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).updateProject(CORRECT_ID, testRequest);
        }
        // Нужно добавить последнюю проверку, когда создателя не было найдено
        @Test
        @DisplayName("404 - Владельца с данным id не существует")
        void updateProjectById_OwnerNotFound_Return404() throws Exception {
            Long nonExistentOwnerId = 999L;
            String requestJson = """
        {
            "name": "Обновлённое название",
            "description": "Обновлённое описание",
            "status": "ACTIVE",
            "ownerId": %d
        }
        """.formatted(nonExistentOwnerId);
            when(projectService.updateProject(eq(CORRECT_ID), any(ProjectRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Владелец", nonExistentOwnerId));
            mockMvc.perform(put("/manager/api/projects/{id}", CORRECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Владелец с id 999 не найден"));
            verify(projectService, times(1)).updateProject(eq(CORRECT_ID), any(ProjectRequestDTO.class));
        }
        //==========================================================
        // УДАЛЕНИЕ ПРОЕКТА ПО ID
        //==========================================================
        @Test
        @DisplayName("400 - Отрицательное значение id проекта")
        void deleteProjectById_IdIsNegative_Return400() throws Exception {
            mockMvc.perform(delete("/manager/api/projects/{id}", -100L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).deleteProjectById(-100L);
        }
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void deleteProjectById_ProjectNotFound_Return404() throws Exception {
           doThrow(new ResourceNotFoundException("Проект", 999L)).when(projectService).deleteProjectById(999L);
           mockMvc.perform(delete("/manager/api/projects/{id}", 999L))
                   .andDo(print())
                   .andExpect(status().isNotFound());
           verify(projectService, times(1)).deleteProjectById(999L);
        }
        //==========================================================
        // ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ В ПРОЕКТ
        //==========================================================
        @Test
        @DisplayName("400 - id проекта отрицательное")
        void addUserToProject_ProjectIdIsNegative_Return400() throws Exception {
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}",-100L, 2L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).addUserToProject(-100L,2L);
        }
        @Test
        @DisplayName("400 - id пользователя отрицательное")
        void addUserToProject_UserIdIsNegative_Return400() throws Exception {
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}",100L, -2L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).addUserToProject(100L,-2L);
        }
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void addUserToProject_ProjectNotFound_Return404() throws Exception {
            when(projectService.addUserToProject(100L,2L)).thenThrow(new ResourceNotFoundException("Проект", 100L));
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}",100L, 2L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).addUserToProject(100L,2L);
        }
        @Test
        @DisplayName("404 - Пользователь с данным id не существует")
        void addUserToProject_UserNotFound_Return404() throws Exception {
            when(projectService.addUserToProject(1L,200L)).thenThrow(new ResourceNotFoundException("Пользователь", 200L));
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}",1L, 200L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).addUserToProject(1L,200L);
        }
        @Test
        @DisplayName("400 - Пользователь является владельцем проекта")
        void addUserToProject_UserIsCreator_Return400() throws Exception {
            when(projectService.addUserToProject(1L,2L)).thenThrow(new BadRequestException("Пользователь является владельцем проекта"));
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}",1L, 2L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, times(1)).addUserToProject(1L,2L);
        }
        @Test
        @DisplayName("400 - Пользователь уже добавлен")
        void addUserToProject_UserIsFound_Return400() throws Exception {
            when(projectService.addUserToProject(1L,2L)).thenThrow(new BadRequestException("Попытка добавить дубликат пользователя"));
            mockMvc.perform(put("/manager/api/projects/{id}/users/{userId}",1L, 2L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, times(1)).addUserToProject(1L,2L);
        }
        //==========================================================
        // УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ИЗ ПРОЕКТА
        //==========================================================
        @Test
        @DisplayName("400 - id проекта отрицательное")
        void deleteUserFromProject_ProjectIdIsNegative_Return400() throws Exception {
            mockMvc.perform(delete("/manager/api/projects/{id}/users/{userId}", -100L, 20L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).deleteUserFromProject(-100L, 20L);
        }
        @Test
        @DisplayName("400 - id пользователя отрицательное")
        void deleteUserFromProject_UserIdIsNegative_Return400() throws Exception {
            mockMvc.perform(delete("/manager/api/projects/{id}/users/{userId}", 100L, -20L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).deleteUserFromProject(100L, -20L);
        }
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void deleteUserFromProject_ProjectNotFound_Return404() throws Exception {
            doThrow(new ResourceNotFoundException("Проект", 100L)).when(projectService).deleteUserFromProject(100L, 20L);
            mockMvc.perform(delete("/manager/api/projects/{id}/users/{userId}", 100L, 20L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).deleteUserFromProject(100L, 20L);
        }
        @Test
        @DisplayName("404 - Пользователь с данным id не существует")
        void deleteUserFromProject_UserNotFound_Return404() throws Exception {
            doThrow(new ResourceNotFoundException("Пользователь", 20L)).when(projectService).deleteUserFromProject(100L, 20L);
            mockMvc.perform(delete("/manager/api/projects/{id}/users/{userId}", 100L, 20L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).deleteUserFromProject(100L, 20L);
        }
        @Test
        @DisplayName("400 - Попытка удаления владельца проекта")
        void deleteUserFromProject_UserIsCreator_Return400() throws Exception {
            doThrow(new BadRequestException("Попытка удаления владельца проекта")).when(projectService).deleteUserFromProject(100L, 20L);
            mockMvc.perform(delete("/manager/api/projects/{id}/users/{userId}", 100L, 20L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, times(1)).deleteUserFromProject(100L, 20L);
        }
        //==========================================================
        // Проверка является ли пользователь участником проекта
        //==========================================================
        @Test
        @DisplayName("400 - id проекта отрицательное")
        void isUserInProject_ProjectIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/projects/{id}/users/{userId}", -100L, 10L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).isUserInProject(-100L, 10L);
        }
        @Test
        @DisplayName("400 - id пользователя отрицательное")
        void isUserInProject_UserIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/projects/{id}/users/{userId}", 100L, -10L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).isUserInProject(100L, -10L);
        }
        @Test
        @DisplayName("404 - Проект с данным id не существует")
        void isUserInProject_ProjectNotFound_Return404() throws Exception {
            when(projectService.isUserInProject(100L, 20L)).thenThrow(new ResourceNotFoundException("Проект",100L));
            mockMvc.perform(get("/manager/api/projects/{id}/users/{userId}", 100L, 20L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).isUserInProject(100L, 20L);
        }
        @Test
        @DisplayName("404 - Пользователь с данным id не существует")
        void isUserInProject_UserNotFound_Return404() throws Exception {
            when(projectService.isUserInProject(100L, 20L)).thenThrow(new ResourceNotFoundException("Пользователь",20L));
            mockMvc.perform(get("/manager/api/projects/{id}/users/{userId}", 100L, 20L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).isUserInProject(100L, 20L);
        }
        //==========================================================
        // Получение списка участников проекта
        //==========================================================
        @Test
        @DisplayName("400 - id проекта отрицательное")
        void getProjectParticipants_ProjectIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/projects/{id}/participants",-100L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).getProjectsByParticipant(-100L);
        }
        @Test
        @DisplayName("404 - Проекта с данным id не существует")
        void getProjectParticipants_ProjectNotFound_Return404() throws Exception {
            when(projectService.getProjectParticipants(100L)).thenThrow(new ResourceNotFoundException("Проект", 100L));
            mockMvc.perform(get("/manager/api/projects/{id}/participants", 100L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).getProjectParticipants(100L);
        }
        //==========================================================
        // Получение списка проектов пользователя
        //==========================================================
        @Test
        @DisplayName("400 - id пользователя отрицательное")
        void getUserProjects_UserIdIsNegative_Return400() throws Exception {
            mockMvc.perform(get("/manager/api/projects/users/{usersId}",-20L))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            verify(projectService, never()).getUserProjects(-20L);
        }
        @Test
        @DisplayName("404 - Пользователь с данным id не существует")
        void getUserProjects_UserNotFound_Return404() throws Exception {
            when(projectService.getUserProjects(100L)).thenThrow(new ResourceNotFoundException("Пользователь", 100L));
            mockMvc.perform(get("/manager/api/projects/users/{usersId}",100L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
            verify(projectService, times(1)).getUserProjects(100L);
        }
    }
    @Nested
    @DisplayName("Ошибки сервера (5хх)")
    class ServerErrorTest{
        @Test
        @DisplayName("500 - Неожиданная ошибка при создании проекта")
        void createProject_InternalServerError_Return500() throws Exception {
            when(projectService.createProject(any(ProjectRequestDTO.class)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/manager/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Ошибка сервера"));
        }
        @Test
        @DisplayName("500 - Неожиданная ошибка при получении проекта по id")
        void getProjectById_InternalServerError_Return500() throws Exception {
            when(projectService.getProjectById(CORRECT_ID))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(get("/manager/api/projects/{id}", CORRECT_ID))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Ошибка сервера"));
        }
    }

}



