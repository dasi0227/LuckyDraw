package com.dasi.domain.behavior.model.io;

import com.dasi.domain.behavior.model.type.BehaviorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorContext {

    private String userId;

    private Long activityId;

    private BehaviorType behaviorType;

    private String businessNo;

}
