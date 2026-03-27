package com.taskmanager.mapper;
import com.taskmanager.dto.request.tasks.TasksRequestDTO;
import com.taskmanager.dto.response.tasks.TasksResponseDTO;
import com.taskmanager.dto.response.tasks.TasksSummaryDTO;
import com.taskmanager.model.Tasks;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.firstName", target = "assigneeFirstName")
    @Mapping(source = "project.name", target = "projectName")
    TasksSummaryDTO toSummaryDTO(Tasks tasks);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.firstName", target = "assigneeFirstName")
    @Mapping(source = "assignee.lastName", target = "assigneeLastName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "createdBy.firstName", target = "createdByFirstName")
    @Mapping(source = "createdBy.lastName", target = "createdByLastName")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    TasksResponseDTO toResponseDTO(Tasks tasks);

    // Игнорирование авто генерирование полей
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "project", ignore = true)
    Tasks toEntity(TasksRequestDTO request);

    // Для обновления entity
    void updateEntity(@MappingTarget Tasks tasks, TasksRequestDTO request);

}
