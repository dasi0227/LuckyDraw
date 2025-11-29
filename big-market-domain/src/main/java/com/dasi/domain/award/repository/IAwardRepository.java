package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;

import java.util.List;

public interface IAwardRepository {

    void saveRaffleAward(RaffleAwardEntity raffleAwardEntity, TaskEntity taskEntity);

    List<TaskEntity> queryUnsolvedTask();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskState(TaskEntity taskEntity);

}
