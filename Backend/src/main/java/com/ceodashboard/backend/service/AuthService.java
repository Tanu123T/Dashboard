package com.ceodashboard.backend.service;

import com.ceodashboard.backend.dto.request.LoginRequest;
import com.ceodashboard.backend.entity.User;

public interface AuthService {

    String register(User user);

    String login(LoginRequest request);
}