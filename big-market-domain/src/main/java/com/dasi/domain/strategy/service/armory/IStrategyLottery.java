package com.dasi.domain.strategy.service.armory;

public interface IStrategyLottery {

    Integer doLottery(Long strategyId);

    Integer doLottery(Long strategyId, String ruleWeight);

}
