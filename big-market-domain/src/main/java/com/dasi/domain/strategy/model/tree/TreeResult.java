package com.dasi.domain.strategy.model.tree;

import com.dasi.domain.strategy.model.enumeration.RuleCheckResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeResult {

    private RuleCheckResult ruleCheckResult;

    private Integer awardId;

    private String ruleModel;

}
