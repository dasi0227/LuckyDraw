package com.dasi.domain.strategy.service.raffle;


import com.dasi.domain.strategy.model.dto.RaffleRequestDTO;
import com.dasi.domain.strategy.model.dto.RaffleResponseDTO;

public interface IRaffle {

    RaffleResponseDTO doRaffle(RaffleRequestDTO raffleRequestDTO);

}
