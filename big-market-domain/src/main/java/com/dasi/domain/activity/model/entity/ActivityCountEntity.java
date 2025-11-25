package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCountEntity {

    private Long activityCountId;
    private Integer totalCount;
    private Integer dayCount;
    private Integer monthCount;

}
