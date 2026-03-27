package com.taskmanager.service.projects;

import com.taskmanager.dto.request.projects.ProjectRequestDTO;
import com.taskmanager.dto.response.projects.ProjectResponseDTO;
import com.taskmanager.dto.response.projects.ProjectResponseParticipantDTO;
import com.taskmanager.dto.response.projects.ProjectResponseSummaryDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.mapper.ProjectMapper;
import com.taskmanager.model.Projects;
import com.taskmanager.model.Users;
import com.taskmanager.repository.projects.ProjectsRepository;
import com.taskmanager.repository.users.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectsRepository projectsRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    // Тестовые данные
    private final Long CORRECT_ID = 1L;
    private final Long INVALID_ID = 999L;

    // ========== DTO ==========
    private ProjectRequestDTO requestDTO;
    private ProjectRequestDTO requestInvalid;
    private ProjectRequestDTO updateRequest;
    private ProjectResponseDTO responseProject1;
    private ProjectResponseDTO updatedResponse;
    private ProjectResponseDTO responseProject4;
    private ProjectResponseSummaryDTO summaryProject1;
    private ProjectResponseSummaryDTO summaryProject2;
    private ProjectResponseSummaryDTO summaryProject3;
    // ========== ТЕСТОВЫЕ СУЩНОСТИ ==========
    private Projects project1;
    private Projects project2;
    private Projects project3;
    private Projects project4;
    private Projects updatedProject;
    private Users user1;
    private Users user2;


    // Тестовые данные
    @BeforeEach
    void setUp(){


        user1 = new Users();
        user1.setId(1L);
        user1.setEmail("test@test.com");
        user1.setPassword("encodePassword");
        user1.setFirstName("Иван");
        user1.setLastName("Чухманов");
        user1.setRole("USER");
        user1.setCreatedAt(Timestamp.from(Instant.now()));
        user1.setUpdatedAt(Timestamp.from(Instant.now()));


        user2 = new Users();
        user2.setId(2L);
        user2.setEmail("ivan@test.com");
        user2.setPassword("encodePassword123");
        user2.setFirstName("Олег");
        user2.setLastName("Филипов");
        user2.setRole("USER");
        user2.setCreatedAt(Timestamp.from(Instant.now()));
        user2.setUpdatedAt(Timestamp.from(Instant.now()));

        List<Users> users = List.of(user1, user2);

        project1 = new Projects();
        project1.setId(1L);
        project1.setName("Название проекта");
        project1.setDescription("Описание проекта");
        project1.setStatus("ACTIVE");
        project1.setOwner(user1);
        project1.setCreatedAt(Timestamp.from(Instant.now()));
        project1.setUpdatedAt(Timestamp.from(Instant.now()));
        project1.setUsers(users);

        project2 = new Projects();
        project2.setId(2L);
        project2.setName("Название проекта test 2");
        project2.setDescription("Описание проекта test 2");
        project2.setStatus("ACTIVE");
        project2.setOwner(user2);
        project2.setCreatedAt(Timestamp.from(Instant.now()));
        project2.setUpdatedAt(Timestamp.from(Instant.now()));
        project2.setUsers(users);

        project3 = new Projects();
        project3.setId(3L);
        project3.setName("Название проекта test 3");
        project3.setDescription("Описание проекта test 3");
        project3.setStatus("COMPLETED");
        project3.setOwner(user2);
        project3.setCreatedAt(Timestamp.from(Instant.now()));
        project3.setUpdatedAt(Timestamp.from(Instant.now()));
        project3.setUsers(users);

        project4 = new Projects();
        project4.setId(4L);
        project4.setName("Название проекта test 4");
        project4.setDescription("Описание проекта test 4");
        project4.setStatus("COMPLETED");
        project4.setOwner(user2);
        project4.setCreatedAt(Timestamp.from(Instant.now()));
        project4.setUpdatedAt(Timestamp.from(Instant.now()));
        project4.setUsers(new ArrayList<>());



        updatedProject = new Projects();
        updatedProject.setId(CORRECT_ID);
        updatedProject.setName("Обновленное название");
        updatedProject.setDescription("Обновленное описание");
        updatedProject.setStatus("COMPLETED");
        updatedProject.setOwner(user2);  // если ownerId изменился
        updatedProject.setCreatedAt(Timestamp.from(Instant.now()));
        updatedProject.setUpdatedAt(Timestamp.from(Instant.now()));
        updatedProject.setUsers(project1.getUsers());



        requestDTO = new ProjectRequestDTO(
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                1L
        );

        requestInvalid = new ProjectRequestDTO(
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                 INVALID_ID
        );
        updateRequest = new ProjectRequestDTO(
                "Обновленное название",
                "Обновленное описание",
                "COMPLETED",
                2L   // возможно, меняем владельца
        );

        updatedResponse = new ProjectResponseDTO(
                1L,
                "Обновленное название",
                "Обновленное описание",
                "COMPLETED",
                2L,
                "Олег",
                "Филипов",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        // Детальный ответ пользователю
        responseProject1 = new ProjectResponseDTO(
                1L,
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                1L,
                "Иван",
                "Чухманов",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        responseProject4 = new ProjectResponseDTO(
                4L,
                "Название проекта test 4",
                "Описание проекта test 4",
                "COMPLETED",
                2L,
                "Олег",
                "Филипов",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        // Частичный ответ пользователю
        summaryProject1 = new ProjectResponseSummaryDTO(
                1L,
                "Название проекта",
                "Описание проекта",
                "ACTIVE",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        summaryProject2 = new ProjectResponseSummaryDTO(
                2L,
                "Название проекта test 2",
                "Описание проекта test 2",
                "ACTIVE",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        summaryProject3 = new ProjectResponseSummaryDTO(
                3L,
                "Название проекта test 3",
                "Описание проекта test 3",
                "COMPLETED",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

    }
    //==========================================================
    // СОЗДАНИЕ ПРОЕКТА С УКАЗАНИЕМ ВЛАДЕЛЬЦА
    // Успешный сценарий
    //==========================================================
    @Test
    @DisplayName("Успешный сценарий создания проекта")
    void createProject_ReturnProjectResponseDTO(){
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(user1));
        when(projectMapper.toEntity(requestDTO)).thenReturn(project1);
        when(projectsRepository.save(project1)).thenReturn(project1);
        when(projectMapper.toProjectResponseDto(project1)).thenReturn(responseProject1);
        ProjectResponseDTO result = projectService.createProject(requestDTO);
        assertThat(result).isNotNull();
        assertThat(result.ownerFirstName()).isEqualTo("Иван");
        assertThat(result.ownerLastName()).isEqualTo("Чухманов");
        assertThat(result.ownerId()).isEqualTo(1L);
        verify(usersRepository, times(1)).findById(CORRECT_ID);
        verify(projectMapper, times(1)).toEntity(requestDTO);
        verify(projectsRepository, times(1)).save(project1);
        verify(projectMapper, times(1)).toProjectResponseDto(project1);
    }
    //==========================================================
    // Провальный сценарий создания проекта, когда пользователя не существует => владельца проекта не будет
    //==========================================================
    @Test
    @DisplayName("Пользователя с данным id не существует")
    void createProject_ReturnNotFoundException(){
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> projectService.createProject(requestInvalid))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(usersRepository, times(1)).findById(INVALID_ID);
        verify(projectMapper, never()).toEntity(any(ProjectRequestDTO.class));
        verify(projectsRepository, never()).save(any(Projects.class));
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }
    //==========================================================
    // ПОЛУЧЕНИЕ ИНФОРМАЦИИ О ПРОЕКТЕ ПО ID
    //==========================================================
    // Получение информации о проекте по id - успешный сценарий
    @Test
    @DisplayName("Успешный сценарий получения проекта по id")
    void getProjectById_ReturnProjectResponseDTO(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(projectMapper.toProjectResponseDto(project1)).thenReturn(responseProject1);
        ProjectResponseDTO result = projectService.getProjectById(CORRECT_ID);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.ownerId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.ownerFirstName()).isEqualTo("Иван");
        assertThat(result.ownerLastName()).isEqualTo("Чухманов");
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(projectMapper, times(1)).toProjectResponseDto(project1);
    }
    //==========================================================
    // Получение информации о проекте по не существующему id
    //==========================================================
    @Test
    @DisplayName("Проекта с данным id не существует")
    void getProjectById_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> projectService.getProjectById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository, times(1)).findById(INVALID_ID);
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }
    //==========================================================
    // Получение списка всех проектов
    //==========================================================
    @Test
    @DisplayName("Успешное получение данных всех проектов")
    void getAllProjects_ReturnListSummaryDTO(){
        when(projectsRepository.findAll()).thenReturn(List.of(project1,project2));
        when(projectMapper.toProjectResponseSummaryDto(project1)).thenReturn(summaryProject1);
        when(projectMapper.toProjectResponseSummaryDto(project2)).thenReturn(summaryProject2);
        List<ProjectResponseSummaryDTO> result = projectService.getAllProjects();
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Название проекта");
        assertThat(result.get(0).description()).isEqualTo("Описание проекта");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Название проекта test 2");
        assertThat(result.get(1).description()).isEqualTo("Описание проекта test 2");
        assertThat(result.get(1).status()).isEqualTo("ACTIVE");
        verify(projectsRepository, times(1)).findAll();
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project1);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project2);
    }
    //==========================================================
    // ПОЛУЧЕНИЕ СПИСКА ПРОЕКТОВ ПО СТАТУСУ
    //==========================================================
    @Test
    @DisplayName("Успешное получение данных всех проектов по статусу")
    void getProjectByStatus_ReturnListSummaryDTO(){
        when(projectsRepository.findByStatus("ACTIVE")).thenReturn(List.of(project1, project2));
        when(projectMapper.toProjectResponseSummaryDto(project1)).thenReturn(summaryProject1);
        when(projectMapper.toProjectResponseSummaryDto(project2)).thenReturn(summaryProject2);
        List<ProjectResponseSummaryDTO> result = projectService.getProjectByStatus("ACTIVE");
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Название проекта");
        assertThat(result.get(0).description()).isEqualTo("Описание проекта");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Название проекта test 2");
        assertThat(result.get(1).description()).isEqualTo("Описание проекта test 2");
        assertThat(result.get(1).status()).isEqualTo("ACTIVE");
        verify(projectsRepository, times(1)).findByStatus("ACTIVE");
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project1);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project2);
    }
    //==========================================================
    // ПОЛУЧЕНИЕ ПРОЕКТОВ ВЛАДЕЛЬЦА
    //==========================================================
    // 1) Успешный сценарий
    @Test
    @DisplayName("Успешное получение проектов владельца")
    void getProjectByOwnerId_ReturnListProjectSummaryDTO(){
        when(usersRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(projectsRepository.findByOwnerId(2L)).thenReturn(List.of(project2, project3));
        when(projectMapper.toProjectResponseSummaryDto(project2)).thenReturn(summaryProject2);
        when(projectMapper.toProjectResponseSummaryDto(project3)).thenReturn(summaryProject3);
        List<ProjectResponseSummaryDTO> result = projectService.getProjectByOwnerId(2L);
        assertThat(result).isNotNull();
        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(0).name()).isEqualTo("Название проекта test 2");
        assertThat(result.get(0).description()).isEqualTo("Описание проекта test 2");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        assertThat(result.get(1).id()).isEqualTo(3L);
        assertThat(result.get(1).name()).isEqualTo("Название проекта test 3");
        assertThat(result.get(1).description()).isEqualTo("Описание проекта test 3");
        assertThat(result.get(1).status()).isEqualTo("COMPLETED");
        verify(usersRepository, times(1)).findById(2L);
        verify(projectsRepository, times(1)).findByOwnerId(2L);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project2);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project3);
    }
    // 2) Пользователя с id не существует
    @Test
    @DisplayName("Получение проектов, у несуществующего владельца")
    void getProjectByOwnerId_ReturnNotFoundException(){
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.getProjectByOwnerId(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(usersRepository, times(1)).findById(INVALID_ID);
        verify(projectsRepository, never()).findByOwnerId(INVALID_ID);
        verify(projectMapper, never()).toProjectResponseSummaryDto(any(Projects.class));
    }
    //==========================================================
    // ПОЛУЧЕНИЕ ПРОЕКТОВ ГДЕ ПОЛЬЗОВАТЕЛЬ УЧАСТНИК
    //==========================================================
    // 1) Успешный сценарий
    @Test
    @DisplayName("Успешное получение проектов, где пользователь участник")
    void getProjectsByParticipant_ReturnListSummaryDTO(){
        List<Projects> projects = List.of(project1, project2, project3);
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(user1));
        when(projectsRepository.findProjectsByParticipantWithRole(1L)).thenReturn(projects);
        when(projectMapper.toProjectResponseSummaryDto(project1)).thenReturn(summaryProject1);
        when(projectMapper.toProjectResponseSummaryDto(project2)).thenReturn(summaryProject2);
        when(projectMapper.toProjectResponseSummaryDto(project3)).thenReturn(summaryProject3);
        List<ProjectResponseSummaryDTO> result = projectService.getProjectsByParticipant(1L);
        assertThat(result).isNotNull();
        assertThat(result.get(0).name()).isEqualTo("Название проекта");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        assertThat(result.get(1).name()).isEqualTo("Название проекта test 2");
        assertThat(result.get(1).status()).isEqualTo("ACTIVE");
        assertThat(result.get(2).name()).isEqualTo("Название проекта test 3");
        assertThat(result.get(2).status()).isEqualTo("COMPLETED");
        verify(usersRepository, times(1)).findById(CORRECT_ID);
        verify(projectsRepository, times(1)).findProjectsByParticipantWithRole(1L);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project1);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project2);
        verify(projectMapper, times(1)).toProjectResponseSummaryDto(project2);
    }
    //==========================================================
    // 2) Провальный сценарий пользователя не существует
    //==========================================================
    // (Если пользователь в целом есть у данного проекта и если он создатель, он все равно является участником проекта)
    @Test
    @DisplayName("Пользователя не существует в базе")
    void getProjectByParticipant_ReturnNotFoundException(){
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.getProjectsByParticipant(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(usersRepository, times(1)).findById(INVALID_ID);
        verify(projectsRepository, never()).findProjectsByParticipantWithRole(1L);
        verify(projectMapper, never()).toProjectResponseSummaryDto(any(Projects.class));

    }
    //==========================================================
    // ОБНОВЛЕНИЕ ПРОЕКТА
    //==========================================================
    // 1) Успешный сценарий
    @Test
    @DisplayName("Успешный сценарий обновления проекта")
    void updateProject_ReturnProjectResponseDTO(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(updateRequest.ownerId())).thenReturn(Optional.of(user2));
        when(projectsRepository.save(project1)).thenReturn(updatedProject);
        when(projectMapper.toProjectResponseDto(updatedProject)).thenReturn(updatedResponse);
        ProjectResponseDTO result = projectService.updateProject(CORRECT_ID, updateRequest);
        // Добавить проверки
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(CORRECT_ID);
        assertThat(result.name()).isEqualTo("Обновленное название");
        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.ownerFirstName()).isEqualTo("Олег");
        assertThat(result.ownerLastName()).isEqualTo("Филипов");
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(updateRequest.ownerId());
        verify(projectsRepository, times(1)).save(project1);
        verify(projectMapper, times(1)).toProjectResponseDto(updatedProject);

    }
    //==========================================================
    // 2) Провальный сценарий проекта не существует
    //==========================================================
    @Test
    @DisplayName("Обновление не существующего проекта")
    void updateProject_ProjectNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.updateProject(INVALID_ID, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository, times(1)).findById(INVALID_ID);
        verify(projectMapper, never()).updateEntity(any(Projects.class),eq(requestDTO));
        verify(usersRepository, never()).findById(anyLong());
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }
    // 3) Провальный сценарий, пытаемся установить не существующего владельца
    @Test
    @DisplayName("Обновление не существующий владелец")
    void updateProject_UserNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(updateRequest.ownerId())).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.updateProject(CORRECT_ID, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Владелец", updatedProject.getOwner());
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(updateRequest.ownerId());
        verify(projectMapper, never()).updateEntity(any(Projects.class),eq(requestDTO));
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }
    //==========================================================
    // УДАЛЕНИЕ ПРОЕКТА ПО ID
    //==========================================================
    // 1) Успешное удаление проекта
    @Test
    @DisplayName("Успешное удаление проекта по id")
    void deleteProject_ReturnVoid(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        projectService.deleteProjectById(CORRECT_ID);
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
    }
    // 2) Проекта с данным id не существует
    @Test
    @DisplayName("Проекта с данным id не существует")
    void deleteProject_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> projectService.deleteProjectById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository, times(1)).findById(INVALID_ID);
        verify(projectsRepository, never()).delete(any(Projects.class));
    }
    //==========================================================
    // ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ В ПРОЕКТ
    //==========================================================
    // 1) Успешный сценарий
    @Test
    @DisplayName("Успешное добавление участника в проект")
    void addUserToProject_ReturnProjectResponseDTO(){
        when(projectsRepository.findById(4L)).thenReturn(Optional.of(project4));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(projectsRepository.save(project4)).thenReturn(project4);
        when(projectMapper.toProjectResponseDto(project4)).thenReturn(responseProject4);
        ProjectResponseDTO result = projectService.addUserToProject(4L, 1L);
        assertThat(result).isNotNull();
        verify(projectsRepository,times(1)).findById(4L);
        verify(usersRepository, times(1)).findById(1L);
        verify(projectsRepository, times(1)).save(project4);
        verify(projectMapper, times(1)).toProjectResponseDto(project4);
    }

    // 2) Проект не найден
    @Test
    @DisplayName("Проект с данным id не найден")
    void addUserToProject_ProjectNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.addUserToProject(INVALID_ID, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository,times(1)).findById(INVALID_ID);
        verify(usersRepository, never()).findById(anyLong());
        verify(projectsRepository, never()).save(any(Projects.class));
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }
    // 3) Пользователь не найден
    @Test
    @DisplayName("Пользователь с данным id не найден")
    void addUserToProject_UserNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.addUserToProject(CORRECT_ID, INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(projectsRepository,times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(INVALID_ID);
        verify(projectsRepository, never()).save(any(Projects.class));
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));

    }
    // 4) Пользователь является создателем проекта
    @Test
    @DisplayName("Попытка добавить пользователя, который является владельцем проекта")
    void addUserToProject_UserIsCreatorProject_ReturnBadRequest(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user1));
        assertThatThrownBy(()->projectService.addUserToProject(CORRECT_ID, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Пользователь является владельцем проекта");
        verify(projectsRepository,times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(1L);
        verify(projectsRepository, never()).save(any(Projects.class));
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }

    // 5) Пользователь уже является участником проекта
    @Test
    @DisplayName("Попытка добавить дубликат пользователя в проект")
    void addUserToProject_UserIsDuplicate_ReturnBadRequest(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(user2));
        assertThatThrownBy(()->projectService.addUserToProject(CORRECT_ID, 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Попытка добавить дубликат пользователя");
        verify(projectsRepository,times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(2L);
        verify(projectsRepository, never()).save(any(Projects.class));
        verify(projectMapper, never()).toProjectResponseDto(any(Projects.class));
    }
    //==========================================================
    // УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ИЗ ПРОЕКТА
    //==========================================================
    // 1) Успешный сценарий
    @Test
    @DisplayName("Успешное удаления пользователя из проекта")
    void deleteUserFromProject_ReturnVoid(){
        // ==========================================================================
        // Отдельные данные для конкретного теста на удаления пользователя из проекта
        // ==========================================================================
        Projects testProject = new Projects();
        testProject.setId(1L);
        testProject.setName("Название проекта");
        testProject.setDescription("Описание проекта");
        testProject.setStatus("ACTIVE");
        testProject.setOwner(user1);
        testProject.setCreatedAt(Timestamp.from(Instant.now()));
        testProject.setUpdatedAt(Timestamp.from(Instant.now()));
        List<Users> participants = new ArrayList<>();
        participants.add(user1);
        participants.add(user2);
        testProject.setUsers(participants);
        // ==========================================================================

        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testProject));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(projectsRepository.save(testProject)).thenReturn(testProject);
        projectService.deleteUserFromProject(CORRECT_ID, 2L);
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(2L);
        verify(projectsRepository, times(1)).save(testProject);
    }

    // 2) Проект не найден
    @Test
    @DisplayName("Проект с данным id не существует")
    void deleteUserFromProject_ProjectNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.deleteUserFromProject(INVALID_ID, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository, times(1)).findById(INVALID_ID);
        verify(usersRepository, never()).findById(2L);
        verify(projectsRepository, never()).save(any(Projects.class));
    }

    // 3) Пользователь не найден
    @Test
    @DisplayName("Пользователь с данным id не существует")
    void deleteUserFromProject_UserNotFoundException(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.deleteUserFromProject(CORRECT_ID, INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(INVALID_ID);
        verify(projectsRepository, never()).save(any(Projects.class));
    }

    // 4) Попытка удаления владельца проекта
    @Test
    @DisplayName("Пользователь является владельцем проекта")
    void deleteUserFromProject_UserIsCreator_ReturnBadRequest(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(user1));
        assertThatThrownBy(()->projectService.deleteUserFromProject(CORRECT_ID, CORRECT_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Попытка удаления владельца проекта");
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).findById(CORRECT_ID);
        verify(projectsRepository, never()).save(any(Projects.class));
    }
    //==========================================================
    // ПРОВЕРКА ЯВЛЯЕТСЯ ЛИ ПОЛЬЗОВАТЕЛЬ УЧАСТНИКОМ ПРОЕКТА
    //==========================================================
    // 1) Успешный сценарий
    @Test
    @DisplayName("Успешная проверка является ли пользователь участником проекта")
    void isUserIsProject_ReturnBoolean(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(user2));
        Boolean result = projectService.isUserInProject(CORRECT_ID, 2L);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(true);
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository,times(1)).findById(2L);
    }
    // 2) Проект с таким id не существует
    @Test
    @DisplayName("Проект с таким id не существует")
    void isUserIsProject_ProjectNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.isUserInProject(INVALID_ID, CORRECT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository, times(1)).findById(INVALID_ID);
        verify(usersRepository,never()).findById(anyLong());
    }
    // 3) Пользователь с таким id не существует
    @Test
    @DisplayName("Пользователь с таким id не существует")
    void isUserIsProject_UserNotFound_ReturnNotFoundException(){
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->projectService.isUserInProject(CORRECT_ID, INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository,times(1)).findById(INVALID_ID);
    }
    //==========================================================
    // ПОЛУЧЕНИЕ СПИСКА УЧАСТИНОКОВ ПРОЕКТА
    //==========================================================
    // 1) Успешный сценарий получения списка участников проекта
    @Test
    @DisplayName("Успешное получение списка участников проекта")
    void getProjectParticipants_ReturnListSummaryDTO(){
        // Конкретные тестовые данные для проверки данного метода, использую отдельно
        ProjectResponseParticipantDTO participantDto1 = new ProjectResponseParticipantDTO(
                user1.getId(), user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getRole()
        );
        ProjectResponseParticipantDTO participantDto2 = new ProjectResponseParticipantDTO(
                user2.getId(), user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getRole()
        );
        when(projectsRepository.findById(CORRECT_ID)).thenReturn(Optional.of(project1));
        when(projectMapper.toParticipantDto(user1)).thenReturn(participantDto1);
        when(projectMapper.toParticipantDto(user2)).thenReturn(participantDto2);

        List<ProjectResponseParticipantDTO> result = projectService.getProjectParticipants(CORRECT_ID);
        assertThat(result).isNotNull();
        assertThat(result.get(0).firstName()).isEqualTo("Иван");
        assertThat(result.get(0).lastName()).isEqualTo("Чухманов");
        assertThat(result.get(1).firstName()).isEqualTo("Олег");
        assertThat(result.get(1).lastName()).isEqualTo("Филипов");
        verify(projectsRepository, times(1)).findById(CORRECT_ID);
        verify(projectMapper, times(1)).toParticipantDto(user1);
        verify(projectMapper, times(1)).toParticipantDto(user2);

    }
    // 2) Проекта с данным id не существует
    @Test
    @DisplayName("Проекта с данным id не существует")
    void getProjectParticipants_ReturnNotFoundException(){
        when(projectsRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> projectService.getProjectParticipants(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Проект", INVALID_ID);
        verify(projectsRepository, times(1)).findById(INVALID_ID);
    }
    //==========================================================
    // ПОЛУЧЕНИЕ СПИСКА ПРОЕКТОВ ПОЛЬЗОВАТЕЛЯ
    //==========================================================
    // 1) Успешный сценарий получения списка проектов пользователя
    @Test
    @DisplayName("Успешное получение списка проектов пользователя")
    void getUserProjects_ReturnListSummaryDTO(){
        // Подготовка: пользователь имеет 2 проекта как владелец и 2 проекта как участник
        // Подготовка: пользователь имеет 2 проекта как владелец и 1 проект как участник
        user1.setProjects(List.of(project1, project2));
        user1.setParticipatedProjects(List.of(project3));
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(user1));
        when(projectMapper.toProjectResponseSummaryDto(project1)).thenReturn(summaryProject1);
        when(projectMapper.toProjectResponseSummaryDto(project2)).thenReturn(summaryProject2);
        when(projectMapper.toProjectResponseSummaryDto(project3)).thenReturn(summaryProject3);
        List<ProjectResponseSummaryDTO> result = projectService.getUserProjects(CORRECT_ID);
        assertThat(result).isNotNull();
        verify(usersRepository).findById(CORRECT_ID);
        verify(projectMapper).toProjectResponseSummaryDto(project1);
        verify(projectMapper).toProjectResponseSummaryDto(project2);
        verify(projectMapper).toProjectResponseSummaryDto(project3);
    }
    // 2) Пользователя с данным id не существует
    @Test
    @DisplayName("Пользователь с данным id не существует")
    void getUserProjects_ReturnNotFoundException(){
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> projectService.getUserProjects(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь", INVALID_ID);
        verify(usersRepository, times(1)).findById(INVALID_ID);
        verify(projectMapper, never()).toProjectResponseSummaryDto(project1);
        verify(projectMapper, never()).toProjectResponseSummaryDto(project2);
    }


}
