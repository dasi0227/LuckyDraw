package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountResponseDTO {

    private Integer totalAllocate;
    private Integer totalSurplus;
    private Integer monthAllocate;
    private Integer monthSurplus;
    private Integer dayAllocate;
    private Integer daySurplus;

}
