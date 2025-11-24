package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardListResponseDTO {

    /** 奖品ID */
    private Integer awardId;

    /** 奖品标题 */
    private String awardTitle;

    /** 奖品副标题 */
    private String awardSubTitle;

    /** 奖品排序 */
    private Integer sort;

}
