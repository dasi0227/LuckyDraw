package com.dasi.trigger.http.Interceptor;

import com.dasi.infrastructure.common.JwtService;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.context.UserIdContext;
import com.dasi.types.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("all")
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${jwt.header}")
    private String headerToken;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    @Resource
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(headerToken);
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ExceptionMessage.TOKEN_ERROR);
        }
        if (StringUtils.isNotBlank(tokenPrefix) && token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length()).trim();
        }
        try {
            String userId = jwtService.parseUserId(token);
            UserIdContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ExceptionMessage.TOKEN_ERROR);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserIdContext.clear();
    }
}
