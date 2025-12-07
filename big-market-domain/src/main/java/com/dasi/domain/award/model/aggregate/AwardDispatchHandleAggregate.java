package com.dasi.domain.award.model.aggregate;

import com.dasi.domain.award.model.entity.AwardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardDispatchHandleAggregate {

    private String userId;

    private Long awardId;

    private String orderId;

    private Integer userPoint;

    private AwardEntity awardEntity;

}
