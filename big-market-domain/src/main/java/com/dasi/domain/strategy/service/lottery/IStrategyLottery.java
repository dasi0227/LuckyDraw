package com.dasi.domain.strategy.service.lottery;

import com.dasi.domain.strategy.model.dto.StrategyLotteryContext;
import com.dasi.domain.strategy.model.dto.StrategyLotteryResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

public interface IStrategyLottery {

    StrategyLotteryResult doStrategyLottery(StrategyLotteryContext strategyLotteryContext);

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    Integer getLotteryAward(Long strategyId);

    Integer getLotteryAward(Long strategyId, String ruleWeight);

}
