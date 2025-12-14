package com.dasi.domain.user.service.auth;

import com.dasi.domain.user.model.io.LoginContext;
import com.dasi.domain.user.model.io.LoginResult;
import com.dasi.domain.user.model.io.RegisterContext;
import com.dasi.domain.user.model.io.RegisterResult;

public interface IUserAuth {

    RegisterResult doRegister(RegisterContext registerContext);

    LoginResult doLogin(LoginContext loginContext);

}
