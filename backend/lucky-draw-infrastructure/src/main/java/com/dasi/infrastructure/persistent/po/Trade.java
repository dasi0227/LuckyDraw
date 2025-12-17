package com.dasi.infrastructure.persistent.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Trade {

    private Long id;

    private Long tradeId;

    private Long activityId;

    private String tradeType;

    private Integer tradePoint;

    private String tradeMoney;

    private String tradeValue;

    private String tradeName;

    private String tradeDesc;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
