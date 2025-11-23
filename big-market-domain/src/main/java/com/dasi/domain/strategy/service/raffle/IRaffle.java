package com.dasi.domain.strategy.service.raffle;


import com.dasi.domain.strategy.model.dto.RaffleRequest;
import com.dasi.domain.strategy.model.dto.RaffleResponse;

public interface IRaffle {

    RaffleResponse doRaffle(RaffleRequest raffleRequest);

}
