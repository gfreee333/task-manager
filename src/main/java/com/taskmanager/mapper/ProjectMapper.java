package com.taskmanager.mapper;

import com.taskmanager.dto.request.projects.ProjectRequestDTO;
import com.taskmanager.dto.response.projects.ProjectResponseDTO;
import com.taskmanager.dto.response.projects.ProjectResponseParticipantDTO;
import com.taskmanager.dto.response.projects.ProjectResponseSummaryDTO;
import com.taskmanager.model.Projects;
import com.taskmanager.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ProjectMapper {
    // ========== Обновление Entity =============
    void updateEntity(@MappingTarget Projects project, ProjectRequestDTO request);
    // ========== DTO → ENTITY  ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(source = "ownerId", target = "owner", qualifiedByName = "idToUser")
    Projects toEntity(ProjectRequestDTO requestDTO);

    // ========== ENTITY → DTO  ==========
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.firstName", target = "ownerFirstName")
    @Mapping(source = "owner.lastName", target = "ownerLastName")
    ProjectResponseDTO toProjectResponseDto(Projects projects);

    // Краткий ответ (без владельца)
    ProjectResponseSummaryDTO toProjectResponseSummaryDto(Projects projects);

    // Преобразование User → ParticipantDTO (для списка участников)
    @Mapping(source = "id", target = "userId")
    ProjectResponseParticipantDTO toParticipantDto(Users user);

    // Вспомогательный метод
    @Named("idToUser")
    default Users userFromId(Long id) {
        if (id == null) return null;
        Users user = new Users();
        user.setId(id);
        return user;
    }

}
