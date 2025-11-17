package com.codewithmosh.store.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be less then 255 characters")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @LowerCase(message = "email must be in lowercase")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min =6, max = 25, message = "password must be at least 6 and max 25 characters")
    private String password;
}
