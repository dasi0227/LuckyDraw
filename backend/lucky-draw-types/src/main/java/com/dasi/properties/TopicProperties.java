package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq.topic", ignoreInvalidFields = true)
public class TopicProperties {

    private String rechargeSkuStockEmpty;

    private String dispatchActivityAward;

    private String dispatchBehaviorReward;

    private String dispatchTradeOutcome;

}
