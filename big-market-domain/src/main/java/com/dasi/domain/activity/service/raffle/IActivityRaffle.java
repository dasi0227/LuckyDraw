package com.dasi.domain.activity.service.raffle;

import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.dto.RaffleResult;

public interface  IActivityRaffle {

    RaffleResult doActivityRaffle(RaffleContext raffleContext);

}
