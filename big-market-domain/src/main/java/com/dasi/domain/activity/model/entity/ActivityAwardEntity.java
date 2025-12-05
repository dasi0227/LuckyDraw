package com.dasi.domain.activity.model.entity;

import com.dasi.domain.activity.model.type.AwardState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAwardEntity {

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 订单id */
    private String orderId;

    /** 奖品id */
    private Long awardId;

    /** 奖品标题 */
    private String awardName;

    /** 中奖时间 */
    private LocalDateTime awardTime;

    /** 奖品发放状态 */
    private AwardState awardState;

}
