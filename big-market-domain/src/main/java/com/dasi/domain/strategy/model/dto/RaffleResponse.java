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
public class RaffleResponse {

    /** 奖品 ID */
    private Integer awardId;

    /** 奖品类型 */
    private String awardKey;

    /** 奖品信息 */
    private String awardConfig;

    /** 奖品描述 */
    private String awardDesc;


    // ---------------------- 静态方法 ----------------------
    public static RaffleResponse buildAward(AwardEntity awardEntity) {
        return RaffleResponse.builder()
                .awardId(awardEntity.getAwardId())
                .awardKey(awardEntity.getAwardKey())
                .awardConfig(awardEntity.getAwardConfig())
                .awardDesc(awardEntity.getAwardDesc())
                .build();
    }

}
