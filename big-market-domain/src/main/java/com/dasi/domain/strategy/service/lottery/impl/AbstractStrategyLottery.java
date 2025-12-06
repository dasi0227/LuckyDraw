package com.dasi.domain.strategy.service.lottery.impl;

import com.dasi.domain.strategy.model.io.LotteryContext;
import com.dasi.domain.strategy.model.io.LotteryResult;
import com.dasi.domain.strategy.model.io.RuleCheckContext;
import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractStrategyLottery implements IStrategyLottery {

    protected final IStrategyRepository strategyRepository;

    protected AbstractStrategyLottery(IStrategyRepository strategyRepository) {
        this.strategyRepository = strategyRepository;
    }

    @Override
    public LotteryResult doStrategyLottery(LotteryContext lotteryContext) {

        String userId = lotteryContext.getUserId();
        Long strategyId = lotteryContext.getStrategyId();
        if (StringUtils.isBlank(userId)) throw new AppException("（抽奖）缺少参数 userId");
        if (strategyId == null) throw new AppException("（抽奖）缺少参数 strategyId");


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
            log.info("【抽奖】得到奖品：awardId={}", ruleCheckResult.getAwardId());
            return LotteryResult.builder().awardId(ruleCheckResult.getAwardId()).build();
        } else {
            ruleCheckContext.setAwardId(ruleCheckResult.getAwardId());
        }

        // 4. 执行后置检查
        ruleCheckResult = afterCheck(ruleCheckContext);

        // 5. 返回结果
        log.info("【抽奖】得到奖品：awardId={}", ruleCheckResult.getAwardId());
        return LotteryResult.builder().awardId(ruleCheckResult.getAwardId()).build();
    }

    protected abstract RuleCheckResult beforeCheck(RuleCheckContext ruleCheckContext);
    protected abstract RuleCheckResult afterCheck(RuleCheckContext ruleCheckContext);

}
