package com.dasi.config;

import com.dasi.aop.CircuitBreakerAOP;
import com.dasi.aop.RateLimitAOP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig {

    @Bean
    public RateLimitAOP rateLimitAOP() {
        return new RateLimitAOP();
    }

    @Bean
    public CircuitBreakerAOP circuitBreakerAOP() {
        return new CircuitBreakerAOP();
    }

}
