package com.example.marketplace.security;

import com.example.marketplace.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.isTokenValid(token)){
            filterChain.doFilter(request, response);
            return;
        }
        String email = jwtUtil.extractEmail(token);
        userRepository.findByEmail(email).ifPresent(user -> {
            if (SecurityContextHolder.getContext().getAuthentication() == null){
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                Authentication authentication  = new  UsernamePasswordAuthenticationToken(user,null, Collections.singletonList(simpleGrantedAuthority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
           }
        });
        filterChain.doFilter(request, response);
    }
}
