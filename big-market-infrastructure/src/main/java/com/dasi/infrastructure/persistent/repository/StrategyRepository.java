package com.dasi.infrastructure.persistent.repository;

import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.infrastructure.persistent.dao.IStrategyAwardDao;
import com.dasi.infrastructure.persistent.dao.IStrategyDao;
import com.dasi.infrastructure.persistent.dao.IStrategyRuleDao;
import com.dasi.infrastructure.persistent.po.Strategy;
import com.dasi.infrastructure.persistent.po.StrategyAward;
import com.dasi.infrastructure.persistent.po.StrategyRule;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.common.Constants;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IStrategyDao strategyDao;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Resource
    private IRedisService redisService;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {

        // 先看缓存是否有【策略奖品】列表
        String key = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> entities = redisService.getValue(key);
        if (null != entities && !entities.isEmpty()) {
            return entities;
        }

        // 如果缓存没有，就从数据库读取【策略奖品】，然后再转换为所需要的实体
        List<StrategyAward> list = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        entities = new ArrayList<>(list.size());
        for (StrategyAward strategyAward : list) {
            StrategyAwardEntity entity = StrategyAwardEntity.builder()
                        .strategyId(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .build();
            entities.add(entity);
        }

        // 从数据库查找完之后，要放入缓存之中
        redisService.setValue(key, entities);

        return entities;
    }

    @Override
    public void storeStrategyAwardRate(String key, Integer rateRange, Map<String, String> strategyAwardMap) {

        // 1. 存储当前策略对应的概率长度
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange);

        // 2. 存储当前策略对应的概率奖品表
        RMap<String, String> cacheMap = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheMap.putAll(strategyAwardMap);

    }

    @Override
    public int getRateRange(String key) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }

    @Override
    public Integer getStrategyAwardAssemble(String key, int randomNum) {
        return Integer.valueOf(redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, String.valueOf(randomNum)));
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 先看缓存是否有【策略】列表
        String key = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity entity = redisService.getValue(key);
        if (null != entity) {
            return entity;
        }

        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        entity = StrategyEntity.builder()
            .strategyId(strategy.getStrategyId())
            .strategyDesc(strategy.getStrategyDesc())
            .ruleModels(strategy.getRuleModels())
            .build();
        redisService.setValue(key, entity);

        return entity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRuleByRuleModel(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleRequest = new StrategyRule();
        strategyRuleRequest.setStrategyId(strategyId);
        strategyRuleRequest.setRuleModel(ruleModel);
        StrategyRule strategyRuleResponse = strategyRuleDao.queryStrategyRuleByRuleModel(strategyRuleRequest);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleResponse.getStrategyId())
                .awardId(strategyRuleResponse.getAwardId())
                .ruleType(strategyRuleResponse.getRuleType())
                .ruleModel(strategyRuleResponse.getRuleModel())
                .ruleValue(strategyRuleResponse.getRuleValue())
                .ruleDesc(strategyRuleResponse.getRuleDesc())
                .build();
    }


}
