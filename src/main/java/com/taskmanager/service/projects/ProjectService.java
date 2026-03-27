package com.taskmanager.service.projects;
import com.taskmanager.dto.request.projects.ProjectRequestDTO;
import com.taskmanager.dto.response.projects.ProjectResponseDTO;
import com.taskmanager.dto.response.projects.ProjectResponseParticipantDTO;
import com.taskmanager.dto.response.projects.ProjectResponseSummaryDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.domain.BadStatusProjectException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.mapper.ProjectMapper;
import com.taskmanager.model.Projects;
import com.taskmanager.model.Users;
import com.taskmanager.repository.projects.ProjectsRepository;
import com.taskmanager.repository.users.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectsRepository projectsRepository;
    private final UsersRepository usersRepository;
    private final ProjectMapper projectMapper;
    public ProjectService(ProjectsRepository projectsRepository, UsersRepository usersRepository, ProjectMapper projectMapper) {
        this.projectsRepository = projectsRepository;
        this.usersRepository = usersRepository;
        this.projectMapper = projectMapper;
    }
    // Основные CRUD операции
    // todo 1: Создание проекта с указанным владельцем // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO request){
        log.info("Создание нового проекта с конкретным владельцем {}", request.ownerId());
        // Проверка существует ли вообще данный пользователь
        usersRepository.findById(request.ownerId()).orElseThrow(
                ()->{
                    log.error("Пользователя с id {} не существует", request.ownerId());
                    return new ResourceNotFoundException("Пользователь", request.ownerId());
                }
        );
        Projects projects = projectMapper.toEntity(request);
        projectsRepository.save(projects);
        log.info("Проект успешно создан");
        return projectMapper.toProjectResponseDto(projects);
    }
    // GET метод в контролере
    // todo 2: Получение проекта по id // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public ProjectResponseDTO getProjectById(Long id){
        log.info("Получение проекта по id");
        Projects project = projectsRepository.findById(id).orElseThrow(()-> {
            log.error("Проект с id {} не найден", id);
            return new ResourceNotFoundException("Проект с id {} не найден", id);
                }
        );
        log.info("Проект с id {} получен", id);
        return projectMapper.toProjectResponseDto(project);
    }
    // GET метод в контролере
    // todo 3: Получение списка всех проектов // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<ProjectResponseSummaryDTO> getAllProjects(){
        log.info("Получение списка проектов");
        List<Projects> projects = projectsRepository.findAll();
        log.info("Список проектов получен");
        return projects.stream().map(projectMapper::toProjectResponseSummaryDto)
                .collect(Collectors.toList());
    }
    // GET метод для контролера
    // todo 4: Получение проектов по статусу // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<ProjectResponseSummaryDTO> getProjectByStatus(String status){
        log.info("Получение проектов по статусу");
        if(!isValidStatus(status)){
            log.error("Статус может быть ACTIVE, COMPLETED, ARCHIVED, ваш статус: {} ", status);
            throw new BadStatusProjectException(status);
        }
        List<Projects> projects = projectsRepository.findByStatus(status);
        log.info("Успешное получение проектов по статусу");
        return projects.stream().map(projectMapper::toProjectResponseSummaryDto)
                .collect(Collectors.toList());
    }
    // Вспомогательный метод для проверки статуса // Если вдруг не пройдем валидацию на контролере (Что мало вероятно)
    private boolean isValidStatus(String status) {
        return status != null && (
                status.equalsIgnoreCase("ACTIVE") ||
                        status.equalsIgnoreCase("COMPLETED") ||
                        status.equalsIgnoreCase("ARCHIVED")
        );
    }

    // todo 5: Получение проектов владельца ( по owner_id ) // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<ProjectResponseSummaryDTO> getProjectByOwnerId(Long id){
        // Проверка существует ли вообще данный пользователь в базе
        log.info("Получение списка проектов пользователя");
        usersRepository.findById(id).orElseThrow(()->{
            log.error("Пользователь с id: {} не существует", id);
            return new ResourceNotFoundException("Пользователь с id: {} не существует", id);
        });
        //
        List<Projects> projects = projectsRepository.findByOwnerId(id);
        log.info("Получен список проектов у пользователя с id: {}", id);
        return projects.stream().map(projectMapper::toProjectResponseSummaryDto)
                .collect(Collectors.toList());
    }
    // Get метод для контролера
    // todo 6: Получение проектов где пользователь участник ( через user_project ) // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<ProjectResponseSummaryDTO> getProjectsByParticipant(Long id){
        // Найти пользователей с конкретной role т.е у меня должна быть проверка на role
        log.info("Получение проектов где пользователь с id: {} участник", id);
        usersRepository.findById(id).orElseThrow(()->{
            log.error("Пользователя с id: {} не существует", id);
            return new ResourceNotFoundException("Пользователь", id);
        });
        // Мне нужно написать сигнатуру метода и проверить на соответствие конкретной роли,
        // то есть вытянуть роль, проверить на соответствие конкретной роли
        List<Projects> projects = projectsRepository.findProjectsByParticipantWithRole(id);
        log.info("Проекты где пользователь с id: {} является участником", id);
        return projects.stream().map(projectMapper::toProjectResponseSummaryDto)
                .collect(Collectors.toList());
    }
    // Update метод для контролера
    // todo 7: Обновление проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO request){
        log.info("Обновление проекта в базе");
        Projects existingProject = projectsRepository.findById(id).orElseThrow(()-> {
            log.error("Проект с id: {} не найден", id);
            return new ResourceNotFoundException("Проект", id);
        });
        projectMapper.updateEntity(existingProject, request);
        //Проверка: если меняется владелец
        if (request.ownerId() != null && !request.ownerId().equals(existingProject.getOwner().getId())) {
            log.error("Владелец с id: {} не найден", request.ownerId());
            Users newOwner = usersRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Владелец", request.ownerId()));
            existingProject.setOwner(newOwner);
        }
        Projects updatedProject = projectsRepository.save(existingProject); // Обновляем данные
        log.info("Обновление проекта в базе прошло успешно");
        return projectMapper.toProjectResponseDto(updatedProject);

    }
    //DELETE метод для контролера
    // todo 8: Удаление проекта по id // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    @Transactional
    public void deleteProjectById(Long id){
        log.info("Удаления проекта по id: {}", id);
        // Проверка существует ли такой проект в базе
        projectsRepository.findById(id).orElseThrow( ()->{
            log.error("Проект с id: {} не найден", id);
            return new ResourceNotFoundException("Проект", id);
        });
        projectsRepository.deleteById(id);
        log.info("Удаление проекта по id: {} прошло успешно", id);
    }

    // TODO Управление участниками ( ManyToMany )

    // todo 9: Добавление пользователя в проект ( участник ) // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    @Transactional
    public ProjectResponseDTO addUserToProject(Long projectId, Long userId){
        log.info("Добавление пользователя с id: {} в проект {}", userId, projectId);
        // Проверка есть ли такой пользователь
        Projects projects = projectsRepository.findById(projectId).orElseThrow(()->{
            log.error("Проект с id: {} не найден", projectId);
            return new ResourceNotFoundException("Проект с id: {} не найден", projectId);
        });
        Users user = usersRepository.findById(userId).orElseThrow(()->{
            log.error("Пользователь с id: {} не найден", userId);
            return new ResourceNotFoundException("Пользователь", userId);
        });
        if(userId.equals(projects.getOwner().getId())){
            log.error("Пользователь id: {} является владельцем проекта ", userId);
            throw new BadRequestException("Пользователь является владельцем проекта");
        }
        // Проверка является ли пользователь владельцем, чтобы избежать дублирования
        // Проверка есть ли уже данный пользователь в проекте, чтобы избежать дублирование
        if(projects.getUsers().stream().anyMatch(u -> userId.equals(u.getId()))){
            log.error("Попытка добавить уже существующего пользователя");
            throw new BadRequestException("Попытка добавить дубликат пользователя");
        }
        projects.getUsers().add(user);
        projectsRepository.save(projects);
        log.info("Пользователь с id: {} добавлен в проект {}", userId, projectId);
        return projectMapper.toProjectResponseDto(projects);
    }

    // todo 10: Удаление пользователя из проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    @Transactional
    public void deleteUserFromProject(Long projectId, Long userId){
        log.info("Удаления пользователя с id: {} и проекта {}", userId, projectId);
        Projects projects = projectsRepository.findById(projectId).orElseThrow(()-> {
            log.error("Проект с id: {} не найден", projectId);
            return new ResourceNotFoundException("Проект", projectId);
        });
        Users user = usersRepository.findById(userId).orElseThrow(()->{
            log.error("Пользователь с id: {} не найден", userId);
            return new ResourceNotFoundException("Пользователь", userId);
        });
        if(userId.equals(projects.getOwner().getId())){
            log.error("Попытка удалить владельца проекта");
            throw new BadRequestException("Попытка удаления владельца проекта");
        }
        projects.getUsers().remove(user);
        projectsRepository.save(projects);
        log.info("Удаления пользователя с id: {} прошло успешно", userId);
    }

    // todo 11: Проверка является ли пользователь участником проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public boolean isUserInProject(Long projectId, Long userId){
        // Проверка существует ли вообще данный проект
        log.info("Является ли пользователь участником проекта");
        Projects projects = projectsRepository.findById(projectId).orElseThrow(()->{
            log.error("Проект с id: {} не существует",projectId);
            return new ResourceNotFoundException("Проект", projectId);
        });
        usersRepository.findById(userId).orElseThrow(()->{
            log.error("Пользователь с id: {} не существует", userId);
            return new ResourceNotFoundException("Пользователь", userId);
        });
        log.info("Проверка прошла успешно");
        return projects.getUsers().stream().anyMatch(users -> userId.equals(users.getId()));
    }

    // todo 12: Получение списка участников проекта // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<ProjectResponseParticipantDTO> getProjectParticipants(Long id){
        log.info("Получение списка участников проекта с id: {}", id);
        // Проверить существует ли такой проект
        Projects projects = projectsRepository.findById(id).orElseThrow(()->{
            log.error("Проект с id {} не найден",id);
            return new ResourceNotFoundException("Проект", id);
        });
        List<Users> users = projects.getUsers();
        log.info("Получен список участников проекта");
        return users.stream().map(projectMapper::toParticipantDto)
                .collect(Collectors.toList());
    }
    // todo 13: Получение списка проектов пользователя //
    public List<ProjectResponseSummaryDTO> getUserProjects(Long id){
        // Проверка есть ли такой пользователь
        Users user = usersRepository.findById(id).orElseThrow(()->{
            log.error("Пользователя с id:{} не существует", id);
            return new ResourceNotFoundException("Пользователь", id);
        });
        List<Projects> projects = user.getProjects();
        log.info("Получен список проектов у пользователя с id: {}", id);
        return projects.stream().map(projectMapper::toProjectResponseSummaryDto)
                .collect(Collectors.toList());
    }

}
