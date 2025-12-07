package com.dasi.domain.award.model.entity;

import java.time.LocalDateTime;

public class UserAwardEntity {

    /** 订单ID */
    private String orderId;

    /** 用户ID */
    private String userId;

    /** 奖品ID */
    private String awardId;

    /** 奖品类型 */
    private String awardType;

    /** 奖品名称 */
    private String awardName;

    /** 奖品描述 */
    private String awardDesc;

    /** 奖品描述 */
    private LocalDateTime awardTime;

}
