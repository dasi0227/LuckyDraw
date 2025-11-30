package com.dasi.domain.strategy.service.lottery;

import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;

public interface IStrategyLottery {

    LotteryResult doStrategyLottery(LotteryContext lotteryContext);

    Long getLotteryAward(Long strategyId);

    Long getLotteryAward(Long strategyId, String ruleWeight);

}
