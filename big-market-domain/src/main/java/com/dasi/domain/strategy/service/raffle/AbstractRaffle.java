package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.dto.RaffleContext;
import com.dasi.domain.strategy.model.dto.RaffleResult;
import com.dasi.domain.strategy.model.dto.RuleCheckContext;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.rule.RuleCheckOutcome;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.ILottery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRaffle implements IRaffle {

    protected final IStrategyRepository strategyRepository;

    protected final ILottery strategyLottery;

    public AbstractRaffle(IStrategyRepository strategyRepository, ILottery strategyLottery) {
        this.strategyRepository = strategyRepository;
        this.strategyLottery = strategyLottery;
    }

    @Override
    public RaffleResult doRaffle(RaffleContext raffleContext) {
        // 1. 构造输入输出
        RuleCheckContext ruleCheckContext = RuleCheckContext.builder()
                .userId(raffleContext.getUserId())
                .strategyId(raffleContext.getStrategyId())
                .build();
        RuleCheckResult ruleCheckResult;

        // 2. 执行前置检查
        ruleCheckResult = beforeCheck(ruleCheckContext);

        // 3. 判断是否需要继续
        if (ruleCheckResult.getRuleCheckOutcome() == RuleCheckOutcome.CAPTURE) {
            return RaffleResult.build(ruleCheckResult.getAwardId(), strategyRepository);
        } else {
            ruleCheckContext.setAwardId(ruleCheckResult.getAwardId());
        }

        // 4. 执行后置检查
        ruleCheckResult = afterCheck(ruleCheckContext);

        // 5. 返回结果
        return RaffleResult.build(ruleCheckResult.getAwardId(), strategyRepository);
    }

    protected abstract RuleCheckResult afterCheck(RuleCheckContext ruleCheckContext);
    protected abstract RuleCheckResult beforeCheck(RuleCheckContext ruleCheckContext);

}
