package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RaffleAward {

    /** 自增id */
    private Long id;

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 策略id */
    private Long strategyId;

    /** 订单id */
    private String orderId;

    /** 奖品id */
    private Long awardId;

    /** 奖品标题 */
    private String awardName;

    /** 中奖时间 */
    private LocalDateTime awardTime;

    /** 奖品发放状态 */
    private String awardState;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
