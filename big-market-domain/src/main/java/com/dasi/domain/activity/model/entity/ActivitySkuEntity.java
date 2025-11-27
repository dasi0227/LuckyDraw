package com.dasi.domain.activity.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySkuEntity {

    /** 库存id */
    private Long skuId;

    /** 活动id */
    private Long activityId;

    /** 定量id */
    private Long activityQuotaId;

    /** 库存分配 */
    private Integer stockAllocate;

    /** 库存剩余 */
    private Integer stockSurplus;

}
