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

        // 2. 查询【单独策略】对应的所有规则，并执行前置规则检查
        String[] beforeRuleModels = repository.queryStrategyRuleModels(strategyId).getBeforeRuleModels();
        RuleResultEntity<RuleResultEntity.RuleBeforeEntity> ruleBeforeEntity = this.checkBeforeRule(
                raffleRequestEntity,
                beforeRuleModels
        );

        // 3. 解析前置规则的返回结果
        if (RuleDecisionVO.TAKE_OVER.getCode().equals(ruleBeforeEntity.getCode())) {
            // 黑名单命中，直接返回固定的奖品 ID
            if (RuleFactory.RuleModel.RULE_BLACKLIST.getName().equals(ruleBeforeEntity.getRuleModel())) {
                Integer awardId = ruleBeforeEntity.getData().getAwardId();
                AwardEntity awardEntity = repository.queryAwardEntityByAwardId(awardId);
                return RaffleResponseEntity.buildAward(strategyId, awardEntity);
            }
            // 权重命中，返回权重抽奖的奖品 ID
            else if (RuleFactory.RuleModel.RULE_WEIGHT.getName().equals(ruleBeforeEntity.getRuleModel())) {
                String ruleWeight = ruleBeforeEntity.getData().getRuleWeight();
                Integer awardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeight);
                AwardEntity awardEntity = repository.queryAwardEntityByAwardId(awardId);
                return RaffleResponseEntity.buildAward(strategyId, awardEntity);
            }
        }

        // 4. 执行默认抽奖
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);

        // 5. 查询【策略-奖品】对应的所有规则，并执行中置规则检查
        String[] duringRuleModels = repository.queryStrategyAwardRuleModels(strategyId, awardId).getDuringRuleModels();
        raffleRequestEntity.setAwardId(awardId);
        RuleResultEntity<RuleResultEntity.RuleDuringEntity> ruleDuringEntity = this.checkDuringRule(
                raffleRequestEntity,
                duringRuleModels
        );

        // 6. 解析中置规则的返回结果
        if (RuleDecisionVO.TAKE_OVER.getCode().equals(ruleDuringEntity.getCode())) {
            log.info("【临时处理】中置规则成功拦截");
            return RaffleResponseEntity.builder().awardDesc("临时处理，中置规则成功拦截").build();
        }

        // x. 根据 awardId 包装结果返回
        AwardEntity awardEntity = repository.queryAwardEntityByAwardId(awardId);
        return RaffleResponseEntity.buildAward(strategyId, awardEntity);
    }

    protected abstract RuleResultEntity<RuleResultEntity.RuleBeforeEntity> checkBeforeRule(RaffleRequestEntity request, String... ruleModels);

    protected abstract RuleResultEntity<RuleResultEntity.RuleDuringEntity> checkDuringRule(RaffleRequestEntity request, String... ruleModels);

}
