package com.dasi.domain.user.service.auth;

import com.dasi.domain.user.model.entity.UserEntity;
import com.dasi.domain.user.model.io.LoginContext;
import com.dasi.domain.user.model.io.LoginResult;
import com.dasi.domain.user.model.io.RegisterContext;
import com.dasi.domain.user.model.io.RegisterResult;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.user.service.token.IJwtService;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.exception.AppException;
import com.dasi.types.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserAuth implements IUserAuth {

    @Resource
    private IUserRepository userRepository;

    @Resource
    private IJwtService tokenService;

    @Override
    public RegisterResult doRegister(RegisterContext registerContext) {

        // 1. 参数校验
        String userId = registerContext.getUserId();
        String password = registerContext.getPassword();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (StringUtils.isBlank(password)) throw new AppException("缺少参数 password");

        // 2. 用户重复校验
        UserEntity userEntity = userRepository.queryUserByUserId(userId);
        if (userEntity != null) {
            throw new BusinessException(ExceptionMessage.USERID_ALREADY_EXISTS);
        }

        // 3. 密码加密
        String encryptedPassword = DigestUtils.md5Hex(password);

        // 4. 创建用户
        userRepository.createUser(UserEntity.builder().userId(userId).password(encryptedPassword).build());

        // 5. 生成 token
        String token = tokenService.generateToken(userId);
        return RegisterResult.builder().userId(userId).token(token).build();
    }

    @Override
    public LoginResult doLogin(LoginContext loginContext) {

        // 1. 参数校验
        String userId = loginContext.getUserId();
        String password = loginContext.getPassword();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (StringUtils.isBlank(password)) throw new AppException("缺少参数 password");

        // 2. 查询用户
        UserEntity userEntity = userRepository.queryUserByUserId(userId);
        if (userEntity == null) {
            throw new BusinessException(ExceptionMessage.USER_NOT_EXISTS);
        }

        // 3. 校验密码
        String encryptedPassword = DigestUtils.md5Hex(password);
        if (!encryptedPassword.equals(userEntity.getPassword())) {
            throw new BusinessException(ExceptionMessage.USER_PASSWORD_ERROR);
        }

        // 4. 生成 token
        String token = tokenService.generateToken(userId);
        return LoginResult.builder().userId(userId).token(token).build();
    }

}
