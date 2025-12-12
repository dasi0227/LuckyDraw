package com.dasi.domain.strategy.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryActivityLuckResult {

    private Integer accountLuck;

    private Map<String, List<String>> luckThreshold;

}
