package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IStrategyRuleDao {
    StrategyRule queryStrategyRuleByRuleModel(StrategyRule strategyRuleRequest);
}
