package com.dasi.trigger.http;

import com.dasi.api.IActivityService;
import com.dasi.api.dto.*;
import com.dasi.domain.activity.model.io.RaffleContext;
import com.dasi.domain.activity.model.io.RaffleResult;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.entity.StrategyAwardEntity;
import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.award.service.query.IAwardQuery;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.domain.strategy.model.io.LotteryContext;
import com.dasi.domain.strategy.model.io.LotteryResult;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.types.model.Result;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/activity")
public class ActivityController implements IActivityService {

    @Resource
    private IBehaviorReward behaviorReward;

    @Resource
    private IStrategyLottery strategyLottery;

    @Resource
    private IActivityRaffle activityRaffle;

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
        log.info("=========================== 活动装配：activityId={} ===========================", activityId);
        boolean flag1 = activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        boolean flag2 = strategyAssemble.assembleStrategyByActivityId(activityId);
        return Result.success(flag1 && flag2);
    }

    @PostMapping("/award")
    @Override
    public Result<List<AwardListResponseDTO>> award(@RequestBody AwardListRequestDTO awardListRequestDTO) {
        String userId = awardListRequestDTO.getUserId();
        Long activityId = awardListRequestDTO.getActivityId();

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
                    Long awardId = strategyAwardEntity.getAwardId();
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

    @PostMapping("/behavior")
    @Override
    public Result<BehaviorSignResponseDTO> behavior(@RequestBody BehaviorSignRequestDTO behaviorSignRequestDTO) {
        String userId = behaviorSignRequestDTO.getUserId();
        Long activityId = behaviorSignRequestDTO.getActivityId();
        String behaviorType = behaviorSignRequestDTO.getBehaviorType();

        log.info("=========================== 账户活动：userId={},behavior={} ===========================", userId, behaviorType);
        BehaviorContext behaviorContext = BehaviorContext.builder()
                .userId(userId)
                .activityId(activityId)
                .behaviorType(BehaviorType.valueOf(behaviorType.toUpperCase()))
                .businessNo(TimeUtil.thisDay(false))
                .build();
        BehaviorResult behaviorResult = behaviorReward.doBehaviorReward(behaviorContext);

        BehaviorSignResponseDTO behaviorSignResponseDTO = BehaviorSignResponseDTO.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorSignResponseDTO);
    }

    @PostMapping("/raffle")
    @Override
    public Result<RaffleResponseDTO> raffle(@RequestBody RaffleRequestDTO raffleRequestDTO) {
        String userId = raffleRequestDTO.getUserId();
        Long activityId = raffleRequestDTO.getActivityId();

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
