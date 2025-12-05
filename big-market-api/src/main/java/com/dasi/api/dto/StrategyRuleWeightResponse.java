package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleWeightResponse {

    private Integer userScore;

    private Integer prevWeight;

    private Integer nextWeight;

    private List<String> awardNameList;

}
