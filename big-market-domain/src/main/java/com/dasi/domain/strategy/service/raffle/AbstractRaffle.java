package com.dasi.domain.strategy.service.raffle;

import com.dasi.domain.strategy.model.dto.RaffleRequestDTO;
import com.dasi.domain.strategy.model.dto.RaffleResponseDTO;
import com.dasi.domain.strategy.model.entity.*;
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
    public RaffleResponseDTO doRaffle(RaffleRequestDTO raffleRequestDTO) {
        // 1. 校验输入
        String userId = raffleRequestDTO.getUserId();
        Long strategyId = raffleRequestDTO.getStrategyId();
        if (StringUtils.isBlank(userId) || strategyId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 执行前置检查
        Integer awardId = beforeCheck(raffleRequestDTO);
        raffleRequestDTO.setAwardId(awardId);


//        // TODO：中置和后置还需要继续重构，raffle的输入输出和check的输入输出可能需要重新定义
//        // 3. 执行中置检查
//        Integer awardId = duringCheck(raffleRequestDTO);
//        String[] duringRuleModels = strategyRepository.queryStrategyAwardRuleModels(strategyId, awardId).getDuringRuleModels();
//        raffleRequestDTO.setAwardId(awardId);
//        FilterResponse<FilterResponse.FilterDuringEntity> ruleDuringEntity = this.checkDuringRule(
//                raffleRequestDTO,
//                duringRuleModels
//        );
//        if (RuleCheckResult.TAKE_OVER.getCode().equals(ruleDuringEntity.getCode())) {
//            log.info("【临时处理】中置规则成功拦截");
//            return RaffleResponseDTO.builder().awardDesc("临时处理，中置规则成功拦截").build();
//        }
//
//        // 4. 执行后置检查
//        Integer awardId = afterCheck(raffleRequestDTO);

        // 5. 返回结果
        AwardEntity awardEntity = strategyRepository.queryAwardEntityByAwardId(awardId);
        return RaffleResponseDTO.buildAward(strategyId, awardEntity);
    }

//    protected abstract Integer duringCheck(RaffleRequestDTO raffleRequestDTO);
//    protected abstract Integer afterCheck(RaffleRequestDTO raffleRequestDTO);
    protected abstract Integer beforeCheck(RaffleRequestDTO raffleRequestDTO);

}
