package com.dasi.domain.trade.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConvertResult {

    private Long tradeId;

    private Integer tradePoint;

    private String tradeName;

}
