package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.io.RaffleContext;
import com.dasi.domain.strategy.model.io.RaffleResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

public interface IRaffle {

    RaffleResult doRaffle(RaffleContext raffleContext);

    List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId);

}
