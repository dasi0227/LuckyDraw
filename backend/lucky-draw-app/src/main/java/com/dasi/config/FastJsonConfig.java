package com.dasi.config;

import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class FastJsonConfig {

    @PostConstruct
    public void init() {

        // 1. LocalDateTime 序列化器
        ObjectSerializer localDateTimeSerializer = (serializer, object, fieldName, fieldType, features) -> {
            if (object == null) {
                serializer.writeNull();
                return;
            }
            LocalDateTime time = (LocalDateTime) object;
            serializer.write(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        };

        // 2. 放入全局配置
        SerializeConfig.getGlobalInstance().put(LocalDateTime.class, localDateTimeSerializer);
    }
}