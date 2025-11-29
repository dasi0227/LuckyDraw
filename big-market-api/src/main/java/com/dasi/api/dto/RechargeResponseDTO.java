package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RechargeResponseDTO {

    private String orderId;

    private Integer totalCount;

    private Integer monthCount;

    private Integer dayCount;

}
