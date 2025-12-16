package com.dasi.aop;

import com.dasi.context.UserIdContext;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.annotation.DCCValue;
import com.dasi.types.annotation.RateLimit;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Aspect
@Slf4j
public class RateLimitAOP {

    @DCCValue("enableRateLimit:off")
    private String enableRateLimit;

    @DCCValue("rateLimitUserQPS:5")
    private String rateLimitUserQPS;

    @DCCValue("rateLimitApiQPS:20")
    private String rateLimitApiQPS;

    @Resource
    private IRedisService redisService;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        if ("off".equalsIgnoreCase(enableRateLimit)) {
            return joinPoint.proceed();
        }

        double apiQpsVal = parseDouble(rateLimitApiQPS);
        double userQpsVal = parseDouble(rateLimitUserQPS);

        String apiKey = RedisKey.RATE_LIMIT_KEY + joinPoint.getSignature().getName();
        String userKey = apiKey + Delimiter.COLON + UserIdContext.getUserId();


        // 接口级限流
        if (!tryAcquire(apiKey, apiQpsVal)) {
            log.warn("【限流】接口级触发 fallback：apiKey={}", apiKey);
            return invokeFallback(joinPoint, rateLimit.fallbackMethod());
        }

        // 用户级限流
        if (!tryAcquire(userKey, userQpsVal)) {
            log.warn("【限流】用户级触发 fallback：userKey={}", userKey);
            return invokeFallback(joinPoint, rateLimit.fallbackMethod());
        }

        return joinPoint.proceed();

    }

    private boolean tryAcquire(String key, double qps) {
        if (qps <= 0) {
            return true;
        }

        long ratePerSecond = Math.max(1L, Math.round(Math.ceil(qps)));

        RRateLimiter limiter = redisService.getRateLimiter(key);
        RateLimiterConfig config = limiter.getConfig();
        if (config == null) {
            limiter.trySetRate(RateType.OVERALL, ratePerSecond, 1, RateIntervalUnit.SECONDS);
        } else if (config.getRate() != ratePerSecond) {
            limiter.setRate(RateType.OVERALL, ratePerSecond, 1, RateIntervalUnit.SECONDS);
        }
        return limiter.tryAcquire();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0D;
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
