package com.dasi.domain.strategy.service.raffle;


import com.dasi.domain.strategy.model.entity.RaffleRequestEntity;
import com.dasi.domain.strategy.model.entity.RaffleResponseEntity;

public interface IRaffle {

    RaffleResponseEntity doRaffle(RaffleRequestEntity raffleRequestEntity);

}
