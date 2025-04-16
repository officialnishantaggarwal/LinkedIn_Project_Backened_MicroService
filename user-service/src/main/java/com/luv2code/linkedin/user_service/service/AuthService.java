package com.luv2code.linkedin.user_service.service;

import com.luv2code.linkedin.user_service.dto.LoginRequestDto;
import com.luv2code.linkedin.user_service.dto.SignupRequestDto;
import com.luv2code.linkedin.user_service.dto.UserDto;

public interface AuthService {
    UserDto signup(SignupRequestDto signupRequestDto);

    String login(LoginRequestDto loginRequestDto);
}
