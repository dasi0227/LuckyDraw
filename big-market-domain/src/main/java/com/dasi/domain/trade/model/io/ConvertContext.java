package com.dasi.domain.trade.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertContext {

    private String userId;

    private String orderId;

    private Long tradeId;

    private String businessNo;

    private Long activityId;

}
