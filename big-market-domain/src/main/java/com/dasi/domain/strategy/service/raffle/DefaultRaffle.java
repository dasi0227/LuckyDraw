package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.dto.RaffleRequestDTO;
import com.dasi.domain.strategy.model.dto.FilterResponse;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.armory.IStrategyLottery;
import com.dasi.domain.strategy.service.rule.chain.IRuleChain;
import com.dasi.domain.strategy.service.rule.chain.RuleChainFactory;
import com.dasi.domain.strategy.service.rule.filter.RuleFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DefaultRaffle extends AbstractRaffle {

    @Resource
    private RuleFilterFactory ruleFilterFactory;

    @Resource
    private RuleChainFactory ruleChainFactory;

    public DefaultRaffle(IStrategyRepository repository, IStrategyLottery strategyDispatch) {
        super(repository, strategyDispatch);
    }

    @Override
    protected Integer beforeCheck(RaffleRequestDTO raffleRequestDTO) {
        IRuleChain firstRuleChain = ruleChainFactory.getFirstRuleChain(raffleRequestDTO.getStrategyId());
        return firstRuleChain.logic(raffleRequestDTO.getUserId(), raffleRequestDTO.getStrategyId());
    }

//    // TODO：重构 during check，暂时先测试前置规则
//    @Override
//    protected FilterResponse<FilterResponse.FilterDuringEntity> checkDuringRule(RaffleRequestDTO raffleRequest, String... ruleModels) {
//        // 1. 先检查 ruleModels 是否存在，然后获取在工厂中注册的所有规则
//        FilterResponse<FilterResponse.FilterDuringEntity> check = checkRuleModelsExist(ruleModels);
//        if (check != null) return check;
//
//        // 2. 获取所有规则过滤器
//        Map<String, IRuleFilter<FilterResponse.FilterDuringEntity>> ruleFilterMap = ruleFilterFactory.openLogicFilter();
//        IRuleFilter<FilterResponse.FilterDuringEntity> ruleFilter;
//        FilterResponse<FilterResponse.FilterDuringEntity> filterResponse;
//
//        // 3. 执行规则
//        for (String ruleModel : ruleModels) {
//            ruleFilter = ruleFilterMap.get(ruleModel);
//            FilterRequest filterRequest = FilterRequest.buildFilterRequest(raffleRequest, ruleModel);
//            log.info("【执行 {}】context = {}", ruleModel, filterRequest);
//            filterResponse = ruleFilter.filter(filterRequest);
//            log.info("【执行 {}】result = {}", ruleModel, filterResponse);
//            if (!RuleCheckResult.ALLOW.getCode().equals(filterResponse.getCode())) {
//                return filterResponse;
//            }
//        }
//
//        return null;
//    }

    // 检查 ruleModels 是否存在
    private <T extends FilterResponse.FilterDataEntity> FilterResponse<T> checkRuleModelsExist(String[] ruleModels) {
        if (ruleModels == null || ruleModels.length == 0) {
            return FilterResponse.allow();
        }
        return null;
    }
}
