package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.config")
public class AppConfigProperties {

    private String crossOrigin;

    private String apiVersion;

}
