package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryActivityAccountResponse {

    private Integer accountPoint;

    private Integer totalSurplus;

    private Integer monthSurplus;

    private Integer daySurplus;

    private Integer monthPending;

    private Integer dayPending;

}
