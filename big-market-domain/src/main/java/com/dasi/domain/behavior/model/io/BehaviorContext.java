package com.dasi.domain.behavior.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorContext {

    private String userId;

    private String businessNo;

    private List<Long> behaviorIds;

}
