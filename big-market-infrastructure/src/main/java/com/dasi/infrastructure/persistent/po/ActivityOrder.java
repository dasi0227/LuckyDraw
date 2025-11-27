package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityOrder {

    /** 自增id */
    private Long id;

    /** 订单id */
    private String orderId;

    /** 业务幂等id */
    private String bizId;

    /** 活动id */
    private Long activityId;

    /** 定量id */
    private Long activityQuotaId;

    /** 策略id */
    private Long strategyId;

    /** 用户id */
    private String userId;

    /** 库存id */
    private Long skuId;

    /** 本次下单获得的总次数 */
    private Integer totalCount;

    /** 本次下单获得的月次数 */
    private Integer monthCount;

    /** 本次下单获得的日次数 */
    private Integer dayCount;

    /** 订单状态 */
    private String activityOrderState;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}