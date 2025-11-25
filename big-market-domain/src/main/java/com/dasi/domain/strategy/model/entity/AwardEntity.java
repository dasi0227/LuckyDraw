package com.dasi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AwardEntity {

    /** 奖品ID */
    private Integer awardId;

    /** 奖品类型 */
    private String awardName;

    /** 奖品信息 */
    private String awardConfig;

    /** 奖品描述 */
    private String awardDesc;

}
