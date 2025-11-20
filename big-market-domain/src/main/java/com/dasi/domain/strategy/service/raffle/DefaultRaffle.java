package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.entity.RaffleRequestEntity;
import com.dasi.domain.strategy.model.entity.RuleContextEntity;
import com.dasi.domain.strategy.model.entity.RuleResultEntity;
import com.dasi.domain.strategy.model.vo.RuleDecisionVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.armory.IStrategyDispatch;
import com.dasi.domain.strategy.service.rule.IRuleFilter;
import com.dasi.domain.strategy.service.rule.factory.RuleFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultRaffle extends AbstractRaffle {

    @Resource
    private RuleFactory ruleFactory;

    public DefaultRaffle(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
        super(repository, strategyDispatch);
    }

    @Override
    protected RuleResultEntity<RuleResultEntity.RuleDataBeforeEntity> doCheckRaffleBeforeRule(RaffleRequestEntity raffleRequestEntity, String... ruleModels) {
        Map<String, IRuleFilter<RuleResultEntity.RuleDataBeforeEntity>> ruleFilterMap = ruleFactory.openLogicFilter();

        // 先过滤黑名单
        if (Arrays.asList(ruleModels).contains(RuleFactory.RuleModel.RULE_BLACKLIST.getName())) {
            // 先从工厂拿到过滤器
            IRuleFilter<RuleResultEntity.RuleDataBeforeEntity> ruleFilter = ruleFilterMap.get(RuleFactory.RuleModel.RULE_BLACKLIST.getName());
            // 构造上下文
            RuleContextEntity ruleContextEntity = new RuleContextEntity();
            ruleContextEntity.setUserId(raffleRequestEntity.getUserId());
            ruleContextEntity.setStrategyId(raffleRequestEntity.getStrategyId());
            ruleContextEntity.setRuleModel(RuleFactory.RuleModel.RULE_BLACKLIST.getName());
            // 获取结果
            RuleResultEntity<RuleResultEntity.RuleDataBeforeEntity> ruleResultEntity = ruleFilter.filter(ruleContextEntity);
            // 解析结果
            if (!RuleDecisionVO.ALLOW.getCode().equals(ruleResultEntity.getCode())) {
                return ruleResultEntity;
            }
        }

        // 不再需要判断黑名单规则
        List<String> ruleModelList = Arrays.stream(ruleModels)
                .filter(ruleModel -> !ruleModel.equals(RuleFactory.RuleModel.RULE_BLACKLIST.getName()))
                .collect(Collectors.toList());

        // 判断剩余规则
        RuleResultEntity<RuleResultEntity.RuleDataBeforeEntity> ruleResultEntity = null;
        for (String ruleModel : ruleModelList) {
            IRuleFilter<RuleResultEntity.RuleDataBeforeEntity> ruleFilter = ruleFilterMap.get(ruleModel);
            RuleContextEntity ruleContextEntity = new RuleContextEntity();
            ruleContextEntity.setUserId(raffleRequestEntity.getUserId());
            ruleContextEntity.setStrategyId(raffleRequestEntity.getStrategyId());
            ruleContextEntity.setRuleModel(ruleModel);
            ruleResultEntity = ruleFilter.filter(ruleContextEntity);
            if (!RuleDecisionVO.ALLOW.getCode().equals(ruleResultEntity.getCode())) {
                return ruleResultEntity;
            }
        }

        return ruleResultEntity;
    }
}
