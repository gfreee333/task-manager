package com.taskmanager.contoller.users;

import com.taskmanager.dto.request.users.UserRequestDTO;
import com.taskmanager.dto.response.users.UserResponseDTO;
import com.taskmanager.exception.common.BadRequestException;
import com.taskmanager.exception.common.DuplicateResourceException;
import com.taskmanager.exception.common.ResourceNotFoundException;
import com.taskmanager.exception.domain.UserByEmailNotFoundException;
import com.taskmanager.service.users.UsersService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsersControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UsersService usersService;

    // ========== ОБЩИЕ ДАННЫЕ (для всех тестов) ==========
    private UserResponseDTO testResponse;
    private UserRequestDTO testRequest;
    private final Long CORRECT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Базовый DTO для ответа
        testResponse = new UserResponseDTO(
                "test@test.com",
                "Иван",
                "Чухманов",
                "USER",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );

        // Базовый DTO для запроса
        testRequest = new UserRequestDTO(
                "test@test.com",
                "password123",
                "Иван",
                "Чухманов",
                "USER"
        );
    }
    //==========================================================
    // Успешное получение пользователя по Id HttpStatus 200
    //==========================================================
        @Nested
        @DisplayName("Успешные сценарии http status: (2xx)")
        class SuccessTests {
            @Test
            @DisplayName("GET /manager/api/users/{id} - успешное получение по id")
            void getUserById_Return200() throws Exception {
                when(usersService.getUserById(CORRECT_ID)).thenReturn(testResponse);
                mockMvc.perform(get("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value("test@test.com"))
                        .andExpect(jsonPath("$.firstName").value("Иван"))
                        .andExpect(jsonPath("$.lastName").value("Чухманов"))
                        .andExpect(jsonPath("$.role").value("USER"));
                verify(usersService, times(1)).getUserById(CORRECT_ID);
            }
        //==========================================================
        // Получение всех пользователей по имени и фамилии
        //==========================================================
            @Test
            @DisplayName("GET /manager/api/users?firstName={firstName}&lastName={lastName} - успешное получение по имени и фамилии")
            void getUsersByFirstNameAndLastName_Return200() throws Exception {
                when(usersService.getUserByNameAndLastName("Иван", "Чухманов")).thenReturn(List.of(testResponse));
                mockMvc.perform(get("/manager/api/users").param("firstName",
                                "Иван").param("lastName", "Чухманов"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].email").value("test@test.com"))
                        .andExpect(jsonPath("$[0].firstName").value("Иван"))
                        .andExpect(jsonPath("$[0].lastName").value("Чухманов"))
                        .andExpect(jsonPath("$[0].role").value("USER"));
                verify(usersService, times(1)).getUserByNameAndLastName("Иван", "Чухманов");
            }
        //==========================================================
        // получение пользователя по role"
        //==========================================================
            @Test
            @DisplayName("GET /manager/api/users?role={role} - получение пользователя по role")
            void getUsersByRole_Return200() throws Exception {
                when(usersService.getUserByRole("USER")).thenReturn(List.of(testResponse));
                mockMvc.perform(get("/manager/api/users").param("role", "USER"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].email").value("test@test.com"))
                        .andExpect(jsonPath("$[0].firstName").value("Иван"))
                        .andExpect(jsonPath("$[0].lastName").value("Чухманов"))
                        .andExpect(jsonPath("$[0].role").value("USER"));
                verify(usersService, times(1)).getUserByRole("USER");
            }
        //==========================================================
        // Получение всех пользователей - успешный сценарий status 200
        //==========================================================
            @Test
            @DisplayName("GET /manager/api/users")
            void getAllUsers_Return200() throws Exception {
                when(usersService.getAllUser()).thenReturn(List.of(testResponse));
                mockMvc.perform(get("/manager/api/users"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].email").value("test@test.com"))
                        .andExpect(jsonPath("$[0].firstName").value("Иван"))
                        .andExpect(jsonPath("$[0].lastName").value("Чухманов"))
                        .andExpect(jsonPath("$[0].role").value("USER"));
                verify(usersService, times(1)).getAllUser();
            }
        //==========================================================
        // получение пользователя по email
        //==========================================================
            @Test
            @DisplayName("GET /manager/api/users/email/{email} - успешное получение")
            void getUserByEmail_Return200() throws Exception {
                when(usersService.getUserByEmail("test@test.com")).thenReturn(testResponse);
                mockMvc.perform(get("/manager/api/users/email/{email}", "test@test.com"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value("test@test.com"))
                        .andExpect(jsonPath("$.firstName").value("Иван"))
                        .andExpect(jsonPath("$.lastName").value("Чухманов"))
                        .andExpect(jsonPath("$.role").value("USER"));
                verify(usersService, times(1)).getUserByEmail("test@test.com");
            }
        //==========================================================
        // успешное создание пользователя
        //==========================================================
            @Test
            @DisplayName("POST /manager/api/users - успешное создание пользователя")
            void createUser_Return201() throws Exception {
                when(usersService.createUser(testRequest)).thenReturn(testResponse);
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                //
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.email").value("test@test.com"))
                        .andExpect(jsonPath("$.firstName").value("Иван"))
                        .andExpect(jsonPath("$.lastName").value("Чухманов"))
                        .andExpect(jsonPath("$.role").value("USER"));
                verify(usersService, times(1)).createUser(any(UserRequestDTO.class));
            }
        //==========================================================
        // Обновление данных пользователя
        //==========================================================
            @Test
            @DisplayName("PUT /manager/api/users/{id} Обновление данных пользователя")
            void updateUserById_Return200() throws Exception {
                when(usersService.updateUserById(CORRECT_ID, testRequest)).thenReturn(testResponse);
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value("test@test.com"))
                        .andExpect(jsonPath("$.firstName").value("Иван"))
                        .andExpect(jsonPath("$.lastName").value("Чухманов"))
                        .andExpect(jsonPath("$.role").value("USER"));
                verify(usersService, times(1)).updateUserById(CORRECT_ID, testRequest);
            }
        //==========================================================
        // ТЕСТ 8: Удаление пользователя по id
        //==========================================================
            @Test
            @DisplayName("DELETE /manager/api/users/{id} Удаление пользователя по id")
            void deleteUserById() throws Exception {
                doNothing().when(usersService).deleteUser(CORRECT_ID);
                mockMvc.perform(delete("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isNoContent());
                verify(usersService, times(1)).deleteUser(CORRECT_ID);
            }

        }
        @Nested
        @DisplayName("Ошибка клиента (4хх)")
        class ClientErrorTests{
            //==========================================================
            // GET /manager/api/users/{id}
            //==========================================================
            @Test
            @DisplayName("400 - ID в виде букв")
            void getUserById_InvalidIdFormat_Return400() throws Exception{
                mockMvc.perform(get("/manager/api/users/{id}", "abc"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserById(CORRECT_ID);
            }
            //==========================================================
            // GET /manager/api/users/{id}
            //==========================================================
            @Test
            @DisplayName("400 - ID отрицательное значение")
            void getUserById_NotPositiveId_Return400() throws Exception {
                mockMvc.perform(get("/manager/api/users/{id}", -1L))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserById(CORRECT_ID);
            }
            //==========================================================
            // GET /manager/api/users/{id}
            //==========================================================
            @Test
            @DisplayName("400 - ID является нулем")
            void getUserById_ZeroId_Return400() throws Exception{
                mockMvc.perform(get("/manager/api/users/{id}", 0L))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserById(CORRECT_ID);
            }
            @Test
            @DisplayName("404 - пользователь с данным id не найден")
            void getUserById_NotFound_Return404() throws Exception{
                when(usersService.getUserById(1000L))
                        .thenThrow(new ResourceNotFoundException("Пользователь", 1000L));
                mockMvc.perform(get("/manager/api/users/{id}", 1000L))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Пользователь с id 1000 не найден"));
                verify(usersService, times(1)).getUserById(1000L);
            }
            @Test
            @DisplayName("400 - невалидная роль")
            void getUsers_InvalidRole_Return400() throws Exception{
                mockMvc.perform(get("/manager/api/users").param("role", "SUPER_ADMIN"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserByRole(anyString());
            }
            @Test
            @DisplayName("400 - нарушение размера имени")
            void getUser_InvalidSizeName_Return400()throws Exception{
                mockMvc.perform(get("/manager/api/users").param("firstName",
                        "aa")
                        .param("lastName", "Чухманов"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserByNameAndLastName(anyString(), anyString());
            }
            @Test
            @DisplayName("400 - нарушение размера фамилии")
            void getUser_InvalidSizeLastName_Return400()throws Exception{
                mockMvc.perform(get("/manager/api/users").param("firstName",
                                        "Иван")
                                .param("lastName", "Чу"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserByNameAndLastName(anyString(), anyString());
            }
            //==========================================================
            // Получение не валидных данных от пользователя POST /manager/api/users,
            //==========================================================

            @Test
            @DisplayName("400 - нарушение размера пароля")
            void createUser_InvalidPassword_Return400() throws Exception{
                //
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "22",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                //
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(any(UserRequestDTO.class));
            }
            @Test
            @DisplayName("400 - нарушение формата email")
            void createUser_InvalidEmail_Return400() throws Exception{
                String requestJson = """
                        {
                            "email": "testtest.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                //
                mockMvc.perform(post("/manager/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(any(UserRequestDTO.class));
            }
            @Test
            @DisplayName("409 - нарушение уникальности email")
            void createUser_DuplicateEmail_Return409() throws Exception{
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                when(usersService.createUser(testRequest)).thenThrow(DuplicateResourceException.class);
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isConflict());
                verify(usersService, times(1)).createUser(testRequest);
            }
            @Test
            @DisplayName("400 - имя некорректного размера")
            void createUser_InvalidFirstNameSize_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Ив",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }
            @Test
            @DisplayName("400 - фамилия некорректного размера")
            void createUser_InvalidLastNameSize_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чу",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }
            @Test
            @DisplayName("400 - некорректное поле role")
            void createUser_InvalidRole_Return400() throws Exception{
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "U"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }
            @Test
            @DisplayName("400 - поле role является null")
            void createUser_RoleIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": null
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }


            @Test
            @DisplayName("400 - поле email является null")
            void createUser_EmailIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": null,
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "ADMIN"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }

            @Test
            @DisplayName("400 - Имя является null")
            void createUser_FirstNameIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": null,
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }

            @Test
            @DisplayName("400 - поле role является null")
            void createUser_LastNameIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": null,
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }
            @Test
            @DisplayName("400 - поле role является null")
            void createUser_PasswordIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": null,
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).createUser(testRequest);
            }
            //==========================================================
            // ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО EMAIL
            //==========================================================
            @Test
            @DisplayName("400 - Некорректный email")
            void getUserByEmail_InvalidEmail_Return400() throws Exception {
                mockMvc.perform(get("/manager/api/users/email/{email}", "@@@"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).getUserByEmail(anyString());
            }
            @Test
            @DisplayName("404 - Некорректный email is null")
            void getUserByEmail_NotValidEmail_Return404() throws Exception {
                mockMvc.perform(get("/manager/api/users/email/{email}", ""))
                        .andDo(print())
                        .andExpect(status().isNotFound());
                verify(usersService, never()).getUserByEmail(anyString());
            }
            @Test
            @DisplayName("404 - Пользователя с email нету в базе")
            void getUserByEmail_EmailNotFound_Return404() throws Exception {
                when(usersService.getUserByEmail("test@test.com")).thenThrow(UserByEmailNotFoundException.class);
                mockMvc.perform(get("/manager/api/users/email/{email}", "test@test.com"))
                        .andDo(print())
                        .andExpect(status().isNotFound());
                verify(usersService, times(1)).getUserByEmail("test@test.com");
            }
            //==========================================================
            // ОБРАБОТКА КОНТРОЛЕРА PUT /manager/api/users/{id}
            // Обновление пользователя по id
            //==========================================================
            // 1) Пользователь может быть не найден 404
            @Test
            @DisplayName("404 - пользователь с id не найден")
            void updateUserById_NotFound_Return404() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "h222222222222",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                when(usersService.updateUserById(anyLong(), any(UserRequestDTO.class)))
                        .thenThrow(new ResourceNotFoundException("Пользователь", 999L));
                mockMvc.perform(put("/manager/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Пользователь с id 999 не найден"));
                verify(usersService, times(1)).updateUserById(anyLong(), any(UserRequestDTO.class));
            }
            // 2 проверки на корректность введенных данных в id
            @Test
            @DisplayName("400 - Id отрицательное")
            void updateUserById_NotPositiveId_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "h2222222222222",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", -10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(anyLong(),any(UserRequestDTO.class));
            }
            // 3 Проверка на id = 0 при обновлении
            @Test
            @DisplayName("400 - Id является 0")
            void updateUserById_ZeroId_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "h22222222222222",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", 0)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(anyLong(),any(UserRequestDTO.class));
            }
            // 4 Проверка валидности входных данных ( Проверка корректности firstName, LastName, Password, role, email)

            @Test
            @DisplayName("400 - невалидный email")
            void updateUserById_EmailIsNotValid_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "@@@@@",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(anyLong(),any(UserRequestDTO.class));
            }

            @Test
            @DisplayName("400 - email is null")
            void updateUserById_EmailIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": null,
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(anyLong(),any(UserRequestDTO.class));
            }
            // Если email которые мы пытаемся обновить уже есть в базе 409 status
            @Test
            @DisplayName("409 - email который пытаемся обновить уже есть в базе")
            void updateUserById_EmailDuplicate_Return409() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                when(usersService.updateUserById(CORRECT_ID, testRequest))
                        .thenThrow(new DuplicateResourceException("Пользователь с данным email уже существует"));
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value("Пользователь с данным email уже существует"));

                verify(usersService, times(1)).updateUserById(anyLong(), any(UserRequestDTO.class));
            }

            @Test
            @DisplayName("400 - firstName is null")
            void updateUserById_FirstNameIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": null,
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }

            @Test
            @DisplayName("400 - firstName меньше 3 символов")
            void updateUserById_FirstNameIsNotValid_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Ив",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }

            @Test
            @DisplayName("400 - lastName is null")
            void updateUserById_LastNameIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": null,
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }

            @Test
            @DisplayName("400 - lastName меньше 3 символов")
            void updateUserById_LastNameIsNotValid_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чу",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }

            // Пароль не может быть null если null получим 400 status
            @Test
            @DisplayName("400 - password is null")
            void updateUserById_PasswordIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": null,
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }
            // Пароль должен быть минимум из 10 символов иначе 400 status ошибка валидации
            @Test
            @DisplayName("400 - password меньше 10 символов")
            void updateUserById_PasswordIsNotValid_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "USER"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }

            // Роль не должна быть null
            @Test
            @DisplayName("400 - role is null")
            void updateUserById_RoleIsNull_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": null
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }
            // Роль должна проходить под pattern USER|ADMIN|MANAGER проверка валидности данных
            @Test
            @DisplayName("400 - не валидная role")
            void updateUserById_RoleIsNotValid_Return400() throws Exception {
                String requestJson = """
                        {
                            "email": "test@test.com",
                            "password": "password123",
                            "firstName": "Иван",
                            "lastName": "Чухманов",
                            "role": "SUPER_ADMIN"
                        }
                        """;
                mockMvc.perform(put("/manager/api/users/{id}", CORRECT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).updateUserById(CORRECT_ID, testRequest);
            }

            // Проверка контролера DELETE /manager/api/users/{id}
            @Test
            @DisplayName("400 - ID пользователя отрицательное")
            void deleteUserById_NotPositiveId_Return400() throws Exception {
                mockMvc.perform(delete("/manager/api/users/{id}", -1L))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, never()).deleteUser(anyLong());
            }
            @Test
            @DisplayName("404 - Пользователя нету в БД")
            void deleteUserById_UserNotFound_Return404() throws Exception {
                doThrow(new ResourceNotFoundException("Пользователь", 999L))
                        .when(usersService).deleteUser(999L);
                mockMvc.perform(delete("/manager/api/users/{id}", 999L))
                        .andDo(print())
                        .andExpect(status().isNotFound());
                verify(usersService, times(1)).deleteUser(999L);
            }
            @Test
            @DisplayName("400 - пользователь является создателем задачи")
            void deleteUserById_UserIsCreator_Return400()throws Exception{
                doThrow(new BadRequestException("Нельзя удалить пользователя, который создавал задачи"))
                        .when(usersService).deleteUser(CORRECT_ID);
                mockMvc.perform(delete("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, times(1)).deleteUser(CORRECT_ID);
            }
            @Test
            @DisplayName("400 - пользователь является исполнителем задачи")
            void deleteUserById_UserIsAssignee_Return400()throws Exception{
                doThrow(new BadRequestException("Нельзя удалить пользователя, который является исполнителем задачи"))
                        .when(usersService).deleteUser(CORRECT_ID);
                mockMvc.perform(delete("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, times(1)).deleteUser(CORRECT_ID);
            }
            @Test
            @DisplayName("400 - пользователь является создателем проекта")
            void deleteUserById_UserIsCreatorProject_Return400() throws Exception{
                doThrow(new BadRequestException("Нельзя удалить пользователя, который является создателем проекта"))
                        .when(usersService).deleteUser(CORRECT_ID);
                mockMvc.perform(delete("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, times(1)).deleteUser(CORRECT_ID);
            }
            @Test
            @DisplayName("400 - пользователь является участником проекта")
            void deleteUserById_UserIsParticipation_Return400() throws Exception{
                doThrow(new BadRequestException("Нельзя удалить пользователя участвующего в проекте"))
                        .when(usersService).deleteUser(CORRECT_ID);
                mockMvc.perform(delete("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
                verify(usersService, times(1)).deleteUser(CORRECT_ID);
            }

        }
        @Nested
        @DisplayName("Ошибки сервера (5хх)")
        class ServerErrorTests{
            @Test
            @DisplayName("500 - Неожиданная ошибка при создании пользователя")
            void createUser_InternalServerError_Return500() throws Exception {
                String requestJson = """
                {
                    "email": "test@test.com",
                    "password": "password123",
                    "firstName": "Иван",
                    "lastName": "Чухманов",
                    "role": "USER"
                }
                """;
                when(usersService.createUser(any(UserRequestDTO.class)))
                        .thenThrow(new RuntimeException("Unexpected database error"));

                mockMvc.perform(post("/manager/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.message").value("Ошибка сервера"));
            }
            @Test
            @DisplayName("500 - Неожиданная ошибка при получении пользователя по id")
            void getUserById_InternalServerError_Return500() throws Exception {
                when(usersService.getUserById(CORRECT_ID))
                        .thenThrow(new RuntimeException("Unexpected database error"));

                mockMvc.perform(get("/manager/api/users/{id}", CORRECT_ID))
                        .andDo(print())
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.message").value("Ошибка сервера"));
            }
        }
}
