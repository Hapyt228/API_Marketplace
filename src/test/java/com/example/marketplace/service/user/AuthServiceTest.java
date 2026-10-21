package com.example.marketplace.service.user;

import com.example.marketplace.dto.user.LoginRequest;
import com.example.marketplace.dto.user.RegisterRequest;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.exception.EmailAlreadyExistsException;
import com.example.marketplace.exception.InvalidCredentialsException;
import com.example.marketplace.repository.user.UserRepository;
import com.example.marketplace.security.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JWTUtil jwtUtil;
    @InjectMocks AuthService authService;

    private RegisterRequest register(String email) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword("password123");
        return r;
    }

    private LoginRequest login(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    @Test
    void register_existingEmail_throwsConflict() {
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> authService.register(register("a@test.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(login("a@test.com", "password123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_wrongPassword_throwsSameException() {
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("hash");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(login("a@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}