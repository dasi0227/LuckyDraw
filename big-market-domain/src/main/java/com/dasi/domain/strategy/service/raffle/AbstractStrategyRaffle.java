package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.dto.RaffleContext;
import com.dasi.domain.strategy.model.dto.RaffleResult;
import com.dasi.domain.strategy.model.dto.RuleCheckContext;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStrategyRaffle implements IStrategyRaffle {

    protected final IStrategyRepository strategyRepository;

    protected final IStrategyLottery strategyLottery;

    public AbstractStrategyRaffle(IStrategyRepository strategyRepository, IStrategyLottery strategyLottery) {
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
        if (ruleCheckResult.getRuleModel() == RuleModel.RULE_BLACKLIST) {
            return RaffleResult.build(ruleCheckResult.getAwardId(), strategyRepository);
        } else {
            ruleCheckContext.setAwardId(ruleCheckResult.getAwardId());
        }

        // 4. 执行后置检查
        ruleCheckResult = afterCheck(ruleCheckContext);

        // 5. 返回结果
        return RaffleResult.build(ruleCheckResult.getAwardId(), strategyRepository);
    }

    protected abstract RuleCheckResult beforeCheck(RuleCheckContext ruleCheckContext);
    protected abstract RuleCheckResult afterCheck(RuleCheckContext ruleCheckContext);

}
