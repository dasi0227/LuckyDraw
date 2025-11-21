package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.entity.*;
import com.dasi.domain.strategy.model.io.RaffleRequest;
import com.dasi.domain.strategy.model.io.RaffleResponse;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.armory.IStrategyLottery;
import com.dasi.types.enums.ResponseCode;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractRaffle implements IRaffle {

    protected final IStrategyRepository strategyRepository;

    protected final IStrategyLottery strategyLottery;

    public AbstractRaffle(IStrategyRepository strategyRepository, IStrategyLottery strategyLottery) {
        this.strategyRepository = strategyRepository;
        this.strategyLottery = strategyLottery;
    }

    @Override
    public RaffleResponse doRaffle(RaffleRequest raffleRequest) {
        // 1. 校验输入
        String userId = raffleRequest.getUserId();
        Long strategyId = raffleRequest.getStrategyId();
        if (StringUtils.isBlank(userId) || strategyId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 执行前置检查
        Integer awardId = beforeCheck(raffleRequest);
        raffleRequest.setAwardId(awardId);


//        // TODO：中置和后置还需要继续重构，raffle的输入输出和check的输入输出可能需要重新定义
//        // 3. 执行中置检查
//        Integer awardId = duringCheck(raffleRequest);
//        String[] duringRuleModels = strategyRepository.queryStrategyAwardRuleModels(strategyId, awardId).getDuringRuleModels();
//        raffleRequest.setAwardId(awardId);
//        FilterResponse<FilterResponse.FilterDuringEntity> ruleDuringEntity = this.checkDuringRule(
//                raffleRequest,
//                duringRuleModels
//        );
//        if (FilterDecision.TAKE_OVER.getCode().equals(ruleDuringEntity.getCode())) {
//            log.info("【临时处理】中置规则成功拦截");
//            return RaffleResponse.builder().awardDesc("临时处理，中置规则成功拦截").build();
//        }
//
//        // 4. 执行后置检查
//        Integer awardId = afterCheck(raffleRequest);

        // 5. 返回结果
        AwardEntity awardEntity = strategyRepository.queryAwardEntityByAwardId(awardId);
        return RaffleResponse.buildAward(strategyId, awardEntity);
    }

//    protected abstract Integer duringCheck(RaffleRequest raffleRequest);
//    protected abstract Integer afterCheck(RaffleRequest raffleRequest);
    protected abstract Integer beforeCheck(RaffleRequest raffleRequest);

}
