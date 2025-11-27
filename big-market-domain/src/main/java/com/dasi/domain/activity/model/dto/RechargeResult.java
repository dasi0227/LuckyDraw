package com.dasi.domain.activity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeResult {

    private String userId;

    private String orderId;

    private Integer totalCount;

    private Integer monthCount;

    private Integer dayCount;

}