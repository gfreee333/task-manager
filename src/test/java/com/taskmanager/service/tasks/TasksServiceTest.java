package com.taskmanager.service.tasks;

import com.taskmanager.dto.request.tasks.TasksRequestDTO;
import com.taskmanager.dto.response.tasks.TasksResponseDTO;
import com.taskmanager.dto.response.tasks.TasksSummaryDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.model.Projects;
import com.taskmanager.model.Tasks;
import com.taskmanager.model.Users;
import com.taskmanager.repository.projects.ProjectsRepository;
import com.taskmanager.repository.tasks.TasksRepository;
import com.taskmanager.repository.users.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TasksServiceTest {
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ProjectsRepository projectsRepository;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private TaskMapper taskMapper;
    @InjectMocks
    private TasksService tasksService;
    private final Long CORRECT_ID = 1L;
    private final Long INVALID_ID = 999L;

    // ========== DTO ==========
    private TasksRequestDTO createRequest;
    private TasksRequestDTO updateRequest;
    private TasksSummaryDTO summaryDTO;
    private TasksSummaryDTO summary2;
    private TasksResponseDTO responseDTO;
    private TasksResponseDTO updateResponseDTO;

    // ========== ТЕСТОВЫЕ СУЩНОСТИ ==========
    private Users assignee;
    private Users creator;
    private Projects project;
    private Tasks task;
    private Tasks task2;
    private Tasks updatedTask;

    // Тестовые данные
    @BeforeEach
    void setUp(){

        assignee = new Users();
        assignee.setId(10L);
        assignee.setFirstName("Иван");
        assignee.setLastName("Петров");
        assignee.setEmail("ivan@test.com");

        creator = new Users();
        creator.setId(20L);
        creator.setFirstName("Анна");
        creator.setLastName("Смирнова");
        creator.setEmail("anna@test.com");

        // 2. Создаём проект с владельцем и участниками (для проверки бизнес-правила)
        project = new Projects();
        project.setId(100L);
        project.setName("Тестовый проект");
        project.setUsers(new ArrayList<>());
        project.getUsers().add(assignee); // assignee – участник проекта

        // 3. Создаём задачу
        task = new Tasks();
        task.setId(CORRECT_ID);
        task.setTitle("Реализовать авторизацию");
        task.setDescription("Описание задачи");
        task.setStatus("TODO");
        task.setPriority("HIGH");
        task.setDueDate(Date.valueOf(LocalDate.now().plusDays(7)));
        task.setAssignee(assignee);
        task.setCreatedBy(creator);
        task.setProject(project);
        task.setCreatedAt(Timestamp.from(Instant.now()));
        task.setUpdatedAt(Timestamp.from(Instant.now()));

        // Вторая задача для проверки с листом
        task2 = new Tasks();
        task2.setId(2L);
        task2.setTitle("Реализовать авторизацию");
        task2.setDescription("Описание задачи");
        task2.setStatus("TODO");
        task2.setPriority("HIGH");
        task2.setDueDate(Date.valueOf(LocalDate.now().plusDays(7)));
        task2.setAssignee(assignee);
        task2.setCreatedBy(creator);
        task2.setProject(project);
        task2.setCreatedAt(Timestamp.from(Instant.now()));
        task2.setUpdatedAt(Timestamp.from(Instant.now()));


        updatedTask = new Tasks();
        updatedTask.setId(CORRECT_ID);
        updatedTask.setTitle("Обновлённый заголовок");
        updatedTask.setDescription("Обновлённое описание");
        updatedTask.setStatus("IN_PROGRESS");
        updatedTask.setPriority("MEDIUM");
        updatedTask.setDueDate(Date.valueOf(LocalDate.now().plusDays(3)));
        updatedTask.setAssignee(assignee);
        updatedTask.setCreatedBy(creator);
        updatedTask.setProject(project);
        updatedTask.setCreatedAt(task.getCreatedAt());
        updatedTask.setUpdatedAt(Timestamp.from(Instant.now()));

        // 4. DTO для создания задачи
        createRequest = new TasksRequestDTO(
                "Реализовать авторизацию",
                "Описание задачи",
                "TODO",
                "HIGH",
                LocalDate.now().plusDays(7),
                assignee.getId(),
                project.getId(),
                creator.getId()
        );

        // 5. DTO для обновления задачи
        updateRequest = new TasksRequestDTO(
                "Обновлённый заголовок",
                "Обновлённое описание",
                "IN_PROGRESS",
                "MEDIUM",
                LocalDate.now().plusDays(3),
                assignee.getId(),
                project.getId(),
                creator.getId()
        );

        // 6. Summary DTO
        summaryDTO = new TasksSummaryDTO(
                CORRECT_ID,
                "Реализовать авторизацию",
                "Описание задачи",
                "TODO",
                "HIGH",
                assignee.getId(),
                assignee.getFirstName(),
                project.getName(),
                LocalDate.now().plusDays(7)
        );

        // 7. Response DTO (детальный)
        responseDTO = new TasksResponseDTO(
                CORRECT_ID,
                "Реализовать авторизацию",
                "Описание задачи",
                "TODO",
                "HIGH",
                LocalDate.now().plusDays(7),
                assignee.getId(),
                assignee.getFirstName(),
                assignee.getLastName(),
                creator.getId(),
                creator.getFirstName(),
                creator.getLastName(),
                project.getId(),
                project.getName(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );

        updateResponseDTO = new TasksResponseDTO(
                CORRECT_ID,
                "Обновлённый заголовок",
                "Обновлённое описание",
                "IN_PROGRESS",
                "MEDIUM",
                LocalDate.now().plusDays(3),
                assignee.getId(),
                assignee.getFirstName(),
                assignee.getLastName(),
                creator.getId(),
                creator.getFirstName(),
                creator.getLastName(),
                project.getId(),
                project.getName(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );


        summary2 = new TasksSummaryDTO(
                2L,
                "Реализовать авторизацию",
                "Описание задачи",
                "TODO",
                "HIGH",
                assignee.getId(),
                assignee.getFirstName(),
                project.getName(),
                LocalDate.now().plusDays(7)
        );


    }

    //==========================================================
    // ТЕСТ получение информации о всех задачах
    //==========================================================
    @Test
    @DisplayName("Успешное получение информации о задачах")
    void getAllTask_ReturnListTaskSummary(){
        List<Tasks> tasks = List.of(task, task2);
        when(tasksRepository.findAll()).thenReturn(tasks);
        when(taskMapper.toSummaryDTO(task)).thenReturn(summaryDTO);
        when(taskMapper.toSummaryDTO(task2)).thenReturn(summary2);
        List<TasksSummaryDTO> result = tasksService.getAllTask();
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(CORRECT_ID);
        assertThat(result.get(0).title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.get(0).priority()).isEqualTo("HIGH");
        assertThat(result.get(0).status()).isEqualTo("TODO");
        assertThat(result.get(0).projectName()).isEqualTo("Тестовый проект");
        assertThat(result.get(0).assigneeFirstName()).isEqualTo("Иван");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.get(1).priority()).isEqualTo("HIGH");
        assertThat(result.get(1).status()).isEqualTo("TODO");
        assertThat(result.get(1).projectName()).isEqualTo("Тестовый проект");
        assertThat(result.get(1).assigneeFirstName()).isEqualTo("Иван");
        verify(tasksRepository, times(1)).findAll();
        verify(taskMapper, times(1)).toSummaryDTO(task);
        verify(taskMapper, times(1)).toSummaryDTO(task2);
    }
    // =========================================================
    // ТЕСТ Получение задач по проекту - Успешный сценарий
    //==========================================================
    @Test
    @DisplayName("Успешное получение задач по проекту")
    void getAllTasksByProject_ReturnListTaskSummary(){
        List<Tasks> tasks = List.of(task, task2);
        when(projectsRepository.findById(100L)).thenReturn(Optional.of(project));
        when(tasksRepository.findAllByProjectId(100L)).thenReturn(tasks);
        when(taskMapper.toSummaryDTO(task)).thenReturn(summaryDTO);
        when(taskMapper.toSummaryDTO(task2)).thenReturn(summary2);
        List<TasksSummaryDTO> result = tasksService.getAllTaskByProject(100L);
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(CORRECT_ID);
        assertThat(result.get(0).title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.get(0).priority()).isEqualTo("HIGH");
        assertThat(result.get(0).status()).isEqualTo("TODO");
        assertThat(result.get(0).projectName()).isEqualTo("Тестовый проект");
        assertThat(result.get(0).assigneeFirstName()).isEqualTo("Иван");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.get(1).priority()).isEqualTo("HIGH");
        assertThat(result.get(1).status()).isEqualTo("TODO");
        assertThat(result.get(1).projectName()).isEqualTo("Тестовый проект");
        assertThat(result.get(1).assigneeFirstName()).isEqualTo("Иван");
        verify(projectsRepository, times(1)).findById(100L);
        verify(tasksRepository, times(1)).findAllByProjectId(100L);
        verify(taskMapper, times(1)).toSummaryDTO(task);
        verify(taskMapper,times(1)).toSummaryDTO(task2);
    }
    //==========================================================
    // Получение всех задач по проекту провальный сценарий, когда проекта не существует
    //==========================================================
    @Test
    @DisplayName("Проект с данным id не существует")
    void getAllTasksByProject_ReturnException(){
        when(projectsRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.getAllTaskByProject(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", 10L);
        verify(projectsRepository,times(1)).findById(10L);
        verify(tasksRepository, never()).findAllByProjectId(10L);
        verify(taskMapper, never()).toSummaryDTO(task);
        verify(taskMapper, never()).toSummaryDTO(task2);
    }
    // =========================================================
    // ТЕСТ получение задачи по исполнителю, покрываю 2 сценария 1) Когда все успешно 2) Когда исполнителя нет
    //==========================================================
    @Test
    @DisplayName("Успешное получение задач исполнителя")
    void getAllTaskByAssignee_ReturnSummaryDTO(){
        List<Tasks> tasks = List.of(task, task2);
        when(usersRepository.findById(10L)).thenReturn(Optional.of(assignee));
        when(tasksRepository.findAllByAssigneeId(10L)).thenReturn(tasks);
        when(taskMapper.toSummaryDTO(task)).thenReturn(summaryDTO);
        when(taskMapper.toSummaryDTO(task2)).thenReturn(summary2);
        List<TasksSummaryDTO> result = tasksService.getAllTaskByAssignee(10L);
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(CORRECT_ID);
        assertThat(result.get(0).assigneeFirstName()).isEqualTo("Иван");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).assigneeFirstName()).isEqualTo("Иван");
        verify(usersRepository, times(1)).findById(10L);
        verify(tasksRepository, times(1)).findAllByAssigneeId(10L);
        verify(taskMapper, times(1)).toSummaryDTO(task);
        verify(taskMapper, times(1)).toSummaryDTO(task2);
    }
    //==========================================================
    // Исполнитель не найден
    //==========================================================
    @Test
    @DisplayName("Исполнитель с данным id не найден")
    void getAllTaskByAssignee_ReturnNotFoundException(){
        when(usersRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.getAllTaskByAssignee(100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Исполнитель", 100L);
        verify(usersRepository, times(1)).findById(100L);
        verify(tasksRepository, never()).findAllByAssigneeId(100L);
        verify(taskMapper, never()).toSummaryDTO(task);
        verify(taskMapper, never()).toSummaryDTO(task2);
    }

    // ==============================================
    // ТЕСТ получение задачи по создателю 2 покрываю 2 сценария, 1) успешный 2) создатель задач не найден
    //==========================================================
    @Test
    @DisplayName("Успешное получение задач создателя")
    void getAllTaskByCreated_ReturnSummaryDTO(){
        List<Tasks> tasks = List.of(task, task2);
        when(usersRepository.findById(20L)).thenReturn(Optional.of(creator));
        when(tasksRepository.findAllByCreatedById(20L)).thenReturn(tasks);
        when(taskMapper.toSummaryDTO(task)).thenReturn(summaryDTO);
        when(taskMapper.toSummaryDTO(task2)).thenReturn(summary2);
        List<TasksSummaryDTO> result = tasksService.getAllTaskByCreated(20L);
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(CORRECT_ID);
        assertThat(result.get(0).title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.get(0).projectName()).isEqualTo("Тестовый проект");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.get(1).projectName()).isEqualTo("Тестовый проект");
        verify(usersRepository, times(1)).findById(20L);
        verify(tasksRepository, times(1)).findAllByCreatedById(20L);
        verify(taskMapper, times(1)).toSummaryDTO(task);
        verify(taskMapper, times(1)).toSummaryDTO(task2);
    }
    //==========================================================
    // Провальный сценарий, создателя не существует (Уточнение: Создатель так же является пользователем)
    //==========================================================
    @Test
    @DisplayName("Создателя не существует")
    void getAllTaskByCreated_ReturnNotFoundException(){
        when(usersRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.getAllTaskByCreated(100L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(usersRepository, times(1)).findById(100L);
        verify(tasksRepository, never()).findAllByCreatedById(100L);
        verify(taskMapper, never()).toSummaryDTO(task);
        verify(taskMapper, never()).toSummaryDTO(task2);
    }

    // ==============================================
    // TEST получение детальной информации о задачи по id
    //==========================================================
    @Test
    @DisplayName("Успешное получение детальной информации о задачи по id")
    void getTaskById_ReturnTasksResponseDTO(){
        when(tasksRepository.findById(CORRECT_ID)).thenReturn(Optional.of(task));
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);
        TasksResponseDTO result = tasksService.getTaskById(CORRECT_ID);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(CORRECT_ID);
        assertThat(result.priority()).isEqualTo("HIGH");
        assertThat(result.status()).isEqualTo("TODO");
        assertThat(result.assigneeLastName()).isEqualTo("Петров");
        assertThat(result.assigneeFirstName()).isEqualTo("Иван");
        assertThat(result.createdByLastName()).isEqualTo("Смирнова");
        verify(tasksRepository,times(1)).findById(CORRECT_ID);
        verify(taskMapper, times(1)).toResponseDTO(task);
    }
    //==========================================================
    // Задачи с таким id не существует
    //==========================================================
    @Test
    @DisplayName("Задачи с данным id не существует")
    void getTaskById_ReturnNotFoundException(){
        when(tasksRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.getTaskById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Задачи",INVALID_ID);
        verify(tasksRepository, times(1)).findById(INVALID_ID);
        verify(taskMapper, never()).toResponseDTO(any(Tasks.class));
    }
    // ==============================================
    // ТЕСТ получение информации о задачах с конкретным статусом
    //==========================================================
    @Test
    @DisplayName("Успешное получение задач по статусу")
    void getAllTasksByStatus_ReturnSummaryDTO(){
        List<Tasks> tasks = List.of(task, task2);
        when(tasksRepository.findAllByStatus("TODO")).thenReturn(tasks);
        when(taskMapper.toSummaryDTO(task)).thenReturn(summaryDTO);
        when(taskMapper.toSummaryDTO(task2)).thenReturn(summary2);
        List<TasksSummaryDTO> result = tasksService.getAllTaskByStatus("TODO");
        assertThat(result).isNotNull();
        assertThat(result.get(0).status()).isEqualTo("TODO");
        assertThat(result.get(1).status()).isEqualTo("TODO");
        verify(tasksRepository,times(1)).findAllByStatus("TODO");
        verify(taskMapper, times(1)).toSummaryDTO(task);
        verify(taskMapper, times(1)).toSummaryDTO(task2);
    }
    // ==============================================
    // Получение информации о задачах с конкретным приоритетом
    //==========================================================
    @Test
    @DisplayName("Успешное получение информации о задачах по приоритету")
    void getAllTaskByPriority_ReturnSummaryDTO(){
        List<Tasks> tasks = List.of(task, task2);
        when(tasksRepository.findAllByPriority("HIGH")).thenReturn(tasks);
        when(taskMapper.toSummaryDTO(task)).thenReturn(summaryDTO);
        when(taskMapper.toSummaryDTO(task2)).thenReturn(summary2);
        List<TasksSummaryDTO> result = tasksService.getAllTaskByPriority("HIGH");
        // Проверка для первого task
        assertThat(result.get(0).priority()).isEqualTo("HIGH");
        assertThat(result.get(1).priority()).isEqualTo("HIGH");
        verify(tasksRepository, times(1)).findAllByPriority("HIGH");
        verify(taskMapper, times(1)).toSummaryDTO(task);
        verify(taskMapper, times(1)).toSummaryDTO(task2);
    }
    // ==============================================
    // ТЕСТ Обновление задачи, 2 сценария 1) Успешный когда задача есть в базе 2) Провальный когда задачи нету в базе
    // ==============================================
    @Test
    @DisplayName("Успешное обновление задачи")
    void updateTaskByID_ReturnTasksResponseDTO(){
        when(tasksRepository.findById(CORRECT_ID)).thenReturn(Optional.of(task));
        when(tasksRepository.save(any(Tasks.class))).thenReturn(updatedTask);
        when(taskMapper.toResponseDTO(any(Tasks.class))).thenReturn(updateResponseDTO);
        TasksResponseDTO result = tasksService.updateTaskById(CORRECT_ID, updateRequest);
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("IN_PROGRESS");
        assertThat(result.priority()).isEqualTo("MEDIUM");
        assertThat(result.title()).isEqualTo("Обновлённый заголовок");
        verify(tasksRepository, times(1)).findById(CORRECT_ID);
        verify(taskMapper,times(1)).updateEntity(task, updateRequest);
        verify(tasksRepository,times(1)).save(task);
        verify(taskMapper,times(1)).toResponseDTO(updatedTask);
    }
    //==========================================================
    // Случай когда задачу не получилось найти в базе
    //==========================================================
    @Test
    @DisplayName("Не существующие id задачи")
    void updateTaskById_ReturnNotFoundException(){
        when(tasksRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.updateTaskById(INVALID_ID, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Задача", INVALID_ID);
        verify(tasksRepository, times(1)).findById(INVALID_ID);
        verify(taskMapper, never()).updateEntity(task, updateRequest);
        verify(tasksRepository,never()).save(task);
        verify(taskMapper,never()).toResponseDTO(updatedTask);
    }

    // ==============================================
    // ТЕСТ Создание задачи 5 сценариев 1) Успешный когда удалось создать задачу
    // ==============================================
    @Test
    @DisplayName("Успешный сценарий создания задачи")
    void createTask_ReturnTaskResponseDTO(){
        when(projectsRepository.findById(100L)).thenReturn(Optional.of(project));
        when(usersRepository.findById(10L)).thenReturn(Optional.of(assignee));
        when(usersRepository.findById(20L)).thenReturn(Optional.of(creator));
        when(taskMapper.toEntity(createRequest)).thenReturn(task);
        when(tasksRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);
        TasksResponseDTO result = tasksService.createTask(createRequest);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(CORRECT_ID);
        assertThat(result.title()).isEqualTo("Реализовать авторизацию");
        assertThat(result.assigneeId()).isEqualTo(10L);
        assertThat(result.createdById()).isEqualTo(20L);
        assertThat(result.projectId()).isEqualTo(100L);
        verify(projectsRepository, times(1)).findById(100L);
        verify(usersRepository, times(1)).findById(10L);
        verify(usersRepository, times(1)).findById(20L);
        verify(taskMapper, times(1)).toEntity(createRequest);
        verify(tasksRepository, times(1)).save(task);
        verify(taskMapper, times(1)).toResponseDTO(task);
    }
    //==========================================================
    // Проекта с данным id не существует
    //==========================================================
    @Test
    @DisplayName("Проект с id не существует")
    void createTask_Project_ReturnNotFoundException(){
        when(projectsRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.createTask(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", 100L);
        verify(projectsRepository, times(1)).findById(100L);
        verify(usersRepository, never()).findById(10L);
        verify(usersRepository, never()).findById(20L);
        verify(taskMapper, never()).toEntity(createRequest);
        verify(tasksRepository, never()).save(task);
        verify(taskMapper, never()).toResponseDTO(task);
    }
    //==========================================================
    // Данного исполнителя не существует
    //==========================================================
    @Test
    @DisplayName("Исполнителя с данным id не существует")
    void createTask_Assignee_ReturnNotFoundException(){
        when(projectsRepository.findById(100L)).thenReturn(Optional.of(project));
        when(usersRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.createTask(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Исполнитель", 10L);
        verify(projectsRepository, times(1)).findById(100L);
        verify(usersRepository, times(1)).findById(10L);
        verify(taskMapper, never()).toEntity(createRequest);
        verify(usersRepository, never()).findById(20L);
        verify(tasksRepository, never()).save(task);
        verify(taskMapper, never()).toResponseDTO(task);
    }
    //==========================================================
    // Создателя с данным id не существует
    //==========================================================
    @Test
    @DisplayName("Создателя с данным id не существует")
    void createTask_Creator_ReturnNotFoundException(){
        when(projectsRepository.findById(100L)).thenReturn(Optional.of(project));
        when(usersRepository.findById(10L)).thenReturn(Optional.of(assignee));
        when(usersRepository.findById(20L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.createTask(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Создателя", 20L);
        verify(projectsRepository, times(1)).findById(100L);
        verify(usersRepository, times(1)).findById(10L);
        verify(usersRepository, times(1)).findById(20L);
        verify(taskMapper, never()).toEntity(createRequest);
        verify(tasksRepository, never()).save(task);
        verify(taskMapper, never()).toResponseDTO(task);
    }
    //==========================================================
    // Исполнитель не является участником данного проекта
    //==========================================================
    @Test
    @DisplayName("Исполнитель не является участником проекта")
    void createTask_AssigneeIsNotParticipants_ReturnBadRequest(){
        project.getUsers().clear();
        when(projectsRepository.findById(100L)).thenReturn(Optional.of(project));
        when(usersRepository.findById(10L)).thenReturn(Optional.of(assignee));
        when(usersRepository.findById(20L)).thenReturn(Optional.of(creator));
        assertThatThrownBy(()->tasksService.createTask(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Исполнитель должен быть участником проекта");
        verify(projectsRepository, times(1)).findById(100L);
        verify(usersRepository, times(1)).findById(10L);
        verify(usersRepository, times(1)).findById(20L);
        verify(taskMapper, never()).toEntity(createRequest);
        verify(tasksRepository, never()).save(task);
        verify(taskMapper, never()).toResponseDTO(task);

    }
    // ==============================================
    // ======== ТЕСТ УДАЛЕНИЕ ЗАДАЧИ ПО ID ========
    // Успешный сценарий удаление задачи по ID
    //==========================================================
    @Test
    @DisplayName("Успешное удаление задачи по id")
    void deleteTaskById_ReturnVoid(){
        when(tasksRepository.findById(CORRECT_ID)).thenReturn(Optional.of(task));
        tasksService.deleteTaskById(CORRECT_ID);
        verify(tasksRepository, times(1)).findById(CORRECT_ID);
        verify(tasksRepository, times(1)).deleteById(CORRECT_ID);
    }
    //==========================================================
    // Провальный сценарий удаление задачи по id ее не существует
    //==========================================================
    @Test
    @DisplayName("Задачи с данным id не существует")
    void deleteTaskById_Exception(){
        when(tasksRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->tasksService.deleteTaskById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Задача с id 999 не найден");
        verify(tasksRepository, times(1)).findById(INVALID_ID);
        verify(tasksRepository, never()).deleteById(INVALID_ID);
    }
    // ==============================================

}
