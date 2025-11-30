package com.dasi.domain.award.service.query.impl;

import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.query.IAwardQuery;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class DefaultAwardQuery implements IAwardQuery {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId) {
        return awardRepository.queryStrategyAwardListByActivityId(activityId);
    }

    @Override
    public Map<String, AwardEntity> queryAwardMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId) {
        return awardRepository.queryAwardMapByActivityId(strategyAwardEntityList, activityId);
    }

    @Override
    public Map<String, Integer> queryRuleNodeLockCountMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId) {
        return awardRepository.queryRuleNodeLockCountMapByActivityId(strategyAwardEntityList, activityId);
    }

    @Override
    public Integer queryUserLotteryCount(String userId, Long activityId) {
        return awardRepository.queryUserLotteryCount(userId, activityId);
    }

}
