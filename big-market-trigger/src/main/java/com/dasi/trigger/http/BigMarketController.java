package com.dasi.trigger.http;

import com.dasi.api.IBigMarketService;
import com.dasi.api.dto.*;
import com.dasi.domain.activity.model.io.*;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.activity.service.query.IActivityQuery;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.service.query.IBehaviorQuery;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.domain.strategy.model.io.ActivityAwardDetail;
import com.dasi.domain.strategy.model.io.LotteryContext;
import com.dasi.domain.strategy.model.io.LotteryResult;
import com.dasi.domain.strategy.model.io.StrategyRuleWeightDetail;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.domain.strategy.service.query.IStrategyQuery;
import com.dasi.types.model.Result;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/big-market")
public class BigMarketController implements IBigMarketService {

    @Resource
    private IBehaviorReward behaviorReward;

    @Resource
    private IStrategyLottery strategyLottery;

    @Resource
    private IActivityRaffle activityRaffle;

    @Resource
    private IAwardDistribute awardDistribute;

    @Resource
    private IBehaviorQuery behaviorQuery;

    @Resource
    private IActivityQuery activityQuery;

    @Resource
    private IStrategyQuery strategyQuery;

    @Resource
    private IActivityAssemble activityAssemble;

    @Resource
    private IStrategyAssemble strategyAssemble;

    @PostMapping("/assemble")
    @Override
    public Result<Void> assemble(@RequestParam Long activityId) {
        log.info("=========================== 活动装配：activityId={} ===========================", activityId);
        boolean flag1 = activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        boolean flag2 = strategyAssemble.assembleStrategyByActivityId(activityId);
        return flag1 && flag2 ? Result.success("装配活动成功") : Result.error("装配活动失败");
    }

    @PostMapping("/raffle")
    @Override
    public Result<RaffleResponse> raffle(@RequestBody RaffleRequest raffleRequest) {

        String userId = raffleRequest.getUserId();
        Long activityId = raffleRequest.getActivityId();

        // 1. 参与活动
        log.info("=========================== 参与活动：userId={},activityId={} ===========================", userId, activityId);
        RaffleContext raffleContext = RaffleContext.builder().userId(userId).activityId(activityId).build();
        RaffleResult raffleResult = activityRaffle.doActivityRaffle(raffleContext);

        // 2. 执行抽奖
        log.info("=========================== 执行抽奖：userId={},strategyId={} ===========================", userId, raffleResult.getStrategyId());
        LotteryContext lotteryContext = LotteryContext.builder().userId(userId).strategyId(raffleResult.getStrategyId()).build();
        LotteryResult lotteryResult = strategyLottery.doStrategyLottery(lotteryContext);

        // 3. 记录中奖
        log.info("=========================== 记录中奖：userId={},activityId={} ===========================", userId, activityId);
        DistributeContext distributeContext = DistributeContext.builder().userId(userId).activityId(activityId).awardId(lotteryResult.getAwardId()).orderId(raffleResult.getOrderId()).build();
        DistributeResult distributeResult = awardDistribute.doAwardDistribute(distributeContext);

        RaffleResponse raffleResponse = RaffleResponse.builder().awardId(distributeResult.getAwardId()).awardType(distributeResult.getAwardType()).awardName(distributeResult.getAwardName()).build();
        return Result.success(raffleResponse);
    }

    @PostMapping("/behavior/sign")
    @Override
    public Result<BehaviorResponse> behaviorSign(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();

        log.info("=========================== 账户签到：userId={} ===========================", userId);
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.SIGN);

        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    @PostMapping("/behavior/like")
    @Override
    public Result<BehaviorResponse> behaviorLike(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();

        log.info("=========================== 账户点赞：userId={} ===========================", userId);
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.LIKE);

        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    @PostMapping("/behavior/share")
    @Override
    public Result<BehaviorResponse> behaviorShare(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();

        log.info("=========================== 账户分享：userId={} ===========================", userId);
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.SHARE);

        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    @PostMapping("/behavior/comment")
    @Override
    public Result<BehaviorResponse> behaviorComment(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();

        log.info("=========================== 账户评论：userId={} ===========================", userId);
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.COMMENT);

        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    private BehaviorResult behavior(String userId, Long activityId, BehaviorType behaviorType) {
        BehaviorContext behaviorContext = BehaviorContext.builder()
                .userId(userId)
                .activityId(activityId)
                .behaviorType(behaviorType)
                .businessNo(TimeUtil.thisDay(false))
                .build();
        return behaviorReward.doBehaviorReward(behaviorContext);
    }

    @PostMapping("/isSign")
    @Override
    public Result<Boolean> querySign(@RequestBody QuerySign querySign) {
        String userId = querySign.getUserId();
        Long activityId = querySign.getActivityId();

        log.info("=========================== 查询签到：userId={} ===========================", userId);
        Boolean flag = behaviorQuery.querySign(userId, activityId);
        return Result.success(flag);
    }

    @PostMapping("/account")
    @Override
    public Result<QueryAccountResponse> queryActivityAccount(@RequestBody QueryAccountRequest queryAccountRequest) {
        String userId = queryAccountRequest.getUserId();
        Long activityId = queryAccountRequest.getActivityId();

        log.info("=========================== 查询账户：userId={} ===========================", userId);
        QueryAccountContext queryAccountContext = QueryAccountContext.builder().userId(userId).activityId(activityId).build();
        QueryAccountResult queryAccountResult = activityQuery.queryActivityAccount(queryAccountContext);
        QueryAccountResponse queryAccountResponse = QueryAccountResponse.builder()
                .monthKey(queryAccountResult.getMonthKey())
                .dayKey(queryAccountResult.getDayKey())
                .monthLimit(queryAccountResult.getMonthLimit())
                .dayLimit(queryAccountResult.getDayLimit())
                .totalAllocate(queryAccountResult.getTotalAllocate())
                .totalSurplus(queryAccountResult.getTotalSurplus())
                .monthAllocate(queryAccountResult.getMonthAllocate())
                .monthSurplus(queryAccountResult.getMonthSurplus())
                .dayAllocate(queryAccountResult.getDayAllocate())
                .daySurplus(queryAccountResult.getDaySurplus())
                .build();

        return Result.success(queryAccountResponse);
    }


    @PostMapping("/award")
    @Override
    public Result<List<ActivityAwardResponse>> queryActivityAward(@RequestBody ActivityAwardRequest activityAwardRequest) {
        String userId = activityAwardRequest.getUserId();
        Long activityId = activityAwardRequest.getActivityId();

        log.info("=========================== 查询奖品：userId={},activityId={} ===========================", userId, activityId);
        List<ActivityAwardDetail> activityAwardDetailList = strategyQuery.queryActivityAward(userId, activityId);

        List<ActivityAwardResponse> activityAwardResponseList = activityAwardDetailList.stream()
                .map(activityAwardDetail -> ActivityAwardResponse.builder()
                        .awardId(activityAwardDetail.getAwardId())
                        .awardName(activityAwardDetail.getAwardName())
                        .awardTitle(activityAwardDetail.getAwardTitle())
                        .awardDesc(activityAwardDetail.getAwardDesc())
                        .awardConfig(activityAwardDetail.getAwardConfig())
                        .awardRate(activityAwardDetail.getAwardRate())
                        .awardIndex(activityAwardDetail.getAwardIndex())
                        .limitLotteryCount(activityAwardDetail.getLimitLotteryCount())
                        .needLotteryCount(activityAwardDetail.getNeedLotteryCount())
                        .isLock(activityAwardDetail.getIsLock())
                        .build())
                .collect(Collectors.toList());


        return Result.success(activityAwardResponseList);
    }

    @PostMapping("/ruleWeight")
    @Override
    public Result<StrategyRuleWeightResponse> queryStrategyRuleWeight(@RequestBody StrategyRuleWeightRequest strategyRuleWeightRequest) {
        String userId = strategyRuleWeightRequest.getUserId();
        Long activityId = strategyRuleWeightRequest.getActivityId();

        log.info("=========================== 查询权重：userId={},activityId={} ===========================", userId, activityId);
        StrategyRuleWeightDetail strategyRuleWeightDetail = strategyQuery.queryStrategyRuleWeight(userId, activityId);

        StrategyRuleWeightResponse strategyRuleWeightResponse = StrategyRuleWeightResponse.builder()
                .userScore(strategyRuleWeightDetail.getUserScore())
                .prevWeight(strategyRuleWeightDetail.getPrevWeight())
                .nextWeight(strategyRuleWeightDetail.getNextWeight())
                .awardNameList(strategyRuleWeightDetail.getAwardNameList())
                .build();

        return Result.success(strategyRuleWeightResponse);
    }
}
