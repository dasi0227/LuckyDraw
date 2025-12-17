package com.dasi.trigger.http.controller;

import com.dasi.api.IUserService;
import com.dasi.api.dto.LoginRequest;
import com.dasi.api.dto.RegisterRequest;
import com.dasi.api.dto.LoginResponse;
import com.dasi.api.dto.RegisterResponse;
import com.dasi.domain.user.model.io.LoginContext;
import com.dasi.domain.user.model.io.LoginResult;
import com.dasi.domain.user.model.io.RegisterContext;
import com.dasi.domain.user.model.io.RegisterResult;
import com.dasi.domain.user.service.auth.IUserAuth;
import com.dasi.types.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/lucky-draw/auth")
public class UserController implements IUserService {

    @Resource
    private IUserAuth userAuth;

    /**
     * 注册
     *
     * @param registerRequest userId, password
     * @return userId, token
     */
    @PostMapping("/register")
    @Override
    public Result<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {

        RegisterContext registerContext = RegisterContext.builder().userId(registerRequest.getUserId()).password(registerRequest.getPassword()).build();
        RegisterResult registerResult = userAuth.doRegister(registerContext);
        RegisterResponse registerResponse = RegisterResponse.builder().userId(registerResult.getUserId()).token(registerResult.getToken()).build();
        return Result.success(registerResponse);

    }

    /**
     * 登录
     * @param loginRequest userId, password
     * @return userId, token
     */
    @PostMapping("/login")
    @Override
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        LoginContext loginContext = LoginContext.builder().userId(loginRequest.getUserId()).password(loginRequest.getPassword()).build();
        LoginResult loginResult = userAuth.doLogin(loginContext);
        LoginResponse loginResponse = LoginResponse.builder().userId(loginResult.getUserId()).token(loginResult.getToken()).build();

        return Result.success(loginResponse);
    }

}
