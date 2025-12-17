package com.dasi.domain.activity.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityAccountResult {

    private Integer accountPoint;

    private Integer totalSurplus;

    private Integer monthSurplus;

    private Integer daySurplus;

    private Integer monthPending;

    private Integer dayPending;

}
