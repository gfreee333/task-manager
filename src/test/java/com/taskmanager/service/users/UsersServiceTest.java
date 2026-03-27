package com.taskmanager.service.users;

import com.taskmanager.dto.request.users.UserRequestDTO;
import com.taskmanager.dto.response.users.UserResponseDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.DuplicateResourceException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.exception.domain.UserByEmailNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.Projects;
import com.taskmanager.model.Tasks;
import com.taskmanager.model.Users;
import com.taskmanager.repository.users.UsersRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UsersService usersService;
    private Users testUser;
    private Users anotherUser;
    private UserRequestDTO testRequest;
    private UserRequestDTO updateRequest;
    private UserResponseDTO testResponse;
    private UserResponseDTO anotherResponse;
    private final Long CORRECT_ID = 1L;
    private final Long INVALID_ID = 999L;

    @BeforeEach// Подготовка тестовых данных
    void setUp() {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setPassword("encodePassword");
        testUser.setFirstName("Иван");
        testUser.setLastName("Чухманов");
        testUser.setRole("USER");
        testUser.setCreatedAt(Timestamp.from(Instant.now()));
        testUser.setUpdatedAt(Timestamp.from(Instant.now()));

        anotherUser = new Users();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@test.com");
        anotherUser.setPassword("encodePassword2");
        anotherUser.setFirstName("Петр");
        anotherUser.setLastName("Петрович");
        anotherUser.setCreatedAt(Timestamp.from(Instant.now()));
        anotherUser.setUpdatedAt(Timestamp.from(Instant.now()));


        testRequest = new UserRequestDTO(
                "test@test.com",
                "password123",
                "Иван",
                "Чухманов",
                "USER"
        );

        updateRequest = new UserRequestDTO(
                "newemail@test.com",
                "newStrongPassword123",
                "Алексей",
                "Сидоров",
                "ADMIN"
        );

        testResponse = new UserResponseDTO(
                "test@test.com",
                "Иван",
                "Чухманов",
                "USER",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );
        anotherResponse = new UserResponseDTO(
                "another@test.com",
                "Петр",
                "Петрович",
                "USER",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

    }
    //==========================================================
    // УСПЕШНОЕ СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ
    //==========================================================
    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void createUser_Success_ReturnUserResponseDTO() {
        when(usersRepository.existsByEmail(testRequest.email())).thenReturn(false);
        when(userMapper.toEntity(testRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(testRequest.password())).thenReturn("encodePassword");
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponse);
        UserResponseDTO result = usersService.createUser(testRequest);
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@test.com");
        verify(usersRepository, times(1)).existsByEmail(testRequest.email());
        verify(usersRepository, times(1)).save(any(Users.class));
        verify(passwordEncoder, times(1)).encode(testRequest.password());
    }
    //==========================================================
    // ОШИБКА, ПОЛЬЗОВАТЕЛЬ С ДАННЫМ EMAIL УЖЕ СУЩЕСТВУЕТ
    //==========================================================
    @Test
    @DisplayName("Создание пользователя - email уже существует")
    void createUser_DuplicateEmail_ThrowsDuplicateResourceException() {
        when(usersRepository.existsByEmail(testRequest.email())).thenReturn(true);
        assertThatThrownBy(() -> usersService.createUser(testRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");
        verify(usersRepository, times(1)).existsByEmail(testRequest.email());
        verify(usersRepository, never()).save(any(Users.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).toResponseDTO(any(Users.class));
    }
    //==========================================================
    // УСПЕШНОЕ ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ID
    //==========================================================
    @Test
    @DisplayName("Успешное получение пользователя по id - Успешный сценарий")
    void getUserById_ReturnUserResponseDTO() {
        when(usersRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponse);
        UserResponseDTO result = usersService.getUserById(1L);
        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Иван");
        assertThat(result.lastName()).isEqualTo("Чухманов");
        assertThat(result.email()).isEqualTo("test@test.com");
        verify(usersRepository, times(1)).findById(1L);
    }
    //==========================================================
    // ПОЛЬЗОВАТЕЛЯ С ДАННЫМ ID НЕ СУЩЕСТВУЕТ
    //==========================================================
    @Test
    @DisplayName("Пользователя с данным id не существует")
    void getUserById_ReturnResourceNotFoundException() {
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> usersService.getUserById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User с id 999 не найден");
        verify(usersRepository, times(1)).findById(INVALID_ID);
    }
    //==========================================================
    // ПОЛУЧИТЬ СПИСКО ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
    //==========================================================
    @Test
    @DisplayName("Получение списка всех пользователей - Успешный сценарий")
    void getAllUser_ReturnListUserResponseDTO() {
        List<Users> usersList = List.of(testUser, anotherUser);
        when(usersRepository.findAll()).thenReturn(usersList);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponse);
        when(userMapper.toResponseDTO(anotherUser)).thenReturn(anotherResponse);
        List<UserResponseDTO> result = usersService.getAllUser();
        Assertions.assertEquals(2, result.size());
        assertThat(result.get(0).email()).isEqualTo("test@test.com");
        assertThat(result.get(1).email()).isEqualTo("another@test.com");
        verify(usersRepository, times(1)).findAll();
    }
    //==========================================================
    // ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО EMAIL
    //==========================================================
    @Test
    @DisplayName("Получение пользователя по email - Успешный сценарий")
    void getUserByEmail_ReturnUserResponseDTO() {
        when(usersRepository.findUsersByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponse);

        when(usersRepository.findUsersByEmail("another@test.com")).thenReturn(Optional.of(anotherUser));
        when(userMapper.toResponseDTO(anotherUser)).thenReturn(anotherResponse);

        UserResponseDTO result1 = usersService.getUserByEmail("test@test.com");
        assertThat(result1).isNotNull();
        assertThat(result1.email()).isEqualTo("test@test.com");

        UserResponseDTO result2 = usersService.getUserByEmail("another@test.com");
        assertThat(result2).isNotNull();
        assertThat(result2.email()).isEqualTo("another@test.com");

        verify(usersRepository, times(1)).findUsersByEmail("test@test.com");
        verify(usersRepository, times(1)).findUsersByEmail("another@test.com");
    }
    //==========================================================
    // ПОЛЬЗОВАТЕЛЬ С EMAIL НЕ НАЙДЕН
    //==========================================================
    @Test
    @DisplayName("Пользователь с данным email не найден")
    void getUserByEmail_ReturnUserByEmailNotFoundException() {
        String email = "not@test.com";
        when(usersRepository.findUsersByEmail(email)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> usersService.getUserByEmail(email))
                .isInstanceOf(UserByEmailNotFoundException.class)
                .hasMessageContaining("не найден");
        verify(usersRepository, times(1)).findUsersByEmail(email);
        verify(userMapper, never()).toResponseDTO(any(Users.class));
    }
    //==========================================================
    // ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ROLE
    //==========================================================
    @Test
    @DisplayName("Получение пользователя по role - Успешный сценарий")
    void getUserByRole_ReturnListUserResponseDTO(){
        String role = "USER";
        List<Users> usersList = List.of(testUser, anotherUser);
        when(usersRepository.findAllByRole(role)).thenReturn(usersList);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponse);
        when(userMapper.toResponseDTO(anotherUser)).thenReturn(anotherResponse);
        List<UserResponseDTO> result = usersService.getUserByRole(role);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).email()).isEqualTo("test@test.com");
        assertThat(result.get(1)).isNotNull();
        assertThat(result.get(1).email()).isEqualTo("another@test.com");
        verify(usersRepository, times(1)).findAllByRole(role);
        verify(userMapper, times(1)).toResponseDTO(testUser);
        verify(userMapper, times(1)).toResponseDTO(anotherUser);
    }
    //==========================================================
    // ОБНОВЛЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ
    //==========================================================
    @Test
    @DisplayName("Обновление данных пользователя - Успешный сценарий")
    void updateUserById_ReturnUserResponseDTO() {
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        when(usersRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(updateRequest.password())).thenReturn("newEncodedPassword");
        when(usersRepository.save(testUser)).thenReturn(testUser);

        UserResponseDTO expectedResponse = new UserResponseDTO(
                updateRequest.email(),
                updateRequest.firstName(),
                updateRequest.lastName(),
                updateRequest.role(),
                testUser.getCreatedAt(),
                Timestamp.from(Instant.now())
        );
        when(userMapper.toResponseDTO(testUser)).thenReturn(expectedResponse);

        UserResponseDTO result = usersService.updateUserById(CORRECT_ID, updateRequest);

        assertThat(result.email()).isEqualTo(updateRequest.email());
        assertThat(result.firstName()).isEqualTo(updateRequest.firstName());
        assertThat(result.lastName()).isEqualTo(updateRequest.lastName());
        assertThat(result.role()).isEqualTo(updateRequest.role());

        verify(usersRepository).findById(CORRECT_ID);
        verify(usersRepository).existsByEmail(updateRequest.email());
        verify(usersRepository).save(testUser);
        verify(passwordEncoder).encode(updateRequest.password());
        verify(userMapper).updateEntity(testUser, updateRequest);
    }
    //==========================================================
    // ОБНОВЛЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ - ПОЛЬЗОВАТЕЛЯ НЕ СУЩЕСТВУЕТ
    //==========================================================
    @Test
    @DisplayName("Обновление данных, пользователя не существует")
    void updateUserById_ReturnRecurseNotFoundException(){
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->usersService.updateUserById(INVALID_ID, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
        verify(usersRepository).findById(INVALID_ID);
        verify(usersRepository, never()).existsByEmail(anyString());
        verify(usersRepository, never()).save(any(Users.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).updateEntity(any(Users.class), any(UserRequestDTO.class));
    }
    //==========================================================
    // ОБНОВЛЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ - НАРУШЕНИЕ УНИКАЛЬНОСТИ EMAIL
    //==========================================================
    @Test
    @DisplayName("Обновление данных, ошибка уникальности email")
    void updateUserById_ReturnDuplicateResourceException(){
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        when(usersRepository.existsByEmail("newemail@test.com")).thenReturn(true);
        assertThatThrownBy(()->usersService.updateUserById(CORRECT_ID, updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email уже используется");
        verify(usersRepository).findById(CORRECT_ID);
        verify(usersRepository).existsByEmail("newemail@test.com");
        verify(usersRepository, never()).save(any(Users.class));
        verify(passwordEncoder,never()).encode(anyString());
        verify(userMapper, never()).updateEntity(any(Users.class), any(UserRequestDTO.class));
    }
    //==========================================================
    // ПОИСК ПОЛЬЗОВАТЕЛЯ ПО ИМЕНИ И ФАМИЛИИ
    //==========================================================
    @Test
    @DisplayName("Поиск пользователя по имени и фамилии - Успешный сценарий")
    void getUserByFirstNameAndLastName_ReturnListResponse(){
        List<Users> users = List.of(testUser, anotherUser);
        when(usersRepository.findByFirstNameContainingAndLastNameContaining("Иван", "Чухманов")).thenReturn(users);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponse);
        when(userMapper.toResponseDTO(anotherUser)).thenReturn(anotherResponse);
        List<UserResponseDTO> result = usersService.getUserByNameAndLastName("Иван", "Чухманов");
        assertThat(result).isNotNull();
        assertThat(result.get(0).firstName()).isEqualTo("Иван");
        assertThat(result.get(1).firstName()).isEqualTo("Петр");
        assertThat(result.get(0).lastName()).isEqualTo("Чухманов");
        assertThat(result.get(1).lastName()).isEqualTo("Петрович");
        verify(usersRepository, times(1)).findByFirstNameContainingAndLastNameContaining("Иван", "Чухманов");
        verify(userMapper, times(1)).toResponseDTO(testUser);
        verify(userMapper, times(1)).toResponseDTO(anotherUser);
    }
    //==========================================================
    // УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ
    //==========================================================
    @Test
    @DisplayName("Удаление пользователя - успешный сценарий")
    void deleteUser_ReturnVoid(){
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        usersService.deleteUser(CORRECT_ID);
        verify(usersRepository, times(1)).findById(CORRECT_ID);
        verify(usersRepository, times(1)).delete(testUser);
    }
    //==========================================================
    // УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ, ПОЛЬЗОВАТЕЛЬ НЕ НАЙДЕН
    //==========================================================
    @Test
    @DisplayName("Пользователь не найден")
    void deleteUser_ReturnResourceNotFoundException(){
        when(usersRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(()->usersService.deleteUser(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("не найден");
        verify(usersRepository).findById(INVALID_ID);
        verify(usersRepository, never()).delete(any(Users.class));
    }
    //==========================================================
    // ПОЛЬЗОВАТЕЛЬ ЯВЛЯЕТСЯ АВТОРОМ ХОТЯ БЫ ОДНОЙ ЗАДАЧИ
    //==========================================================
    @Test
    @DisplayName("Пользователь является создателем задач(и)")
    void deleteUser_TasksCreator_ReturnBadRequestException(){
        Tasks task = new Tasks();
        task.setId(1L);
        task.setTitle("ЗАДАЧА");
        testUser.setCreatedTasks(List.of(task));
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        assertThatThrownBy(()->usersService.deleteUser(CORRECT_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Нельзя удалить пользователя, который создавал задачи");
        verify(usersRepository).findById(CORRECT_ID);
        verify(usersRepository, never()).delete(testUser);
    }
    //==========================================================
    // ПОЛЬЗОВАТЕЛЬ ЯВЛЯЕТСЯ ИСПОЛНИТЕЛЕМ ЗАДАЧИ
    //==========================================================
    @Test
    @DisplayName("Пользователь является исполнителем задачи")
    void deleteUser_TasksAssignee_ReturnBadRequestException(){
        Tasks task = new Tasks();
        task.setId(1L);
        task.setTitle("ЗАДАЧА");
        testUser.setAssigneeTasks(List.of(task));
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        assertThatThrownBy(()->usersService.deleteUser(CORRECT_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Нельзя удалить пользователя, который является исполнителем задачи");
        verify(usersRepository).findById(CORRECT_ID);
        verify(usersRepository, never()).delete(testUser);
    }
    //==========================================================
    // ПОЛЬЗОВАТЕЛЬ ЯВЛЯЕТСЯ ВЛАДЕЛЬЦЕМ ПРОЕКТА
    //==========================================================
    @Test
    @DisplayName("Пользователь является владельцем проекта")
    void deleteUser_ProjectCreator_ReturnBadRequestException(){
        Projects project = new Projects();
        project.setId(1L);
        project.setDescription("ОПИСАНИЕ ПРОЕКТА");
        testUser.setProjects(List.of(project));
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        assertThatThrownBy(()->usersService.deleteUser(CORRECT_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Нельзя удалить пользователя, который является создателем проекта");
        verify(usersRepository).findById(CORRECT_ID);
        verify(usersRepository, never()).delete(testUser);
    }
    //==========================================================
    // ПОЛЬЗОВАТЕЛЬ ЯВЛЯЕТСЯ УЧАСТНИКОМ ПРОЕКТА
    //==========================================================
    @Test
    @DisplayName("Пользователь является участником проекта")
    void deleteUser_ParticipatedProjects_ReturnBadRequestException(){
        Projects project = new Projects();
        project.setId(1L);
        project.setDescription("ОПИСАНИЕ ПРОЕКТА");
        testUser.setParticipatedProjects(List.of(project));
        when(usersRepository.findById(CORRECT_ID)).thenReturn(Optional.of(testUser));
        assertThatThrownBy(()->usersService.deleteUser(CORRECT_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Нельзя удалить пользователя участвующего в проекте");
        verify(usersRepository).findById(CORRECT_ID);
        verify(usersRepository, never()).delete(testUser);
    }

}