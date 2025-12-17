package com.dasi.domain.activity.model.entity;

import com.dasi.domain.activity.model.type.RaffleState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleOrderEntity {

    /** 订单id */
    private String orderId;

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 策略id */
    private Long strategyId;

    /** 订单状态 */
    private RaffleState raffleState;

    /** 下单时间 */
    private LocalDateTime raffleTime;

}
