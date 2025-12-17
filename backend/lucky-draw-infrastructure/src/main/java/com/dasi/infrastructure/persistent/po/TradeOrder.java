package com.dasi.infrastructure.persistent.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TradeOrder {

    private Long id;

    private String orderId;

    private String bizId;

    private String userId;

    private Long tradeId;

    private Long activityId;

    private String tradeType;

    private String tradeState;

    private LocalDateTime tradeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}