package com.dasi.domain.activity.model.aggregate;

import com.dasi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.dasi.domain.activity.model.entity.RaffleOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleOrderAggregate {

    private String userId;

    private Long activityId;

    private ActivityAccountEntity activityAccountEntity;

    private ActivityAccountMonthEntity activityAccountMonthEntity;

    private ActivityAccountDayEntity activityAccountDayEntity;

    private RaffleOrderEntity raffleOrderEntity;

}
