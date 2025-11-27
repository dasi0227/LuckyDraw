package com.dasi.domain.strategy.service.lottery;

import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

public interface IStrategyLottery {

    LotteryResult doStrategyLottery(LotteryContext lotteryContext);

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    Integer getLotteryAward(Long strategyId);

    Integer getLotteryAward(Long strategyId, String ruleWeight);

}
