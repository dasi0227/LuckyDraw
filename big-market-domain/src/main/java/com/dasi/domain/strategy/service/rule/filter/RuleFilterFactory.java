package com.dasi.domain.strategy.service.rule.filter;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.io.FilterResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unchecked", "unused"})
@Service
public class RuleFilterFactory {

    // 存储了所有实现 IRuleFilter 的集合
    private final Map<String, IRuleFilter<?>> ruleFilterMap = new ConcurrentHashMap<>();

    // 返回集合
    public <T extends FilterResponse.FilterDataEntity> Map<String, IRuleFilter<T>> openLogicFilter() {
        return (Map<String, IRuleFilter<T>>) (Map<?, ?>) ruleFilterMap;
    }

    // Spring 会注入所有实现了 IRuleFilter 接口的类
    public RuleFilterFactory(List<IRuleFilter<?>> ruleFilterList) {
        ruleFilterList.forEach(ruleFilter -> {
            // 只有带有 @RuleConfig 注解的才能放入集合
            RuleConfig config = AnnotationUtils.findAnnotation(ruleFilter.getClass(), RuleConfig.class);
            if (null != config) {
                // 规则名字作为 key，对应的过滤器作为 value
                ruleFilterMap.put(config.ruleModel().getName(), ruleFilter);
            }
        });
    }

}
