package com.dasi.domain.strategy.model.entity;


import com.dasi.domain.strategy.repository.IStrategyRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleResponseEntity {

    /** 策略 ID */
    private Long strategyId;

    /** 奖品 ID */
    private Integer awardId;

    /** 奖品类型 */
    private String awardKey;

    /** 奖品熟悉 */
    private String awardConfig;

    /** 奖品描述 */
    private String awardDesc;

    public static RaffleResponseEntity buildAward(Long strategyId, Integer awardId, IStrategyRepository repository) {
        AwardEntity awardEntity = repository.queryAwardEntityByAwardId(awardId);
        return RaffleResponseEntity.builder()
                .strategyId(strategyId)
                .awardId(awardEntity.getAwardId())
                .awardKey(awardEntity.getAwardKey())
                .awardConfig(awardEntity.getAwardConfig())
                .awardDesc(awardEntity.getAwardDesc())
                .build();
    }

}
