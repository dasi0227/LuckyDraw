package com.dasi.domain.behavior.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaveContext {

    private String userId;

    private Long behaviorId;

    private String bizId;

}
