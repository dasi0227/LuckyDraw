package com.dasi.domain.award.model.aggregate;

import com.dasi.domain.award.model.entity.ActivityAccountEntity;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.entity.UserAwardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchHandleAggregate {

    private String userId;

    private Long awardId;

    private String orderId;

    private Long activityId;

    private Integer accountPoint;

    private AwardEntity awardEntity;

    private ActivityAwardEntity activityAwardEntity;

    private UserAwardEntity userAwardEntity;

    private ActivityAccountEntity activityAccountEntity;

}
