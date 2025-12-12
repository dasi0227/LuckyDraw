package com.dasi.domain.award.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributeContext {

    private String userId;

    private Long activityId;

    private String orderId;

    private Long awardId;

}
