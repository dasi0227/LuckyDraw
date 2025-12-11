package com.dasi.domain.strategy.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityLuckResult {

    private Integer accountLuck;

    private Integer prevLuck;

    private Integer nextLuck;

    private List<String> awardNameList;

}
