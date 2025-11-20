package com.dasi.domain.strategy.service.rule.factory;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.entity.RuleResultEntity;
import com.dasi.domain.strategy.service.rule.IRuleFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleFactory {

    // 存储了所有实现 IRuleFilter 的集合
    private final Map<String, IRuleFilter<?>> ruleFilterMap = new ConcurrentHashMap<>();

    // 返回集合
    public <T extends RuleResultEntity.RuleDataEntity> Map<String, IRuleFilter<T>> openLogicFilter() {
        return (Map<String, IRuleFilter<T>>) (Map<?, ?>) ruleFilterMap;
    }

    // Spring 会注入所有实现了 IRuleFilter 接口的类
    public RuleFactory(List<IRuleFilter<?>> ruleFilters) {
        ruleFilters.forEach(ruleFilter -> {
            // 只有带有 @RuleConfig 注解的才能放入集合
            RuleConfig config = AnnotationUtils.findAnnotation(ruleFilter.getClass(), RuleConfig.class);
            if (null != config) {
                // 规则名字作为 key，对应的过滤器作为 value
                ruleFilterMap.put(config.ruleModel().getName(), ruleFilter);
            }
        });
    }

    @Getter
    @AllArgsConstructor
    public enum RuleModel {

        RULE_WEIGHT("rule_weight","【抽奖前规则】根据抽奖权重，返回可抽奖范围"),
        RULE_BLACKLIST("rule_blacklist","【抽奖前规则】根据黑名单，过滤用户"),
        ;

        private final String name;
        private final String info;

    }

}
