package com.dasi.domain.behavior.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchContext {

    private String userId;

    private String orderId;

    private Long awardId;

}
