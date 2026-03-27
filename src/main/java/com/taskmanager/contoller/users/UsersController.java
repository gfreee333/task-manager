package com.taskmanager.contoller.users;
import com.taskmanager.dto.request.users.UserRequestDTO;
import com.taskmanager.dto.response.users.UserResponseDTO;
import com.taskmanager.service.users.UsersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/manager/api/users")
@Validated // Включаю валидацию на уровне @PathVariable
public class UsersController {
    private final UsersService usersService;
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }
    //Todo 1: Получение пользователя по Id  // Готово
    // GET /manager/api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable @Positive Long id){
        return ResponseEntity.ok(usersService.getUserById(id));
    }

    // Todo 2: 2.1 Получить информацию о всех пользователях   // Готово
    //         2.2 Получение пользователя по role
    //         2.3 Получение пользователя по firstName и lastName
    //         GET /manager/api/users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers(
        @RequestParam(required = false)
        @Pattern(regexp = "USER|ADMIN|MANAGER",
                message = "Роль должна быть: USER, ADMIN или MANAGER") String role,
        @RequestParam(required = false)
        @Size(min = 3, max = 50,
                message = "Имя должно быть от 3 до 50 символов") String firstName,
        @RequestParam(required = false)
        @Size(min = 3, max = 50,
                message = "Фамилия должно быть от 3 до 50 символов") String lastName){
        // Поиск по имени и фамилии
        if(firstName != null && lastName != null) {
            return ResponseEntity.ok(usersService.getUserByNameAndLastName(firstName, lastName));
        }
        // Поиск по роли пользователя
        if(role != null) {
            return ResponseEntity.ok(usersService.getUserByRole(role));
        }
        return ResponseEntity.ok(usersService.getAllUser());
    }

    //Todo 3: Создание нового пользователя // Готово
    // POST /manager/api/users
    @PostMapping
    public ResponseEntity<UserResponseDTO> createdUser(@Valid @RequestBody UserRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.createUser(request));
    }

    //Todo 4: Получение пользователя по email // Готово
    // GET /manager/api/users/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@Email @NotNull @PathVariable String email){
        return ResponseEntity.ok(usersService.getUserByEmail(email));
    }

    // todo 5. Обновление данных пользователя  // Готово
    //  PUT /manager/api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUsersById(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UserRequestDTO request){
        return ResponseEntity.ok(usersService.updateUserById(id, request));
    }

    // todo 6. Удаление пользователя с проверкой связи // Готово
    //  DELETE /manager/api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsersById(@PathVariable @Positive Long id){
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
