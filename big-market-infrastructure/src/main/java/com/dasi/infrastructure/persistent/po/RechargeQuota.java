package com.dasi.infrastructure.persistent.po;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RechargeQuota {

    /** 自增id */
    private Long id;

    /** 定量id */
    private Long quotaId;

    /** 总次数 */
    private Integer totalCount;

    /** 每月次数 */
    private Integer monthCount;

    /** 每日次数 */
    private Integer dayCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
