package com.taskmanager.mapper;
import com.taskmanager.dto.request.users.UserRequestDTO;
import com.taskmanager.dto.response.users.UserResponseDTO;
import com.taskmanager.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

//
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(Users users);
    @Mapping(target = "id", ignore = true) // Id генерируем в БД
    @Mapping(target = "password", ignore = true) // Пароль хэшируется в сервисе
    @Mapping(target = "createdAt", ignore = true) // Генерируем автоматически
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assigneeTasks", ignore = true) // коллекции не маппим
    @Mapping(target = "createdTasks", ignore = true)
    Users toEntity(UserRequestDTO requestDTO);
    //Обновление сущностей // Сделаю чуть позже при необходимости
    void updateEntity(@MappingTarget Users user, UserRequestDTO request);

}
