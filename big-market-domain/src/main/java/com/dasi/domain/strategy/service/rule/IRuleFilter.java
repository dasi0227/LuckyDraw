package com.dasi.domain.strategy.service.rule;


import com.dasi.domain.strategy.model.entity.RuleResultEntity;
import com.dasi.domain.strategy.model.entity.RuleContextEntity;

public interface IRuleFilter<T extends RuleResultEntity.RuleDataEntity> {

    RuleResultEntity<T> filter(RuleContextEntity ruleContextEntity);

}
