package com.dasi.factory;

import com.dasi.properties.ZooKeeperConfigProperties;
import com.dasi.types.annotation.DCCValue;
import com.dasi.types.constant.Delimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class DCCValueBeanFactory implements BeanPostProcessor {

    private final CuratorFramework curatorFramework;

    private final ZooKeeperConfigProperties properties;

    private final Map<String, Object> dccObjMap = new ConcurrentHashMap<>();

    public DCCValueBeanFactory(CuratorFramework curatorFramework, ZooKeeperConfigProperties properties) throws Exception {

        this.curatorFramework = curatorFramework;
        this.properties = properties;

        // 确保配置根节点存在
        if (this.curatorFramework.checkExists().forPath(properties.getConfigPath()) == null) {
            this.curatorFramework.create().creatingParentsIfNeeded().forPath(properties.getConfigPath());
        }

        // 建立本地缓存，统一监听子节点变更
        CuratorCache curatorCache = CuratorCache.build(this.curatorFramework, properties.getConfigPath());
        curatorCache.start();

        // 注册监听的回调
        curatorCache.listenable().addListener(((type, oldData, newData) -> {

            // 1. 获取新的配置
            String key = newData.getPath();
            String value = new String(newData.getData(), StandardCharsets.UTF_8);

            switch (type) {
                case NODE_CHANGED:

                    // 2. 从映射表中找到使用该配置的 Bean
                    Object objBean = dccObjMap.get(key);
                    if (objBean == null) {
                        return;
                    }

                    // 3. 处理 AOP 代理，拿到真实类
                    Class<?> clazz = objBean.getClass();
                    if (AopUtils.isAopProxy(objBean)) {
                        clazz = AopUtils.getTargetClass(objBean);
                    }

                    try {
                        // 4. 根据节点名反射找到字段，实现热更新
                        Field field = clazz.getDeclaredField(key.substring(key.lastIndexOf(Delimiter.SLASH) + 1));
                        field.setAccessible(true);
                        field.set(objBean, value);
                        field.setAccessible(false);

                        log.info("【配置】DCC 配置节点值：key={}, value={}", key, value);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    break;
                case NODE_CREATED:
                    break;
                case NODE_DELETED:
                    break;
                default:
                    break;
            }

        }));

    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        // 获取注册的类和实例
        Class<?> clazz = bean.getClass();
        Object object = bean;
        if (AopUtils.isAopProxy(bean)) {
            clazz = AopUtils.getTargetClass(bean);
            object = AopProxyUtils.getSingletonTarget(bean);
        }

        // 检查所有字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 检查字段是否有 DCCValue 注解
            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            if (dccValue != null) {

                // 拿到并解析注解的值
                String dccValueConfig = dccValue.value();
                if (StringUtils.isBlank(dccValueConfig)) {
                    throw new RuntimeException(field.getName() + " 的值为空");
                }
                String[] splits = dccValueConfig.split(Delimiter.COLON);
                if (splits.length != 2 || StringUtils.isBlank(splits[0]) || StringUtils.isBlank(splits[1])) {
                    throw new RuntimeException(field.getName() + " 的值配置不正确");
                }

                // 构造 key，如果此前注册过就使用存储的值，否则使用默认值
                try {
                    String key = properties.getConfigPath() + Delimiter.SLASH + splits[0];
                    String value = curatorFramework.checkExists().forPath(key) == null ? splits[1] : new String(curatorFramework.getData().forPath(key));

                    // 创建节点
                    if (curatorFramework.checkExists().forPath(key) == null) {
                        curatorFramework.create().creatingParentsIfNeeded().forPath(key, value.getBytes(StandardCharsets.UTF_8));
                    }

                    field.setAccessible(true);
                    field.set(object, value);
                    field.setAccessible(false);
                    log.info("【配置】DCC 配置节点值：key={}, value={}", key, value);

                    // 保存这个实例
                    dccObjMap.put(key, object);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // 不能返回解代理后的对象，否则会把原有代理链（含 AOP）剥离掉
        return bean;
    }
}
