package com.dasi.domain.strategy.service.lottery.impl;

import com.dasi.domain.strategy.model.dto.LotteryResult;
import com.dasi.domain.strategy.model.dto.RuleCheckContext;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStrategyLottery implements IStrategyLottery {

    protected final IStrategyRepository strategyRepository;

    protected AbstractStrategyLottery(IStrategyRepository strategyRepository) {
        this.strategyRepository = strategyRepository;
    }

    @Override
    public LotteryResult doStrategyLottery(LotteryContext lotteryContext) {
        // 1. 构造输入输出
        RuleCheckContext ruleCheckContext = RuleCheckContext.builder()
                .userId(lotteryContext.getUserId())
                .strategyId(lotteryContext.getStrategyId())
                .build();
        RuleCheckResult ruleCheckResult;

        // 2. 执行前置检查
        ruleCheckResult = beforeCheck(ruleCheckContext);

        // 3. 判断是否需要继续
        if (ruleCheckResult.getRuleModel() == RuleModel.RULE_BLACKLIST) {
            return buildLotteryResult(ruleCheckResult.getAwardId());
        } else {
            ruleCheckContext.setAwardId(ruleCheckResult.getAwardId());
        }

        // 4. 执行后置检查
        ruleCheckResult = afterCheck(ruleCheckContext);

        // 5. 返回结果
        return buildLotteryResult(ruleCheckResult.getAwardId());
    }

    protected abstract RuleCheckResult beforeCheck(RuleCheckContext ruleCheckContext);
    protected abstract RuleCheckResult afterCheck(RuleCheckContext ruleCheckContext);

    private LotteryResult buildLotteryResult(Integer awardId) {
        AwardEntity awardEntity = strategyRepository.queryAwardByAwardId(awardId);
        return LotteryResult.builder()
                .awardId(awardId)
                .awardName(awardEntity.getAwardName())
                .awardConfig(awardEntity.getAwardConfig())
                .build();
    }

}
