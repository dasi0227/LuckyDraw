package com.dasi.domain.behavior.model.aggregate;

import com.dasi.domain.behavior.model.entity.BehaviorOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorOrderAggregate {

    private String userId;

    private BehaviorOrderEntity behaviorOrderEntity;

    private TaskEntity taskEntity;

}
