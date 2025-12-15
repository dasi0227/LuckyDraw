package com.dasi.trigger.http.Interceptor;

import com.dasi.properties.JwtProperties;
import com.dasi.infrastructure.common.JwtService;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.context.UserIdContext;
import com.dasi.types.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("all")
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ExceptionMessage.TOKEN_ERROR);
        }
        if (StringUtils.isNotBlank(jwtProperties.getPrefix()) && token.startsWith(jwtProperties.getPrefix())) {
            token = token.substring(jwtProperties.getPrefix().length()).trim();
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
