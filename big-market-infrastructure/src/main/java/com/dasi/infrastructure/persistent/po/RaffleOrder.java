package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RaffleOrder {

    /** 自增id */
    private Long id;

    /** 订单id */
    private String orderId;

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 策略id */
    private Long strategyId;

    /** 订单状态 */
    private String raffleOrderState;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
