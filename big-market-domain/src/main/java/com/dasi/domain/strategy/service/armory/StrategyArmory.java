package com.dasi.domain.strategy.service.armory;

import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class StrategyArmory implements IStrategyArmory {

    @Resource
    private IStrategyRepository repository;

    @Override
    public void assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略配置
        List<StrategyAwardEntity> entities = repository.queryStrategyAwardList(strategyId);

        // 2. 获取最小概率
        BigDecimal minValue = entities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 3. 获取概率总和
        BigDecimal sumValue = entities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 获取概率长度：把最小概率当作基本单位，计算概率总和对应多少个基本单位，并向上取整数
        BigDecimal rateRange = sumValue.divide(minValue, 0, RoundingMode.CEILING);

        // 5. 计算每个倍率与最小概率的比值，也即对应多少个基本单位，将对应数量的 awardId 加入概率奖品数组
        ArrayList<Integer> strategyAwardArray = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity entity : entities) {
            Integer awardId = entity.getAwardId();
            BigDecimal awardRate = entity.getAwardRate();
            int amount = rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue();
            strategyAwardArray.addAll(Collections.nCopies(amount, awardId));
        }

        // 6. 打乱概率奖品数组
        Collections.shuffle(strategyAwardArray);

        // 7. 将 Array 变成 Map，并将索引作为 key，从而利用 Redis 的 HSET 提升查找性能
        Map<String, String> strategyAwardMap = new HashMap<>();
        for (int i = 0; i < strategyAwardArray.size(); i++) {
            strategyAwardMap.put(String.valueOf(i), String.valueOf(strategyAwardArray.get(i)));
        }

        // 8. 将 Map 存储到 Redis
        repository.storeStrategyAwardRate(strategyId, strategyAwardMap.size(), strategyAwardMap);
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 1. 获取概率长度
        int rateRange = repository.getRateRange(strategyId);

        // 2. 生成随机数，找到对应的概率奖品
        int randomNum = new SecureRandom().nextInt(rateRange);
        return repository.getStrategyAwardAssemble(strategyId, randomNum);
    }

}
