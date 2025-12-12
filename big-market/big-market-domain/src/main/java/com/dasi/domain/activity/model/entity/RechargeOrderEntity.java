package com.dasi.domain.activity.model.entity;

import com.dasi.domain.activity.model.type.RechargeState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeOrderEntity {

    /** 订单id */
    private String orderId;

    /** 业务幂等id */
    private String bizId;

    /** 活动id */
    private Long activityId;

    /** 用户id */
    private String userId;

    /** 库存id */
    private Long skuId;

    /** 本次下单获得的次数 */
    private Integer count;

    /** 订单状态 */
    private RechargeState rechargeState;

    /** 下单时间 */
    private LocalDateTime rechargeTime;

}
