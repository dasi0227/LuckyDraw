package com.dasi.domain.activity.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryAccountResult {

    private Integer totalAllocate;

    private Integer totalSurplus;

    private String monthKey;

    private Integer monthLimit;

    private Integer monthAllocate;

    private Integer monthSurplus;

    private String dayKey;

    private Integer dayLimit;

    private Integer dayAllocate;

    private Integer daySurplus;

}
