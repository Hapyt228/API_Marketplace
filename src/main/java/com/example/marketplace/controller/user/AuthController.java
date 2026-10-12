package com.example.marketplace.controller.user;

import com.example.marketplace.dto.user.AuthResponse;
import com.example.marketplace.dto.user.LoginRequest;
import com.example.marketplace.dto.user.RegisterRequest;
import com.example.marketplace.service.user.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Valid
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
           @RequestBody RegisterRequest registerRequest
    ) {
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User added");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest loginRequest
    ) {
        AuthResponse authResponse = authService.login(loginRequest);
        return  ResponseEntity.status(HttpStatus.OK)
                .body(authResponse);
    }
}
