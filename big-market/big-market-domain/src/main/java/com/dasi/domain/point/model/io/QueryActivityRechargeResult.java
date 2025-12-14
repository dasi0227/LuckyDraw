package com.dasi.domain.point.model.io;

import com.dasi.domain.point.model.type.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityRechargeResult {

    private Long tradeId;

    private TradeType tradeType;

    private Integer tradePoint;

    private String tradeMoney;

    private String tradeValue;

    private String tradeName;

    private String tradeDesc;

}
