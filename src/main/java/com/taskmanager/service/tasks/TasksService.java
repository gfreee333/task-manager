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
import com.taskmanager.service.projects.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TasksService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;
    private final UsersRepository usersRepository;
    private final TaskMapper taskMapper;
    public TasksService(TasksRepository tasksRepository, ProjectsRepository projectsRepository, UsersRepository usersRepository, TaskMapper taskMapper) {
        this.tasksRepository = tasksRepository;
        this.projectsRepository = projectsRepository;
        this.usersRepository = usersRepository;
        this.taskMapper = taskMapper;
    }
    String taskNotFound = "Задачи с таким id: {} не существует";
    String taskFound = "Задача с id: {} получена";
    String projectNotFound = "Проекта с id: {} не существует";

    // todo 1: Создание задачи с указанием // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    //  ( Исполнитель assignee, Создатель created_by, Проекты project )
    @Transactional
    public TasksResponseDTO createTask(TasksRequestDTO request){
        log.info("Попытка добавить задачу в базу");
        // Проверка существует ли вообще данный проект
        Projects project = projectsRepository.findById(request.projectId()).orElseThrow(()->{
            log.error("Проекта с id: {} не существует", request.projectId());
            return new ResourceNotFoundException("Проекта с id: {} не существует", request.projectId());
        });
        // Проверка существует ли вообще данный исполнитель
        Users assignee = usersRepository.findById(request.assigneeId()).orElseThrow(()->{
           log.error("Исполнитель с id: {} не существует", request.assigneeId());
           return new ResourceNotFoundException("Исполнитель с id: {} не существует", request.assigneeId());
        });
        Users creator = usersRepository.findById(request.createdById()).orElseThrow(()->{
            log.error("Создателя с id: {} не существует", request.createdById());
            return new ResourceNotFoundException("Создателя с id: {} не существует", request.createdById());
        });
        // Проверка является ли исполнитель участником проекта, чтобы назначить ему задачу
        if(!project.getUsers().contains(assignee)){
            log.error("Исполнитель не является участником проекта");
            throw new BadRequestException("Исполнитель должен быть участником проекта");
        }

        Tasks tasks = taskMapper.toEntity(request);
        tasks.setProject(project);
        tasks.setAssignee(assignee);
        tasks.setCreatedBy(creator);
        Tasks savedTask = tasksRepository.save(tasks);
        log.info("Добавление задачи прошло успешно");
        return taskMapper.toResponseDTO(savedTask);
    }

    // todo 2: Обновление задачи //
    @Transactional
    public TasksResponseDTO updateTaskById(Long id, TasksRequestDTO request){
        // Проверка есть ли данная задача в базе
        log.info("Попытка обновить задачу с id: {}", id);
        Tasks tasks = tasksRepository.findById(id).orElseThrow(()->{
            log.error("Задача с id: {} не существует", id);
            return new ResourceNotFoundException("Задача с id: {} не существует", id);
        });
        taskMapper.updateEntity(tasks, request);
        Tasks updatedTask = tasksRepository.save(tasks);
        log.info("Успешное обновление задачи с id: {}", id);
        return taskMapper.toResponseDTO(updatedTask);
    }

    // todo 3: Удаление задачи // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    @Transactional
    public void deleteTaskById(Long id){
        log.info("Удаление задачи с id: {} ", id);
        tasksRepository.findById(id).orElseThrow(()->{
            log.error("Задача с id: {} не существует", id);
            return new ResourceNotFoundException("Задача", id);
        });
        tasksRepository.deleteById(id);
        log.info("Удаление задачи с id: {} прошло успешно", id);
    }

    // todo 4: Получение задач по id (Возвращаю детальную информацию о задачи) // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public TasksResponseDTO getTaskById(Long id){
        log.info("Получение задачи по id: {}", id);
        Tasks tasks = tasksRepository.findById(id).orElseThrow(()->{
            log.error(taskNotFound, id);
            return new ResourceNotFoundException(taskNotFound, id);
        });
        log.info(taskFound, id);
        return taskMapper.toResponseDTO(tasks);
    }

    // todo 5: Получение всех задач // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<TasksSummaryDTO> getAllTask(){
        log.info("Получение всех задач");
        List<Tasks> tasks = tasksRepository.findAll();
        log.info("Все задачи в базе были получены");
        return tasks.stream().map(taskMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // todo 6: Получение задач по проекту // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<TasksSummaryDTO> getAllTaskByProject(Long id){
        log.info("Получение всех задач у конкретного проекта с id: {} ", id);
        projectsRepository.findById(id).orElseThrow(()->{
            log.error(projectNotFound, id);
            return new ResourceNotFoundException(projectNotFound, id);
        });
        List<Tasks> tasks = tasksRepository.findAllByProjectId(id);
        log.info("Успешное получение всех задач у проекта");
        return tasks.stream().map(taskMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
    // todo 7: Получение задач по исполнителю // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<TasksSummaryDTO> getAllTaskByAssignee(Long id){
        log.info("Получение всех задач исполнителя с id: {} ", id);
        usersRepository.findById(id).orElseThrow(()->{
            log.error("Исполнителя с id:{} не существует", id);
            return new ResourceNotFoundException("Исполнитель с id: {} не существует", id);
        });
        List<Tasks> tasks = tasksRepository.findAllByAssigneeId(id);
        log.info("Успешное получение всех задач исполнителя с id: {}", id);
        return tasks.stream().map(taskMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
    // todo 8: Получение задач по создателю // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<TasksSummaryDTO> getAllTaskByCreated(Long id){
        log.info("Получение всех задач по id: {} создатель", id);
        usersRepository.findById(id).orElseThrow(()->{
            log.error("Создателя с id: {} не существует", id);
            return new ResourceNotFoundException("Создателя с id: {} не существует", id);
        });
        List<Tasks> tasks = tasksRepository.findAllByCreatedById(id);
        log.info("Успешное получение всех задач создателя с id: {} ", id);
        // Возвращение результата
        return tasks.stream().map(taskMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
    // todo 9: Получение задач по статусу // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<TasksSummaryDTO> getAllTaskByStatus(String status){
        log.info("Получение всех задач по статусу: {} ", status);
        List<Tasks> tasks = tasksRepository.findAllByStatus(status);
        log.info("Успешное получение всех задач по статусу: {} ", status);
        return tasks.stream().map(taskMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
    // todo 10: Получение задач по приоритету // ГОТОВ + ПОКРЫТ ТЕСТАМИ
    public List<TasksSummaryDTO> getAllTaskByPriority(String priority){
        log.info("Попытка получить все задачи с конкретным priority: {} ", priority);
        List<Tasks> tasks = tasksRepository.findAllByPriority(priority);
        log.info("Успешное получение всех задач по приоритету: {} ", priority);
        return tasks.stream().map(taskMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
}
