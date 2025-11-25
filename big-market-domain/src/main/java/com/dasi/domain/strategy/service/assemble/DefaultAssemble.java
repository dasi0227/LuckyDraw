package com.dasi.domain.strategy.service.assemble;

import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.model.rule.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Character;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
@Service
public class DefaultAssemble implements IAssemble {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public boolean assembleStrategy(Long strategyId) {
        // 1. 查询当前策略的奖品列表
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardListByStrategyId(strategyId);

        // 2. 装配库存
        assembleStrategyAwardStock(strategyId, strategyAwardEntities);

        // 3. 直接将策略id作为key，然后装配奖品，此时没有任何规则应用，只有单纯的概率模型
        assembleStrategyAwardRate(String.valueOf(strategyId), strategyAwardEntities);

        // 4. 查询当前策略是否有规则 rule_weight，以及是否有配置 rule_weight 规则
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        if (!strategyEntity.hasRuleWeight()) return true;
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRuleByStrategyIDAndRuleModel(strategyId, RuleModel.RULE_WEIGHT.getName());
        if (null == strategyRuleEntity) throw new AppException("权重规则没有配置");

        // 5. 根据规则值分档装配
        Map<String, List<Integer>> ruleWeight = strategyRuleEntity.getRuleWeightValue();
        for (Entry<String, List<Integer>> entry : ruleWeight.entrySet()) {
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesUnderWeight = new ArrayList<>(strategyAwardEntities);
            strategyAwardEntitiesUnderWeight.removeIf(entity -> !entry.getValue().contains(entity.getAwardId()));
            String cacheKey = String.valueOf(strategyId).concat(Character.UNDERSCORE).concat(entry.getKey());
            assembleStrategyAwardRate(cacheKey, strategyAwardEntitiesUnderWeight);
        }

        return true;
    }

    private void assembleStrategyAwardRate(String cacheKey, List<StrategyAwardEntity> entities) {
        // 1. 获取最小概率
        BigDecimal minValue = entities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2. 获取概率总和
        BigDecimal sumValue = entities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 获取概率长度：把最小概率当作基本单位，计算概率总和对应多少个基本单位，并向上取整数
        BigDecimal rateRange = sumValue.divide(minValue, 0, RoundingMode.CEILING);

        // 4. 计算每个倍率与最小概率的比值，也即对应多少个基本单位，将对应数量的 awardId 加入概率奖品数组
        ArrayList<Integer> strategyAwardArray = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity entity : entities) {
            Integer awardId = entity.getAwardId();
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
        log.info("【装配器 - rate】cacheKey = {}", cacheKey);
    }

    private void assembleStrategyAwardStock(Long strategyId, List<StrategyAwardEntity> strategyAwardEntities) {
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            String cacheKey = RedisKey.STRATEGY_AWARD_STOCK_KEY + strategyId + Character.UNDERSCORE + strategyAwardEntity.getAwardId();
            Integer stock = strategyAwardEntity.getAwardCountSurplus();
            strategyRepository.cacheStrategyAwardStock(cacheKey, stock);
            log.info("【装配器 - stock】cacheKey = {}, stock = {}", cacheKey, stock);
        }
    }

}
