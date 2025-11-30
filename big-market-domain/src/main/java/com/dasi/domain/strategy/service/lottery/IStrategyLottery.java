package com.dasi.domain.strategy.service.lottery;

import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;

public interface IStrategyLottery {

    LotteryResult doStrategyLottery(LotteryContext lotteryContext);

    Integer getLotteryAward(Long strategyId);

    Integer getLotteryAward(Long strategyId, String ruleWeight);

}
