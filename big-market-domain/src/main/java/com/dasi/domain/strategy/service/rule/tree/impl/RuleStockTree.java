package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardStockEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import com.dasi.domain.strategy.service.stock.IStrategyStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_STOCK)
public class RuleStockTree implements IRuleTree {

    @Resource
    private IStrategyStock strategyStock;

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        long surplus = strategyStock.subStrategyAwardCount(strategyId, awardId);
        if (surplus > 0L) {
            log.info("【策略规则树 - rule_stock】接管：awardId = {}, surplus = {}->{}", awardId, surplus + 1, surplus);
            StrategyAwardStockEntity stockUpdateRequest = StrategyAwardStockEntity.builder()
                    .awardId(awardId)
                    .strategyId(strategyId)
                    .build();
            // 扣减成功：放到延迟队列之中，异步操作数据库
            strategyRepository.sendStrategyAwardStockConsumeToMQ(stockUpdateRequest);
            return RuleCheckResult.builder()
                    .awardId(awardId)
                    .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                    .ruleModel(RuleModel.RULE_STOCK)
                    .build();
        } else {
            log.info("【策略规则树 - rule_stock】放行：surplus = {}", surplus);
            return RuleCheckResult.builder()
                    .awardId(null)
                    .ruleCheckOutcome(RuleCheckOutcome.PERMIT)
                    .ruleModel(RuleModel.RULE_STOCK)
                    .build();
        }
    }

}
