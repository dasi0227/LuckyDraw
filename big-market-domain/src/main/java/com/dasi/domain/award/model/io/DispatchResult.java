package com.dasi.domain.award.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchResult {

    /** 奖品类型 */
    private String awardType;

    /** 奖品名称 */
    private String awardName;

}
