package com.dasi.domain.strategy.service.tree;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;
import com.dasi.domain.strategy.service.tree.impl.DefaultStrategyTreeEngine;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StrategyTreeFactory {

    private final Map<String, IStrategyTree> ruleTreeMap = new ConcurrentHashMap<>();

    // 注入所有实现了 IStrategyTree 接口的实现类，统一存入集合之中
    public StrategyTreeFactory(List<IStrategyTree> ruleTreeList) {
        ruleTreeList.forEach(ruleTree -> {
            // 只有带有 @RuleConfig 注解的才能放入集合
            RuleConfig config = AnnotationUtils.findAnnotation(ruleTree.getClass(), RuleConfig.class);
            if (null != config) {
                // 规则名字作为 key，对应的树作为 value
                this.ruleTreeMap.put(config.ruleModel().name(), ruleTree);
            }
        });
    }

    public IStrategyTreeEngine getTreeEngine(RuleTreeVO ruleTreeVO) {
        return new DefaultStrategyTreeEngine(ruleTreeMap, ruleTreeVO);
    }

}
