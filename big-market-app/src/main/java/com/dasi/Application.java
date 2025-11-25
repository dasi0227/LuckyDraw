package com.dasi;

import cn.bugstack.middleware.db.router.config.DataSourceAutoConfig;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configurable
@EnableScheduling
@Import(DataSourceAutoConfig.class)
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
