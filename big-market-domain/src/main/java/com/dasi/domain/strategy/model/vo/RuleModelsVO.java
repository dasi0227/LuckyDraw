package com.dasi.domain.strategy.model.vo;

import com.dasi.domain.strategy.service.rule.factory.RuleFactory;
import com.dasi.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleModelsVO {

    private String ruleModels;

    public String[] getDuringRuleModels() {
        if (ruleModels == null || ruleModels.trim().isEmpty()) {
            return new String[0];
        }

        List<String> duringRuleModelList = new ArrayList<>();

        for (String ruleModel : ruleModels.split(Constants.COMMA)) {
            if (RuleFactory.RuleModel.isDuring(ruleModel)) {
                duringRuleModelList.add(ruleModel);
            }
        }

        return duringRuleModelList.toArray(new String[0]);
    }

    public String[] getBeforeRuleModels() {
        if (ruleModels == null || ruleModels.trim().isEmpty()) {
            return new String[0];
        }

        List<String> duringRuleModelList = new ArrayList<>();

        for (String ruleModel : ruleModels.split(Constants.COMMA)) {
            if (RuleFactory.RuleModel.isBefore(ruleModel)) {
                duringRuleModelList.add(ruleModel);
            }
        }

        return duringRuleModelList.toArray(new String[0]);
    }
}
