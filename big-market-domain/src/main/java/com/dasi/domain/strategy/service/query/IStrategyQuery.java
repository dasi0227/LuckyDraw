package com.dasi.domain.strategy.service.query;

import com.dasi.domain.strategy.model.io.ActivityAwardDetail;
import com.dasi.domain.strategy.model.io.StrategyRuleWeightDetail;

import java.util.List;

public interface IStrategyQuery {

    List<ActivityAwardDetail> queryActivityAward(String userId, Long activityId);

    StrategyRuleWeightDetail queryStrategyRuleWeight(String userId, Long activityId);

}
