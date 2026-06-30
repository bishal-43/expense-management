package com.travel.expense_management.service.Impl;

import com.travel.expense_management.dto.auth.AuthResponse;
import com.travel.expense_management.dto.auth.LoginRequest;
import com.travel.expense_management.dto.auth.RegisterRequest;
import com.travel.expense_management.entity.Role;
import com.travel.expense_management.entity.User;
import com.travel.expense_management.exception.DuplicateEmailException;
import com.travel.expense_management.exception.InvalidCredentialsException;
import com.travel.expense_management.repository.UserRepository;
import com.travel.expense_management.security.JwtService;
import com.travel.expense_management.security.UserPrincipal;
import com.travel.expense_management.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional()
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateEmail(request.email());

        User user = buildUser(request);

        User savedUser = userRepository.save(user);
        
        String token = jwtService.generateToken(UserPrincipal.from(savedUser));

        return AuthResponse.of(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
                );
    }


    public AuthResponse login(LoginRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        String token = jwtService.generateToken(UserPrincipal.from(user));

        return AuthResponse.of(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }


    private void validateEmail(String email){
        if(userRepository.existsByEmail(email)){
            throw new DuplicateEmailException(email);
        }
    }


    private User buildUser(RegisterRequest request){
        Role assignedRole = Role.Employee;
        if (request.role() != null && !request.role().isBlank()) {
            try {
                assignedRole = Role.valueOf(request.role());
            } catch (IllegalArgumentException e) {
                // Fallback to Employee
            }
        }
        return User.builder()
                .fullName(request.fullname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(assignedRole)
                .build();
    }
}
