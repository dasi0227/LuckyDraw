package com.dasi.domain.activity.service.raffle;

import com.dasi.domain.activity.model.io.RaffleContext;
import com.dasi.domain.activity.model.io.RaffleResult;

public interface IActivityRaffle {

    RaffleResult doActivityRaffle(RaffleContext raffleContext);

}
