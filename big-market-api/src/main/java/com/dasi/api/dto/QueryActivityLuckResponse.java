package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryActivityLuckResponse {

    private Integer accountLuck;

    private Integer prevLuck;

    private Integer nextLuck;

    private List<String> awardNameList;

}
