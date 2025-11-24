package com.dasi.domain.strategy.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage {

    private Long strategyId;

    private Integer awardId;

}
