package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt", ignoreInvalidFields = true)
public class JwtProperties {

    private String secret;

    private String issuer;

    private Long ttl;

    private String header;

    private String prefix;

}
