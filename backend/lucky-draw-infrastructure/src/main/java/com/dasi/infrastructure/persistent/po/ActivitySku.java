package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySku {

    /** 自增id */
    private Long id;

    /** 库存id */
    private Long skuId;

    /** 活动id */
    private Long activityId;

    /** 定量id */
    private Integer count;

    /** 库存分配 */
    private Integer stockAllocate;

    /** 库存剩余 */
    private Integer stockSurplus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
