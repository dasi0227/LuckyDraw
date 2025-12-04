package com.dasi.domain.activity.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeResult {

    private String orderId;

    private Integer count;

}