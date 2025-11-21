package com.dasi.domain.strategy.service.rule.chain;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.enumeration.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
@Service
public class RuleChainFactory {

    private final Map<String, IRuleChain> ruleChainMap = new ConcurrentHashMap<>();

    private final IStrategyRepository strategyRepository;

    // 注入所有实现了 IRuleChain 接口的实现类，统一存入集合之中
    public RuleChainFactory(List<IRuleChain> ruleChainList, IStrategyRepository strategyRepository) {
        this.strategyRepository = strategyRepository;
        ruleChainList.forEach(ruleChain -> {
            // 只有带有 @RuleConfig 注解的才能放入集合
            RuleConfig config = AnnotationUtils.findAnnotation(ruleChain.getClass(), RuleConfig.class);
            if (null != config) {
                // 规则名字作为 key，对应的责任链作为 value
                this.ruleChainMap.put(config.ruleModel().getName(), ruleChain);
            }
        });
    }

    public IRuleChain getFirstRuleChain(Long strategyId) {
        // 当前策略包含的所有前置规则
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategyEntity.splitRuleModels();

        // 如果没有前置规则，则直接执行默认责任链
        IRuleChain defaultRuleChain = ruleChainMap.get(RuleModel.RULE_DEFAULT.getName());
        if (null == ruleModels || ruleModels.length == 0) {
            return defaultRuleChain;
        }

        // 否则，按照数据库的顺序，逐一添加责任链
        IRuleChain firstRuleChain = ruleChainMap.get(ruleModels[0]);
        IRuleChain currentRuleChain = firstRuleChain;
        for (int i = 1; i < ruleModels.length; i++) {
            IRuleChain nextRuleChain = ruleChainMap.get(ruleModels[i]);
            currentRuleChain = currentRuleChain.appendNext(nextRuleChain);
        }
        currentRuleChain.appendNext(defaultRuleChain);

        return firstRuleChain;
    }

}
