package com.dasi.trigger.http;

import com.dasi.api.IActivityService;
import com.dasi.api.dto.*;
import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.dto.RaffleResult;
import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.domain.award.model.dto.DistributeContext;
import com.dasi.domain.award.model.dto.DistributeResult;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.award.service.query.IAwardQuery;
import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.types.exception.AppException;
import com.dasi.types.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/activity")
public class IActivityController implements IActivityService {

    @Resource
    private IStrategyLottery strategyLottery;

    @Resource
    private IActivityRaffle activityRaffle;

    @Resource
    private ISkuRecharge skuRecharge;

    @Resource
    private IAwardDistribute awardDistribute;

    @Resource
    private IAwardQuery awardQuery;

    @Resource
    private IActivityAssemble activityAssemble;

    @Resource
    private IStrategyAssemble strategyAssemble;

    @GetMapping("/assemble")
    @Override
    public Result<Boolean> assemble(@RequestParam Long activityId) {
        if (activityId == null) {
            throw new AppException("API 请求参数为空");
        }

        log.info("=========================== 活动装配：activity=Id{} ===========================", activityId);
        boolean flag1 = activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        boolean flag2 = strategyAssemble.assembleStrategyByActivityId(activityId);
        return Result.success(flag1 && flag2);
    }

    @PostMapping("/award")
    @Override
    public Result<List<AwardListResponseDTO>> award(@RequestBody AwardListRequestDTO awardListRequestDTO) {
        String userId = awardListRequestDTO.getUserId();
        Long activityId = awardListRequestDTO.getActivityId();
        if (StringUtils.isBlank(userId) || activityId == null) {
            throw new AppException("API 请求参数为空");
        }

        // 1. 先拿到当前活动对应的策略的所有奖品
        List<StrategyAwardEntity> strategyAwardEntityList = awardQuery.queryStrategyAwardListByActivityId(activityId);

        // 2. 查询奖品的详细信息
        Map<String, AwardEntity> awardEntityMap = awardQuery.queryAwardMapByActivityId(strategyAwardEntityList, activityId);

        // 3. 查询策略奖品的详细信息
        Map<String, Integer> limitLotteryCountMap = awardQuery.queryRuleNodeLockCountMapByActivityId(strategyAwardEntityList, activityId);

        // 4. 查询用户的详细信息
        Integer userLotteryCount = awardQuery.queryUserLotteryCount(userId, activityId);
        Map<String, Integer> needLotteryCountMap = limitLotteryCountMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Math.max(entry.getValue() - userLotteryCount, 0)));

        // 5. 组装结果返回
        List<AwardListResponseDTO> awardListResponseDTOList = strategyAwardEntityList.stream()
                .map(strategyAwardEntity -> {
                    Integer awardId = strategyAwardEntity.getAwardId();
                    String key = String.valueOf(awardId);
                    return AwardListResponseDTO.builder()
                            .awardId(awardId)
                            .awardTitle(strategyAwardEntity.getAwardTitle())
                            .awardRate(strategyAwardEntity.getAwardRate())
                            .awardIndex(strategyAwardEntity.getAwardIndex())
                            .awardName(awardEntityMap.get(key).getAwardName())
                            .awardConfig(awardEntityMap.get(key).getAwardConfig())
                            .awardDesc(awardEntityMap.get(key).getAwardDesc())
                            .limitLotteryCount(limitLotteryCountMap.get(key))
                            .needLotteryCount(needLotteryCountMap.get(key))
                            .isLock(needLotteryCountMap.get(key) > 0)
                            .build();
                })
                .collect(Collectors.toList());

        return Result.success(awardListResponseDTOList);
    }

    @PostMapping("/recharge")
    @Override
    public Result<RechargeResponseDTO> recharge(@RequestBody RechargeRequestDTO rechargeRequestDTO) {
        String userId = rechargeRequestDTO.getUserId();
        String bizId = rechargeRequestDTO.getBizId();
        Long skuId = rechargeRequestDTO.getSkuId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(bizId) || skuId == null) {
            throw new AppException("API 请求参数为空");
        }

        log.info("=========================== 账户充值：userId={},skuId={} ===========================", userId, skuId);
        RechargeContext rechargeContext = RechargeContext.builder().userId(userId).skuId(skuId).bizId(bizId).build();
        RechargeResult rechargeResult = skuRecharge.doRecharge(rechargeContext);
        RechargeResponseDTO rechargeResponseDTO = RechargeResponseDTO.builder().orderId(rechargeResult.getOrderId()).totalCount(rechargeResult.getTotalCount()).monthCount(rechargeResult.getMonthCount()).dayCount(rechargeResult.getDayCount()).build();
        return Result.success(rechargeResponseDTO);
    }

    @PostMapping("/raffle")
    @Override
    public Result<RaffleResponseDTO> raffle(@RequestBody RaffleRequestDTO raffleRequestDTO) {
        String userId = raffleRequestDTO.getUserId();
        Long activityId = raffleRequestDTO.getActivityId();
        if (StringUtils.isBlank(userId) || activityId == null) {
            throw new AppException("API 请求参数为空");
        }

        // 1. 参与活动
        log.info("=========================== 参与活动：userId={},activityId={} ===========================", userId, activityId);
        RaffleContext raffleContext = RaffleContext.builder().userId(userId).activityId(activityId).build();
        RaffleResult raffleResult = activityRaffle.doActivityRaffle(raffleContext);

        // 2. 执行抽奖
        log.info("=========================== 执行抽奖：userId={},strategyId={} ===========================", userId, raffleResult.getStrategyId());
        LotteryContext lotteryContext = LotteryContext.builder().userId(userId).strategyId(raffleResult.getStrategyId()).build();
        LotteryResult lotteryResult = strategyLottery.doStrategyLottery(lotteryContext);

        // 3. 记录中奖
        log.info("=========================== 记录中奖 ===========================");
        DistributeContext distributeContext = DistributeContext.builder().userId(userId).activityId(activityId).awardId(lotteryResult.getAwardId()).awardName(lotteryResult.getAwardName()).strategyId(raffleResult.getStrategyId()).orderId(raffleResult.getOrderId()).build();
        DistributeResult distributeResult = awardDistribute.doAwardDistribute(distributeContext);

        RaffleResponseDTO raffleResponseDTO = RaffleResponseDTO.builder().awardId(distributeResult.getAwardId()).awardName(distributeResult.getAwardName()).build();
        return Result.success(raffleResponseDTO);
    }



}
