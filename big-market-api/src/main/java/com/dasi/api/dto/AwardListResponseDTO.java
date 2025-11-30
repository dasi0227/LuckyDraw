package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AwardListResponseDTO {

    /** 奖品ID */
    private Long awardId;

    /** 奖品名称 */
    private String awardName;

    /** 奖品标题 */
    private String awardTitle;

    /** 奖品描述 */
    private String awardDesc;

    /** 奖品配置 */
    private String awardConfig;

    /** 奖品中奖概率 */
    private BigDecimal awardRate;

    /** 奖品排列序号 */
    private Integer awardIndex;

    /** 解锁次数 */
    private Integer limitLotteryCount;

    /** 等待次数 */
    private Integer needLotteryCount;

    /** 是否解锁 */
    private Boolean isLock;

}
