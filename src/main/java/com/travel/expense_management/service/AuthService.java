package com.travel.expense_management.service;

import com.travel.expense_management.dto.auth.AuthResponse;
import com.travel.expense_management.dto.auth.LoginRequest;
import com.travel.expense_management.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
