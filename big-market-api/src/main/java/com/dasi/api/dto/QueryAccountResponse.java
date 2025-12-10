package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryAccountResponse {

    private Integer userPoint;

    private Integer totalSurplus;

    private Integer monthSurplus;

    private Integer daySurplus;

    private Integer monthRecharge;

    private Integer dayRecharge;

}
