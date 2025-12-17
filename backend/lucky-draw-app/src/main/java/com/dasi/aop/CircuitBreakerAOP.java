package com.dasi.aop;

import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.annotation.CircuitBreaker;
import com.dasi.types.annotation.DCCValue;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Aspect
public class CircuitBreakerAOP {

    @DCCValue("circuitBreakerEnable:off")
    private String circuitBreakerEnable;

    @DCCValue("circuitBreakerThreshold:5")
    private String circuitBreakerThreshold;

    @DCCValue("circuitBreakerWindowTime:30")
    private String circuitBreakerWindowTime;

    @DCCValue("circuitBreakerOpenTime:20")
    private String circuitBreakerOpenTime;

    @Resource
    private IRedisService redisService;

    @Around("@annotation(circuitBreaker)")
    public Object around(ProceedingJoinPoint joinPoint, CircuitBreaker circuitBreaker) throws Throwable {

        if ("off".equalsIgnoreCase(circuitBreakerEnable)) {
            return joinPoint.proceed();
        }

        String method = joinPoint.getSignature().getDeclaringTypeName() + Delimiter.COLON + joinPoint.getSignature().getName();
        String openKey = RedisKey.CIRCUIT_BREAKER_OPEN_KEY + method;
        String failKey = RedisKey.CIRCUIT_BREAKER_FAIL_KEY + method;

        if (redisService.isExists(openKey)) {
            log.warn("【熔断】已开启，fallback 生效：method={}", method);
            return invokeFallback(joinPoint, circuitBreaker.fallbackMethod());
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            // 业务异常不计入熔断失败次数
            if (ex instanceof BusinessException) {
                throw ex;
            }
            handleFailure(failKey, openKey);
            throw ex;
        }
    }

    private void handleFailure(String failKey, String openKey) {
        int threshold = parseInt(circuitBreakerThreshold, 5);
        int windowSec = parseInt(circuitBreakerWindowTime, 30);
        int openSec = parseInt(circuitBreakerOpenTime, 20);

        long count;
        count = redisService.incr(failKey);
        if (count == 1L) {
            redisService.expire(failKey, windowSec * 1000L);
        }

        if (count >= threshold) {
            redisService.setValue(openKey, "open", openSec * 1000L);
            log.warn("【熔断】触发打开：failKey={}, count={}, threshold={}, openSec={}", failKey, count, threshold, openSec);
        }
    }

    private int parseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private Object invokeFallback(ProceedingJoinPoint jp, String fallbackMethod) throws Throwable {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        Method method = jp.getTarget()
                .getClass()
                .getMethod(fallbackMethod, ms.getParameterTypes());
        try {
            return method.invoke(jp.getTarget(), jp.getArgs());
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
