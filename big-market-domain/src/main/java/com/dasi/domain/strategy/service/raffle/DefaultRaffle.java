package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.dto.RuleCheckContext;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.tree.RuleTreeVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.ILottery;
import com.dasi.domain.strategy.service.rule.chain.IRuleChain;
import com.dasi.domain.strategy.service.rule.chain.RuleChainFactory;
import com.dasi.domain.strategy.service.rule.tree.IRuleTreeEngine;
import com.dasi.domain.strategy.service.rule.tree.RuleTreeFactory;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class DefaultRaffle extends AbstractRaffle {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    private RuleChainFactory ruleChainFactory;

    @Resource
    private RuleTreeFactory ruleTreeFactory;

    public DefaultRaffle(IStrategyRepository repository, ILottery strategyDispatch) {
        super(repository, strategyDispatch);
    }

    @Override
    protected RuleCheckResult beforeCheck(RuleCheckContext ruleCheckContext) {
        IRuleChain firstRuleChain = ruleChainFactory.getFirstRuleChain(ruleCheckContext.getStrategyId());
        return firstRuleChain.logic(ruleCheckContext.getUserId(), ruleCheckContext.getStrategyId());
    }

    @Override
    protected RuleCheckResult afterCheck(RuleCheckContext ruleCheckContext) {
        String treeId = strategyRepository.queryStrategyAwardTreeIdByStrategyIdAndAwardId(ruleCheckContext.getStrategyId(), ruleCheckContext.getAwardId());
        if (treeId == null || treeId.isEmpty()) {
            return RuleCheckResult.builder()
                    .awardId(ruleCheckContext.getAwardId())
                    .build();
        }

        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(treeId);
        if (ruleTreeVO == null) {
            throw new AppException("规则树配置错误");
        }
        IRuleTreeEngine ruleTreeEngine = ruleTreeFactory.getTreeEngine(ruleTreeVO);
        return ruleTreeEngine.process(ruleCheckContext.getUserId(), ruleCheckContext.getStrategyId(), ruleCheckContext.getAwardId());
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId) {
        return strategyRepository.queryStrategyAwardListByStrategyId(strategyId);
    }

}
