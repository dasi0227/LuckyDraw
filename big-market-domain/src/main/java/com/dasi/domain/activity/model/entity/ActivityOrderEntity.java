package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityOrderEntity {

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
    private String orderState;

    /** 下单时间 */
    private LocalDateTime orderTime;

}
