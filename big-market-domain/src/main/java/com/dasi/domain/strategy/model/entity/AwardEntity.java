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
    private Long awardId;

    /** 奖品名称 */
    private String awardName;

    /** 奖品描述 */
    private String awardDesc;

    /** 奖品配置 */
    private String awardConfig;

}
