package com.dasi.domain.strategy.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRuleWeightDetail {

    private Integer userScore;

    private Integer prevWeight;

    private Integer nextWeight;

    private List<String> awardNameList;

}
