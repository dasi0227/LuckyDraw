package com.dasi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.Advisor;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AopTest {

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void check() {
        Object bean = applicationContext.getBean("bigMarketController");
        System.out.println("beanClass = " + bean.getClass());
        System.out.println("isAopProxy = " + AopUtils.isAopProxy(bean));
        System.out.println("isJdkProxy = " + AopUtils.isJdkDynamicProxy(bean));
        System.out.println("isCglibProxy = " + AopUtils.isCglibProxy(bean));
    }

    @Test
    public void diagnose() throws Exception {
        // 1) 看 AOP 基础设施是否存在（没有它就不会创建任何代理）
        String[] apc = applicationContext.getBeanNamesForType(
                org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator.class
        );
        System.out.println("AutoProxyCreator count = " + apc.length);
        for (String name : apc) {
            System.out.println("AutoProxyCreator = " + name);
        }

        // 2) 看 RateLimit 注解在运行时是否“真的存在”在方法上（Retention 不是 RUNTIME 会直接 false）
        java.lang.reflect.Method m = com.dasi.trigger.http.controller.BigMarketController.class
                .getMethod("raffle", com.dasi.api.dto.RaffleRequest.class);
        System.out.println("implMethod has @RateLimit = " +
                m.isAnnotationPresent(com.dasi.types.annotation.RateLimit.class));

        java.lang.reflect.Method im = com.dasi.api.IBigMarketService.class
                .getMethod("raffle", com.dasi.api.dto.RaffleRequest.class);
        System.out.println("interfaceMethod has @RateLimit = " +
                im.isAnnotationPresent(com.dasi.types.annotation.RateLimit.class));

        // 3) 看 Advisor 是否被注册（有 Aspect 不等于有 Advisor 生效）
        java.util.Map<String, org.springframework.aop.Advisor> advisors =
                applicationContext.getBeansOfType(org.springframework.aop.Advisor.class);
        System.out.println("Advisor count = " + advisors.size());
    }

    @Test
    public void dumpAdvisorsAndMatch() {
        Object controller = applicationContext.getBean("bigMarketController");

        System.out.println("beanClass = " + controller.getClass());
        System.out.println("isAopProxy = " + AopUtils.isAopProxy(controller));

        Map<String, Advisor> advisors = applicationContext.getBeansOfType(Advisor.class);
        System.out.println("Advisor bean count = " + advisors.size());

        int matchCount = 0;
        for (Map.Entry<String, Advisor> e : advisors.entrySet()) {
            String name = e.getKey();
            Advisor advisor = e.getValue();

            if (advisor instanceof PointcutAdvisor) {
                PointcutAdvisor pa = (PointcutAdvisor) advisor;
                boolean classMatch = pa.getPointcut().getClassFilter().matches(controller.getClass());
                boolean methodMatch = pa.getPointcut().getMethodMatcher()
                        .matches(findRaffleMethod(controller), controller.getClass());

                if (classMatch && methodMatch) {
                    matchCount++;
                    System.out.println("[MATCH] " + name + " -> " + pa);
                }
            } else {
                System.out.println("[Advisor] " + name + " -> " + advisor.getClass().getName());
            }
        }

        System.out.println("Matched advisors = " + matchCount);
    }

    private java.lang.reflect.Method findRaffleMethod(Object controller) {
        try {
            // 注意：这里用实现类方法签名
            return controller.getClass().getMethod("raffle", com.dasi.api.dto.RaffleRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
