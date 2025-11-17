package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.LowerCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginUserRequest {

    @NotBlank(message = "must be provided")
    @Email
    @LowerCase(message = "email must be lower case")
    private String email;

    @NotBlank(message = "must be provided")
    @Size(min = 6, max = 16, message = "password must be in the range 6..16")
    private String password;
}
