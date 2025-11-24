package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.check.RuleCheckModel;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.model.dto.StockUpdateRequest;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import com.dasi.domain.strategy.service.stock.IStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleCheckModel.RULE_STOCK)
public class RuleStockTree implements IRuleTree {

    @Resource
    private IStock stock;

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public RuleCheckResponse logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        long surplus = stock.subStrategyAwardCount(strategyId, awardId);
        if (surplus != -1L) {
            log.info("【规则树 - rule_stock】接管：surplus = {}", surplus);
            StockUpdateRequest stockUpdateRequest = StockUpdateRequest.builder()
                    .awardId(awardId)
                    .strategyId(strategyId)
                    .build();
            // 扣减成功：放到延迟队列之中，异步操作数据库
            strategyRepository.sendStockConsumeToQueue(stockUpdateRequest);
            return RuleCheckResponse.builder()
                    .awardId(awardId)
                    .ruleCheckResult(RuleCheckResult.CAPTURE)
                    .ruleCheckModel(RuleCheckModel.RULE_STOCK)
                    .build();
        } else {
            log.info("【规则树 - rule_stock】放行");
            return RuleCheckResponse.builder()
                    .awardId(null)
                    .ruleCheckResult(RuleCheckResult.PERMIT)
                    .ruleCheckModel(RuleCheckModel.RULE_STOCK)
                    .build();
        }
    }

}
