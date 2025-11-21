package com.dasi.domain.strategy.service.raffle;


import com.dasi.domain.strategy.model.io.RaffleRequest;
import com.dasi.domain.strategy.model.io.RaffleResponse;

public interface IRaffle {

    RaffleResponse doRaffle(RaffleRequest raffleRequest);

}
