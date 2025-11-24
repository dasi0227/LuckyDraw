package com.dasi.domain.strategy.service.lottery;

import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Character;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.SecureRandom;

@Slf4j
@Service
public class DefaultLottery implements ILottery{

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public Integer doLottery(Long strategyId) {
        String key = String.valueOf(strategyId);
        return doLottery(key);
    }

    @Override
    public Integer doLottery(Long strategyId, String ruleWeight) {
        String key = String.valueOf(strategyId).concat(Character.UNDERSCORE).concat(ruleWeight);
        return doLottery(key);
    }

    private Integer doLottery(String key) {
        // 1. 获取概率长度
        int rateRange = strategyRepository.getRateRange(key);
        // 2. 生成随机数，找到对应的概率奖品
        return strategyRepository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
    }

}
