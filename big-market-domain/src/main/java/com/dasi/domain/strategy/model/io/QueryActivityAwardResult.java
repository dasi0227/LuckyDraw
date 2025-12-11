package com.dasi.domain.strategy.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityAwardResult {

    /** 奖品ID */
    private Long awardId;

    /** 奖品名称 */
    private String awardName;

    /** 奖品中奖概率 */
    private BigDecimal awardRate;

    /** 奖品排列序号 */
    private Integer awardIndex;

    /** 等待次数 */
    private Integer needLotteryCount;

    /** 是否解锁 */
    private Boolean isLock;

}
