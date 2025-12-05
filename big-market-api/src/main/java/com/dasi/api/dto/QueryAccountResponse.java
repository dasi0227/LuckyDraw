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

    private String monthKey;
    private String dayKey;
    private Integer monthLimit;
    private Integer dayLimit;
    private Integer totalAllocate;
    private Integer totalSurplus;
    private Integer monthAllocate;
    private Integer monthSurplus;
    private Integer dayAllocate;
    private Integer daySurplus;

}
