package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAward {

    /** 自增ID */
    private Long id;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
    
}
