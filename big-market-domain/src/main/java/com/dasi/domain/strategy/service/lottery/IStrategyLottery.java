package com.dasi.domain.strategy.service.lottery;

public interface IStrategyLottery {

    Integer doLottery(Long strategyId);

    Integer doLottery(Long strategyId, String ruleWeight);

}
