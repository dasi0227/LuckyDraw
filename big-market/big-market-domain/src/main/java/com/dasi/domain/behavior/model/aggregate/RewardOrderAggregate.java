package com.dasi.domain.behavior.model.aggregate;

import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardOrderAggregate {

    private String userId;

    private RewardOrderEntity rewardOrderEntity;

    private TaskEntity taskEntity;

}
