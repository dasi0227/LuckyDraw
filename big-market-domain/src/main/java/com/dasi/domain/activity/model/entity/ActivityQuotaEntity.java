package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityQuotaEntity {

    /** 定量id */
    private Long activityQuotaId;

    /** 总次数 */
    private Integer totalCount;

    /** 每月次数 */
    private Integer monthCount;

    /** 每日次数 */
    private Integer dayCount;

}
