package com.dasi.domain.activity.model.dto;

import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuOrder {

    private String userId;

    private Long activityId;

    private Integer totalCount;

    private Integer dayCount;

    private Integer monthCount;

    private ActivityOrderEntity activityOrderEntity;

}
