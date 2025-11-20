package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.entity.*;
import com.dasi.domain.strategy.model.vo.RuleDecisionVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.armory.IStrategyDispatch;
import com.dasi.domain.strategy.service.rule.factory.RuleFactory;
import com.dasi.types.enums.ResponseCode;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractRaffle implements IRaffle {

    protected final IStrategyRepository repository;
    protected final IStrategyDispatch strategyDispatch;

    public AbstractRaffle(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
        this.repository = repository;
        this.strategyDispatch = strategyDispatch;
    }

    @Override
    public RaffleResponseEntity doRaffle(RaffleRequestEntity raffleRequestEntity) {
        // 1. 参数校验
        String userId = raffleRequestEntity.getUserId();
        Long strategyId = raffleRequestEntity.getStrategyId();
        if (StringUtils.isBlank(userId) || strategyId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 策略查询
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        if (strategy == null) {
            throw new AppException(ResponseCode.NOT_FOUND.getCode(), ResponseCode.NOT_FOUND.getInfo());
        }

        // 3. 抽奖前的规则过滤
        RuleResultEntity<RuleResultEntity.RuleDataBeforeEntity> ruleResultEntity = this.doCheckRaffleBeforeRule(
                raffleRequestEntity,
                strategy.ruleModels()
        );

        // 4. 如果规则命中了，需要处理
        if (RuleDecisionVO.TAKE_OVER.getCode().equals(ruleResultEntity.getCode())) {
            // 黑名单命中，直接返回固定的奖品 ID
            if (RuleFactory.RuleModel.RULE_BLACKLIST.getName().equals(ruleResultEntity.getRuleModel())) {
                Integer awardId = ruleResultEntity.getData().getAwardId();
                return RaffleResponseEntity.buildAward(strategyId, awardId, repository);
            }
            // 权重命中，返回权重抽奖的奖品 ID
            else if (RuleFactory.RuleModel.RULE_WEIGHT.getName().equals(ruleResultEntity.getRuleModel())) {
                String ruleWeight = ruleResultEntity.getData().getRuleWeight();
                Integer awardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeight);
                return RaffleResponseEntity.buildAward(strategyId, awardId, repository);
            }
        }

        // 5. 没有一个规则命中，返回默认抽奖的奖品 ID
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        return RaffleResponseEntity.buildAward(strategyId, awardId, repository);
    }

    protected abstract RuleResultEntity<RuleResultEntity.RuleDataBeforeEntity> doCheckRaffleBeforeRule(RaffleRequestEntity request, String... ruleModels);

}
