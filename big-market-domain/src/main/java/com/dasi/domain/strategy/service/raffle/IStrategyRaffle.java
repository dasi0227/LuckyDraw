package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.dto.RaffleContext;
import com.dasi.domain.strategy.model.dto.RaffleResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

public interface IStrategyRaffle {

    RaffleResult doRaffle(RaffleContext raffleContext);

    List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId);

}
