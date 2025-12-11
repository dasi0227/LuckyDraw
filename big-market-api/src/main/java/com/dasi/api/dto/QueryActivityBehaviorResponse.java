package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryActivityBehaviorResponse {

    private String behaviorType;

    private String behaviorName;

    private Boolean isDone;

}
