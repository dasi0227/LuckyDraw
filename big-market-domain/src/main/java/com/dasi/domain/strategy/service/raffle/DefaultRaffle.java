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
import java.util.Map;

@Slf4j
@Service
public class DefaultRaffle extends AbstractRaffle {

    @Resource
    private RuleFactory ruleFactory;

    public DefaultRaffle(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
        super(repository, strategyDispatch);
    }

    @Override
    protected RuleResultEntity<RuleResultEntity.RuleBeforeEntity> checkBeforeRule(RaffleRequestEntity raffleRequestEntity, String... ruleModels) {
        // 1. 空规直接放行
        RuleResultEntity<RuleResultEntity.RuleBeforeEntity> checkResult = checkRuleModelsExist(ruleModels);
        if (checkResult != null) return checkResult;

        // 2. 获取所有规则过滤器
        Map<String, IRuleFilter<RuleResultEntity.RuleBeforeEntity>> ruleFilterMap = ruleFactory.openLogicFilter();
        IRuleFilter<RuleResultEntity.RuleBeforeEntity> ruleFilter;
        RuleResultEntity<RuleResultEntity.RuleBeforeEntity> ruleResultEntity = null;

        // 3. 黑名单优先执行
        String blacklist = RuleFactory.RuleModel.RULE_BLACKLIST.getName();
        if (Arrays.asList(ruleModels).contains(blacklist)) {
            ruleFilter = ruleFilterMap.get(blacklist);
            ruleResultEntity = doRuleFilter(raffleRequestEntity, ruleFilter, blacklist);
            if (!RuleDecisionVO.ALLOW.getCode().equals(ruleResultEntity.getCode())) {
                return ruleResultEntity;
            }
        }
        ruleModels = Arrays.stream(ruleModels)
                .filter(r -> !r.equals(blacklist))
                .toArray(String[]::new);

        // 4. 执行其他规则
        for (String ruleModel : ruleModels) {
            ruleFilter = ruleFilterMap.get(ruleModel);
            ruleResultEntity = doRuleFilter(raffleRequestEntity, ruleFilter, ruleModel);
            if (!RuleDecisionVO.ALLOW.getCode().equals(ruleResultEntity.getCode())) {
                return ruleResultEntity;
            }
        }

        return ruleResultEntity;
    }

    @Override
    protected RuleResultEntity<RuleResultEntity.RuleDuringEntity> checkDuringRule(RaffleRequestEntity raffleRequestEntity, String... ruleModels) {
        // 1. 先检查 ruleModels 是否存在，然后获取在工厂中注册的所有规则
        RuleResultEntity<RuleResultEntity.RuleDuringEntity> check = checkRuleModelsExist(ruleModels);
        if (check != null) return check;

        // 2. 获取所有规则过滤器
        Map<String, IRuleFilter<RuleResultEntity.RuleDuringEntity>> ruleFilterMap = ruleFactory.openLogicFilter();
        IRuleFilter<RuleResultEntity.RuleDuringEntity> ruleFilter;
        RuleResultEntity<RuleResultEntity.RuleDuringEntity> ruleResultEntity;

        // 3. 执行规则
        for (String ruleModel : ruleModels) {
            ruleFilter = ruleFilterMap.get(ruleModel);
            ruleResultEntity = doRuleFilter(raffleRequestEntity, ruleFilter, ruleModel);
            if (!RuleDecisionVO.ALLOW.getCode().equals(ruleResultEntity.getCode())) {
                return ruleResultEntity;
            }
        }

        return null;
    }

    // 执行单个 RuleFilter
    private <T extends RuleResultEntity.RuleDataEntity> RuleResultEntity<T> doRuleFilter(
            RaffleRequestEntity raffleRequestEntity,
            IRuleFilter<T> filter,
            String ruleModel
    ) {
        RuleContextEntity ruleContextEntity = RuleContextEntity.buildRuleContext(raffleRequestEntity, ruleModel);
        log.info("【执行 {}】context = {}", ruleModel, ruleContextEntity);
        RuleResultEntity<T> ruleResultEntity = filter.filter(ruleContextEntity);
        log.info("【执行 {}】result = {}", ruleModel, ruleResultEntity);
        return ruleResultEntity;
    }

    // 检查 ruleModels 是否存在
    private <T extends RuleResultEntity.RuleDataEntity> RuleResultEntity<T> checkRuleModelsExist(String[] ruleModels) {
        if (ruleModels == null || ruleModels.length == 0) {
            return RuleResultEntity.allow();
        }
        return null;
    }
}
