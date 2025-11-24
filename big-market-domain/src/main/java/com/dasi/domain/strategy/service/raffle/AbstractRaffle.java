package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.check.RuleCheckRequest;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.model.dto.RaffleRequest;
import com.dasi.domain.strategy.model.dto.RaffleResponse;
import com.dasi.domain.strategy.model.entity.*;
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
    public RaffleResponse doRaffle(RaffleRequest raffleRequest) {
        // 1. 构造输入输出
        RuleCheckRequest ruleCheckRequest = RuleCheckRequest.builder()
                .strategyId(raffleRequest.getStrategyId())
                .userId(raffleRequest.getUserId())
                .build();
        RuleCheckResponse ruleCheckResponse;

        // 2. 执行前置检查
        ruleCheckResponse = beforeCheck(ruleCheckRequest);

        // 3. 判断是否需要继续
        if (ruleCheckResponse.getRuleCheckResult() == RuleCheckResult.CAPTURE) {
            AwardEntity awardEntity = strategyRepository.queryAwardEntityByAwardId(ruleCheckRequest.getAwardId());
            return RaffleResponse.buildAward(awardEntity);
        } else {
            ruleCheckRequest.setAwardId(ruleCheckResponse.getAwardId());
        }

        // 4. 执行后置检查
        ruleCheckResponse = afterCheck(ruleCheckRequest);

        // 5. 返回结果
        AwardEntity awardEntity = strategyRepository.queryAwardEntityByAwardId(ruleCheckResponse.getAwardId());
        return RaffleResponse.buildAward(awardEntity);
    }

    protected abstract RuleCheckResponse afterCheck(RuleCheckRequest ruleCheckRequest);
    protected abstract RuleCheckResponse beforeCheck(RuleCheckRequest ruleCheckRequest);

}
