package com.dasi.domain.strategy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAwardStock {

    private Long strategyId;

    private Integer awardId;

}
