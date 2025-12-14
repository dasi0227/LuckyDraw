package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryActivityRechargeResponse {

    private Long tradeId;

    private String tradeMoney;

    private String tradeValue;

    private String tradeName;

}
