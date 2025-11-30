package com.dasi.domain.strategy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryResult {

    private Long awardId;

    private String awardName;

    private String awardConfig;

}
