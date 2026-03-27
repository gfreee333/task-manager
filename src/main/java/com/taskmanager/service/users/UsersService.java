package com.taskmanager.service.users;
import com.taskmanager.dto.request.users.UserRequestDTO;
import com.taskmanager.dto.response.users.UserResponseDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.DuplicateResourceException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.exception.domain.UserByEmailNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.Users;
import com.taskmanager.repository.users.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class UsersService {
    private static final Logger log = LoggerFactory.getLogger(UsersService.class);

    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UsersService(UsersRepository usersRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // todo 1: Получение пользователя по id
    public UserResponseDTO getUserById(Long id){
        log.info("Получения User по id: {}", id);
        Users user = usersRepository.findById(id).orElseThrow( () -> {
            log.error("User с id {} не найден", id);
            return new ResourceNotFoundException("User", id);
        });
        log.debug("Пользователь с id {} получен из БД", id);
        UserResponseDTO responseDTO = userMapper.toResponseDTO(user);
        log.info("Пользователь с id {} успешно получен и возвращен", id);
        return responseDTO;
    }
    // todo 2: Создание пользователя в БД с проверкой по email
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userDto) {
        log.info("Создание нового пользователя с email: {}", userDto.email());
        // Проверка существует ли пользователь с подобным email
        if(usersRepository.existsByEmail(userDto.email())){
            log.error("Email {} уже зарегистрирован", userDto.email());
            throw new DuplicateResourceException("Пользователь с Email "
                    + userDto.email() + " уже существует");
        }
        Users user = userMapper.toEntity(userDto);
        // Производим кэширование пароля
        user.setPassword(passwordEncoder.encode(userDto.password()));
        Users savedUsers = usersRepository.save(user);
        log.info("Пользователь создан с id: {}", savedUsers.getId());
        return userMapper.toResponseDTO(savedUsers);
    }

    // todo 3: Получить список всех пользователей в БД
    public List<UserResponseDTO> getAllUser(){
        log.info("Получение списка всех пользователей");
        List<Users> users = usersRepository.findAll();
        List<UserResponseDTO> usersDto = users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
        log.info("Получено {} пользователей", usersDto.size());
        return usersDto;
    }

    // todo 4: Получить пользователя по email
    public UserResponseDTO getUserByEmail(String email){
        log.info("Получение пользователя по email");
        // Нужно сделать проверку на наличие данного пользователя с таким email в базе
        Users user = usersRepository.findUsersByEmail(email)
                .orElseThrow( () -> {
                    log.error("Пользователя с email {} не найден", email);
                    return new UserByEmailNotFoundException(email);
                });
        log.info("Пользователь с email {} получен", user.getEmail());
        return userMapper.toResponseDTO(user);
    }
    //todo 5: Получить пользователя по role (фильтрация)
    public List<UserResponseDTO> getUserByRole(String role){
        log.info("Получить пользователя по Role");
        List<Users> users = usersRepository.findAllByRole(role);
        if(users.isEmpty()) log.info("Пользователь с ролью {} не найден", role);
        else log.info("Найдено {} пользователей с ролью {}", users.size(), role);
        return users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // todo 6. Обновление данных пользователя
    @Transactional
    public UserResponseDTO updateUserById(Long id, UserRequestDTO request){
        log.info("Обновление пользователя по id");
        Users user = usersRepository.findById(id).orElseThrow(()-> {
                log.error("Пользователь с id {} не найден", id);
                return new ResourceNotFoundException("Пользователь с id {} не найден", id);
            }
        );
        // Проверка на уникальность email
        if (!request.email().equals(user.getEmail()) &&
                usersRepository.existsByEmail(request.email())) {
            log.error("Email {} уже используется", request.email());
            throw new DuplicateResourceException("Email уже используется");
        }
        userMapper.updateEntity(user, request);
        // Кэшируем пароль при изменении пароля.
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        Users updatedUser = usersRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }

    // todo: 8 Поиск пользователя по имени и фамилии ( частичное совпадение )
    public List<UserResponseDTO> getUserByNameAndLastName(String firstName, String lastName){
        // Нашли пользователя по имени и фамилии в базе
        log.info("Частичный поиск по имени и фамилии");
        List<Users> users = usersRepository.findByFirstNameContainingAndLastNameContaining(firstName, lastName);
        if(users.isEmpty()) {
            log.error("Пользователей не найдено");
            return Collections.emptyList();
        }
        log.info("Найдено {} пользователей", users.size());
        return users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    //todo 7. Удаление пользователя с проверкой связей
    // // 7.1 Запрет на удаление пользователя если у него есть активные задачи
    // // 7.2 Запрет на удаление пользователя если у него есть созданные задачи
    // // 7.3 Запрет на удаление пользователя если у него есть свои проекты и он владелец
    // // 7.4 Запрет на удаление пользователя если он участвует в проекте
    @Transactional
    public void deleteUser(Long id){
        log.info("Попытка удаление пользователя");
        // Проверка существует ли вообще данный пользователь
        Users user = usersRepository.findById(id).orElseThrow(()->{
            log.error("Пользователя с id: {} не было найдено", id);
            return new ResourceNotFoundException("Пользователя с id: {} не было найдено", id);
        });
        // Проверка является ли пользователь создателем задач
        if(!user.getCreatedTasks().isEmpty()) {
            log.error("Нельзя удалить пользователя, который создавал задачи");
            throw new BadRequestException("Нельзя удалить пользователя, который создавал задачи");
        }
        // Проверка является ли пользователь исполнителем
        if(!user.getAssigneeTasks().isEmpty()) {
            log.error("Нельзя удалить пользователя, который является исполнителем задачи");
            throw new BadRequestException("Нельзя удалить пользователя, который является исполнителем задачи");
        }
        // Есть ли проекты, где пользователь является владельцем
        if(!user.getProjects().isEmpty()) {
            log.error("Нельзя удалить пользователя, который является создателем проекта");
            throw new BadRequestException("Нельзя удалить пользователя, который является создателем проекта");
        }
        // Участвует ли пользователь в каких-то проектах
        if(user.getParticipatedProjects() != null && !user.getParticipatedProjects().isEmpty()) {
            log.error("Нельзя удалить пользователя участвующего в проекте");
            throw new BadRequestException("Нельзя удалить пользователя участвующего в проекте");
        }
        usersRepository.delete(user);
        log.info("Успешное удаление пользователя");
    }
}
