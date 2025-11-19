package com.dasi.infrastructure.persistent.repository;

import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.infrastructure.persistent.dao.IStrategyAwardDao;
import com.dasi.infrastructure.persistent.po.StrategyAward;
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
    public void storeStrategyAwardRate(Long strategyId, Integer rateRange, Map<String, String> strategyAwardMap) {

        // 1. 存储当前策略对应的概率长度
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, rateRange);

        // 2. 存储当前策略对应的概率奖品表
        RMap<String, String> cacheMap = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        cacheMap.putAll(strategyAwardMap);

    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }

    @Override
    public Integer getStrategyAwardAssemble(Long strategyId, int randomNum) {
        return Integer.valueOf(redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, String.valueOf(randomNum)));
    }


}
