package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;

public interface IAwardRepository {

    void saveRaffleAward(RaffleAwardEntity raffleAwardEntity, TaskEntity taskEntity);

    int updateRaffleAwardState(RaffleAwardEntity raffleAwardEntity);

}
