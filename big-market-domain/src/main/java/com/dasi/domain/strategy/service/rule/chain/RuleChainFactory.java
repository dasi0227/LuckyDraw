package com.dasi.domain.strategy.service.rule.chain;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Delimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleChainFactory {

    private final Map<String, IRuleChain> ruleChainPrototypeCache = new ConcurrentHashMap<>();

    private final Map<String, IRuleChain> ruleChainMap = new ConcurrentHashMap<>();

    private final IStrategyRepository strategyRepository;

    // 注入所有实现了 IRuleChain 接口的实现类，统一存入集合之中
    public RuleChainFactory(List<IRuleChain> ruleChainList, IStrategyRepository strategyRepository) {
        this.strategyRepository = strategyRepository;
        ruleChainList.forEach(ruleChain -> {
            // 只有带有 @RuleConfig 注解的才能放入集合
            RuleConfig ruleConfig = AnnotationUtils.findAnnotation(ruleChain.getClass(), RuleConfig.class);
            if (null != ruleConfig) {
                // 规则名字作为 key，对应的责任链作为 value
                this.ruleChainMap.put(ruleConfig.ruleModel().getCode(), ruleChain);
            }
        });
    }

    public IRuleChain getRuleModelChain(Long strategyId) {
        // 查实体中的前置规则
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        String ruleModelsStr = strategyEntity.getRuleModels();

        // 没有前置规则：直接返回默认链的 clone
        if (StringUtils.isBlank(ruleModelsStr)) {
            IRuleChain defaultChain = ruleChainMap.get(RuleModel.RULE_DEFAULT.getCode());
            return defaultChain.clone();
        }

        // 有前置规则：用 ruleModelsStr 做 key，从本地缓存拿“原型链”
        IRuleChain prototype = ruleChainPrototypeCache.computeIfAbsent(ruleModelsStr, this::buildChainPrototype);

        // 每次请求都 clone 一份，避免 next 被并发修改
        return prototype.clone();
    }

    private IRuleChain buildChainPrototype(String ruleModelsStr) {
        String[] ruleModels = ruleModelsStr.split(Delimiter.COMMA);
        IRuleChain head = ruleChainMap.get(ruleModels[0]).clone();
        IRuleChain current = head;
        for (int i = 1; i < ruleModels.length; i++) {
            current = current.appendNext(ruleChainMap.get(ruleModels[i]).clone());
        }
        current.appendNext(ruleChainMap.get(RuleModel.RULE_DEFAULT.getCode()).clone());
        return head;
    }
}
