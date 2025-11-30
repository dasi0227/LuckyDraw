package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;
import java.util.Map;

public interface IAwardRepository {

    void saveRaffleAward(RaffleAwardEntity raffleAwardEntity, TaskEntity taskEntity);

    List<TaskEntity> queryUnsolvedTask();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskState(TaskEntity taskEntity);

    List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId);

    Map<String, AwardEntity> queryAwardMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId);

    Map<String, Integer> queryRuleNodeLockCountMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId);

    Integer queryUserLotteryCount(String userId, Long activityId);

    int updateRaffleAwardState(RaffleAwardEntity raffleAwardEntity);
}
