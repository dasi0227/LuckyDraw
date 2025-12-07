package com.dasi.domain.strategy.service.tree.impl;

import com.dasi.domain.strategy.annotation.RuleModelConfig;
import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.io.StrategyAwardStock;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.tree.IStrategyTree;
import com.dasi.domain.strategy.service.stock.IStrategyStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Component
@RuleModelConfig(ruleModel = RuleModel.RULE_STOCK)
public class RuleStockTree implements IStrategyTree {

    @Resource
    private IStrategyStock strategyStock;

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId, Long awardId, String ruleValue) {
        // 获取活动结束时间
        LocalDateTime activityEndTime = strategyRepository.queryActivityEndTimeByStrategyId(strategyId);

        long surplus = strategyStock.subtractStrategyAwardCount(strategyId, awardId, activityEndTime);
        if (surplus >= 0L) {
            log.info("【抽奖】RULE_STOCK 放行：awardId={}, surplus={}->{}", awardId, surplus + 1, surplus);
            StrategyAwardStock stockUpdateRequest = StrategyAwardStock.builder()
                    .awardId(awardId)
                    .strategyId(strategyId)
                    .build();
            // 扣减成功：放到延迟队列之中，异步操作数据库
            strategyRepository.sendStrategyAwardStockConsumeToMQ(stockUpdateRequest);
            return RuleCheckResult.builder()
                    .awardId(awardId)
                    .ruleCheckOutcome(RuleCheckOutcome.PERMIT)
                    .ruleModel(RuleModel.RULE_STOCK)
                    .build();
        } else {
            log.info("【抽奖】RULE_STOCK 拦截：surplus={}", surplus);
            return RuleCheckResult.builder()
                    .awardId(null)
                    .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                    .ruleModel(RuleModel.RULE_STOCK)
                    .build();
        }
    }

}
