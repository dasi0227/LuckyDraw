package com.dasi.config;

import com.dasi.aop.CircuitBreakerAspect;
import com.dasi.aop.RateLimitAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig {

    @Bean
    public RateLimitAspect rateLimitAOP() {
        return new RateLimitAspect();
    }

    @Bean
    public CircuitBreakerAspect circuitBreakerAOP() {
        return new CircuitBreakerAspect();
    }

}
