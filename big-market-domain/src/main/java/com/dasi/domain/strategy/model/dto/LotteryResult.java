package com.dasi.domain.strategy.model.dto;

import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryResult {

    private Integer awardId;

    private String awardName;

    private String awardConfig;

    private String awardDesc;

    public static LotteryResult build(Integer awardId, IStrategyRepository strategyRepository) {
        AwardEntity awardEntity = strategyRepository.queryAwardEntityByAwardId(awardId);
        return LotteryResult.builder()
                .awardId(awardId)
                .awardName(awardEntity.getAwardName())
                .awardConfig(awardEntity.getAwardConfig())
                .awardDesc(awardEntity.getAwardDesc())
                .build();
    }
}
