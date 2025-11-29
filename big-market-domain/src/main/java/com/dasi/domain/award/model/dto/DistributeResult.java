package com.dasi.domain.award.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributeResult {

    private Integer awardId;

    private String awardName;

    private String messageId;

}
