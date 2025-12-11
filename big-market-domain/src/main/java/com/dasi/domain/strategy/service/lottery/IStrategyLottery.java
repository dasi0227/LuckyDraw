package com.dasi.domain.strategy.service.lottery;

import com.dasi.domain.strategy.model.io.LotteryContext;
import com.dasi.domain.strategy.model.io.LotteryResult;

public interface IStrategyLottery {

    LotteryResult doStrategyLottery(LotteryContext lotteryContext);

    Long getLotteryAward(Long strategyId);

    Long getLotteryAward(Long strategyId, String luck);

}
