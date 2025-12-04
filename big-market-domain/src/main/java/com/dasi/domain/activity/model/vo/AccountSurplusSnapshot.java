package com.dasi.domain.activity.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSurplusSnapshot {

    // 来自总账户
    Integer totalSurplus;
    // 来自月账户
    Integer monthSurplus;
    // 来自日账户
    Integer daySurplus;

}
