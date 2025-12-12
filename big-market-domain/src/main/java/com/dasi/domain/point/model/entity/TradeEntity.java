package com.dasi.domain.point.model.entity;

import com.dasi.domain.point.model.type.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {

    private Long tradeId;

    private Long activityId;

    private TradeType tradeType;

    private Integer tradePoint;

    private String tradeValue;

    private String tradeName;

    private String tradeDesc;

}
