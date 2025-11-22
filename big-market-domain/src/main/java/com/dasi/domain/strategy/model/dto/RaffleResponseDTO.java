package com.dasi.domain.strategy.model.dto;


import com.dasi.domain.strategy.model.entity.AwardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleResponseDTO {

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


    // ---------------------- 静态方法 ----------------------
    public static RaffleResponseDTO buildAward(Long strategyId, AwardEntity awardEntity) {
        return RaffleResponseDTO.builder()
                .strategyId(strategyId)
                .awardId(awardEntity.getAwardId())
                .awardKey(awardEntity.getAwardKey())
                .awardConfig(awardEntity.getAwardConfig())
                .awardDesc(awardEntity.getAwardDesc())
                .build();
    }

}
