package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.check.RuleCheckRequest;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.tree.RuleTreeVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.ILottery;
import com.dasi.domain.strategy.service.rule.chain.IRuleChain;
import com.dasi.domain.strategy.service.rule.chain.RuleChainFactory;
import com.dasi.domain.strategy.service.rule.tree.IRuleTreeEngine;
import com.dasi.domain.strategy.service.rule.tree.RuleTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    protected RuleCheckResponse beforeCheck(RuleCheckRequest ruleCheckRequest) {
        IRuleChain firstRuleChain = ruleChainFactory.getFirstRuleChain(ruleCheckRequest.getStrategyId());
        return firstRuleChain.logic(ruleCheckRequest.getUserId(), ruleCheckRequest.getStrategyId());
    }

    @Override
    protected RuleCheckResponse afterCheck(RuleCheckRequest ruleCheckRequest) {
        String[] ruleModels = strategyRepository.queryStrategyRuleModelByStrategyIdAndAwardId(ruleCheckRequest.getStrategyId(), ruleCheckRequest.getAwardId());
        if (ruleModels == null || ruleModels.length == 0) {
            return RuleCheckResponse.builder()
                    .awardId(ruleCheckRequest.getAwardId())
                    .ruleCheckModel(null)
                    .build();
        }

        String ruleModel = ruleModels[0];
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(ruleModel);
        if (ruleTreeVO == null) {
            throw new RuntimeException("规则树配置错误");
        }
        IRuleTreeEngine ruleTreeEngine = ruleTreeFactory.getTreeEngine(ruleTreeVO);
        return ruleTreeEngine.process(ruleCheckRequest.getUserId(), ruleCheckRequest.getStrategyId(), ruleCheckRequest.getAwardId());
    }

}
