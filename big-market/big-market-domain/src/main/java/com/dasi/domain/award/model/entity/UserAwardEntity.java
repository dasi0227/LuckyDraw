package com.dasi.domain.award.model.entity;

import com.dasi.domain.award.model.type.AwardSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAwardEntity {

    /** 订单ID */
    private String orderId;

    /** 用户ID */
    private String userId;

    /** 奖品ID */
    private Long awardId;

    /** 活动ID */
    private Long activityId;

    /** 奖品来源 */
    private AwardSource awardSource;

    /** 奖品名称 */
    private String awardName;

    /** 奖品描述 */
    private String awardDesc;

    /** 奖品期限 */
    private LocalDateTime awardDeadline;

    /** 奖品描述 */
    private LocalDateTime awardTime;

}
