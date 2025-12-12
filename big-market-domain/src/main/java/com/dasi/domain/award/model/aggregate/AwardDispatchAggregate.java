package com.dasi.domain.award.model.aggregate;

import com.dasi.domain.award.model.entity.ActivityAccountEntity;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.AwardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardDispatchAggregate {

    private String userId;

    private Long awardId;

    private String orderId;

    private Long activityId;

    private AwardEntity awardEntity;

    private ActivityAwardEntity activityAwardEntity;

    private ActivityAccountEntity activityAccountEntity;

}
