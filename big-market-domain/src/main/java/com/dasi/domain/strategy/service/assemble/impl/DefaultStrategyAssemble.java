package com.dasi.domain.strategy.service.assemble.impl;

import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultStrategyAssemble implements IStrategyAssemble {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public boolean assembleStrategyByActivityId(Long activityId) {
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return assembleStrategyByStrategyId(strategyId);
    }

    @Override
    public boolean assembleStrategyByStrategyId(Long strategyId) {
        try {
            // 1. 查询当前策略的奖品列表
            List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardListByStrategyId(strategyId);
            if (strategyAwardEntityList == null || strategyAwardEntityList.isEmpty()) throw new AppException("（装配）当前策略下没有配置奖品：strategyId=" + strategyId);

            // 2. 库存装配
            assembleStrategyAwardStockSurplus(strategyId, strategyAwardEntityList);

            // 3. 完全装配
            assembleStrategyAwardRate(strategyId, null, strategyAwardEntityList);

            // 4. 权重装配
            assembleStrategyByWeight(strategyId, strategyAwardEntityList);
            return true;
        } catch (Exception e) {
            log.error("【装配】策略奖品：strategyId={}, error={}", strategyId, e.getMessage());
            return false;
        }
    }

    private void assembleStrategyByWeight(Long strategyId, List<StrategyAwardEntity> strategyAwardEntityList) {
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);

        // 1. 策略无权重规则 → 直接返回
        if (!strategyEntity.hasRuleWeight()) return;

        // 2. 查询 rule_weight 规则
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRuleByStrategyIDAndRuleModel(strategyId, RuleModel.RULE_WEIGHT.getCode());
        if (strategyRuleEntity == null) throw new AppException("（装配）当前策略下没有配置权重规则：strategyId=" + strategyId);

        // 3. 权重下概率装配
        Map<String, List<Long>> ruleWeight = strategyRuleEntity.getRuleWeightValue();
        for (Entry<String, List<Long>> entry : ruleWeight.entrySet()) {
            // 1. 获取数据
            String weight = entry.getKey();
            List<Long> awardIdList = entry.getValue();
            // 2. 过滤掉不在列表里面的
            List<StrategyAwardEntity> strategyAwardEntityListUnderWeight = strategyAwardEntityList.stream()
                    .filter(strategyAwardEntity -> awardIdList.contains(strategyAwardEntity.getAwardId()))
                    .collect(Collectors.toList());
            // 3. 概率装配
            assembleStrategyAwardRate(strategyId, weight, strategyAwardEntityListUnderWeight);
        }
    }

    private void assembleStrategyAwardRate(Long strategyId, String weight, List<StrategyAwardEntity> strategyAwardEntityList) {
        if (strategyAwardEntityList.isEmpty()) throw new AppException("（装配）当前权重下没有配置奖品：strategyId=" + strategyId + ", weight=" + weight);

        String awardIds = strategyAwardEntityList.stream()
                .map(entity -> String.valueOf(entity.getAwardId()))
                .collect(Collectors.joining(","));
        log.info("【装配】奖品概率：strategyId={}, weightKey={}, awardIds={}", strategyId, weight, awardIds);

        String cacheKey = weight == null
                ? String.valueOf(strategyId)
                : strategyId + Delimiter.UNDERSCORE + weight;

        // 1. 获取最小概率
        BigDecimal minValue = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2. 获取概率总和
        BigDecimal sumValue = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 获取概率长度：把最小概率当作基本单位，计算概率总和对应多少个基本单位，并向上取整数
        BigDecimal rateRange = sumValue.divide(minValue, 0, RoundingMode.CEILING);

        // 4. 计算每个倍率与最小概率的比值，也即对应多少个基本单位，将对应数量的 awardId 加入概率奖品数组
        ArrayList<Long> strategyAwardArray = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity entity : strategyAwardEntityList) {
            Long awardId = entity.getAwardId();
            BigDecimal awardRate = entity.getAwardRate();
            int amount = rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue();
            strategyAwardArray.addAll(Collections.nCopies(amount, awardId));
        }

        // 5. 打乱概率奖品数组
        Collections.shuffle(strategyAwardArray);

        // 6. 将 Array 变成 Map，并将索引作为 cacheKey，从而利用 Redis 的 HSET 提升查找性能
        Map<String, String> strategyAwardMap = new HashMap<>();
        for (int i = 0; i < strategyAwardArray.size(); i++) {
            strategyAwardMap.put(String.valueOf(i), String.valueOf(strategyAwardArray.get(i)));
        }

        // 7. 将 Map 存储到 Redis
        strategyRepository.cacheStrategyAwardRate(cacheKey, strategyAwardMap.size(), strategyAwardMap);
    }

    // 库存装配
    private void assembleStrategyAwardStockSurplus(Long strategyId, List<StrategyAwardEntity> strategyAwardEntityList) {
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntityList) {
            Long awardId = strategyAwardEntity.getAwardId();
            String cacheKey = RedisKey.STRATEGY_AWARD_STOCK_SURPLUS_KEY + strategyId + Delimiter.UNDERSCORE + awardId;
            Integer surplus = strategyAwardEntity.getAwardSurplus();
            strategyRepository.cacheStrategyAwardStock(cacheKey, surplus);
            log.info("【装配】奖品库存：strategyId={}, awardId={}, surplus={}", strategyId, awardId, surplus);
        }
    }

}
