package com.example.marketplace.service.user;

import com.example.marketplace.dto.user.AuthResponse;
import com.example.marketplace.dto.user.LoginRequest;
import com.example.marketplace.dto.user.RegisterRequest;
import com.example.marketplace.entity.user.Role;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.repository.user.UserRepository;
import com.example.marketplace.security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

    }

    public void register(RegisterRequest registerRequest ) {
        if( userRepository.findByEmail(registerRequest.getEmail()).isPresent() ) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest loginRequest){
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new NoSuchElementException("Not found by email = " + loginRequest.getEmail()));
        if(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())){
            return new AuthResponse(jwtUtil.generateToken(user));
        } else {
            throw new IllegalArgumentException("Wrong password");
        }

    }

}
