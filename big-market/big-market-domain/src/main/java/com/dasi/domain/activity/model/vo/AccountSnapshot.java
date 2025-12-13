package com.dasi.domain.activity.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSnapshot {

    // 来自总账户
    Integer totalSurplus;
    Integer totalAllocate;
    Integer accountPoint;
    Integer accountLuck;
    // 来自月账户
    Integer monthLimit;
    Integer monthSurplus;
    Integer monthAllocate;
    // 来自日账户
    Integer dayLimit;
    Integer daySurplus;
    Integer dayAllocate;

}
