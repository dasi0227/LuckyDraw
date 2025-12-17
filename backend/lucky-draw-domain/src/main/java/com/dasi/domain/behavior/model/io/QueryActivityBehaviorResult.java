package com.dasi.domain.behavior.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityBehaviorResult {

    private String behaviorType;

    private String behaviorName;

    private String rewardDesc;

    private Boolean isDone;

}
