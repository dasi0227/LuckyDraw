package com.dasi.domain.point.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeContext {

    private String userId;

    private Long tradeId;

    private String businessNo;

}
