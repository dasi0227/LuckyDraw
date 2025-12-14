package com.dasi.api;

import com.dasi.api.dto.LoginRequest;
import com.dasi.api.dto.RegisterRequest;
import com.dasi.api.dto.LoginResponse;
import com.dasi.api.dto.RegisterResponse;
import com.dasi.types.model.Result;

@SuppressWarnings("unused")
public interface IUserService {

    Result<RegisterResponse> register(RegisterRequest registerRequest);

    Result<LoginResponse> login(LoginRequest loginRequest);

}
