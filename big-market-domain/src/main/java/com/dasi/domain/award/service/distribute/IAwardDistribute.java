package com.dasi.domain.award.service.distribute;

import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;

public interface IAwardDistribute {

    DistributeResult doAwardDistribute(DistributeContext distributeContext);

    int updateRaffleAwardState(RaffleAwardEntity raffleAwardEntity);

}
