package com.codewithmosh.store.users;

import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers(
            @RequestParam(required = false, defaultValue = "", name = "sort") String sort
    ) {
        return userService.findAll(sort);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            // 1. DTO di Richiesta: Accetta i dati, inclusi quelli sensibili (p.e. password).
            @Valid @RequestBody RegisterUserRequest request
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    public UserDto updateUser(
            @RequestBody UpdateUserRequest request,
            @PathVariable Long id
            )
    {
        return userService.update(request, id);
    }

    @DeleteMapping("/{id}")
    public UserDto deleteUser(
            @PathVariable Long id
    )
    {
        return userService.delete(id);
    }

    @PostMapping("/{id}/change-password")
    public void changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
    )
    {
        userService.changePassword(id, request);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDenied(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Void> handleUserNotFoundException(){
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ErrorDto> handleUserAlreadyExistsException(UserAlreadyExists ex){
        return ResponseEntity.badRequest().body(new ErrorDto(ex.getMessage()));
    }


}
