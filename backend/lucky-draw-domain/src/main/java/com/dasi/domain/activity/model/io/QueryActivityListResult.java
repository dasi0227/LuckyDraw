package com.dasi.domain.activity.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityListResult {

    private Long activityId;

    private String activityName;

    private String activityDesc;

}
