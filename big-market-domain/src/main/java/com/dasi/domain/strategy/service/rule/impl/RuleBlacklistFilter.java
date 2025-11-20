package com.dasi.domain.strategy.service.rule.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.entity.RuleResultEntity;
import com.dasi.domain.strategy.model.entity.RuleContextEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.IRuleFilter;
import com.dasi.domain.strategy.service.rule.factory.RuleFactory;
import com.dasi.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;


// 黑名单规则类：ruleModel 是 RULE_BLACKLIST，结果类型是 RuleBeforeEntity
@Slf4j
@Component
@RuleConfig(ruleModel = RuleFactory.RuleModel.RULE_BLACKLIST)
public class RuleBlacklistFilter implements IRuleFilter<RuleResultEntity.RuleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    // 示例：101:user001,user002,user003
    @Override
    public RuleResultEntity<RuleResultEntity.RuleBeforeEntity> filter(RuleContextEntity ruleContextEntity) {
        // 1. 找到规则对应的值
        String ruleValue = repository.queryStrategyRuleValue(ruleContextEntity.getStrategyId(), ruleContextEntity.getAwardId(), ruleContextEntity.getRuleModel());
        if (StringUtils.isBlank(ruleValue)) {
            return RuleResultEntity.allow();
        }

        // 2. 拆分黑名单规则值得到对应的奖品ID和黑名单Ids
        String[] values = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.valueOf(values[0]);
        String[] blackIds = values[1].split(Constants.COMMA);

        // 3. 查看当前用户是否在黑名单Ids里面
        String userId = ruleContextEntity.getUserId();
        if (Arrays.asList(blackIds).contains(userId)) {
            RuleResultEntity.RuleBeforeEntity result = RuleResultEntity.RuleBeforeEntity.builder()
                    .strategyId(ruleContextEntity.getStrategyId())
                    .awardId(awardId)
                    .build();
            return RuleResultEntity.takeOver(RuleFactory.RuleModel.RULE_BLACKLIST.getName(), result);
        }

        return RuleResultEntity.allow();
    }

}
