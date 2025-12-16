package com.dasi.trigger.http.controller;

import com.dasi.api.IBigMarketService;
import com.dasi.api.dto.*;
import com.dasi.context.UserIdContext;
import com.dasi.domain.activity.model.io.*;
import com.dasi.domain.activity.service.query.IActivityQuery;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.activity.service.recharge.ILuckRecharge;
import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.award.model.io.QueryUserAwardContext;
import com.dasi.domain.award.model.io.QueryUserAwardResult;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.award.service.query.IAwardQuery;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.io.QueryActivityBehaviorContext;
import com.dasi.domain.behavior.model.io.QueryActivityBehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.service.query.IBehaviorQuery;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.domain.point.model.io.*;
import com.dasi.domain.point.service.query.IPointQuery;
import com.dasi.domain.point.service.trade.IPointTrade;
import com.dasi.domain.strategy.model.io.*;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.domain.strategy.service.query.IStrategyQuery;
import com.dasi.types.annotation.CircuitBreaker;
import com.dasi.types.annotation.DCCValue;
import com.dasi.types.annotation.RateLimit;
import com.dasi.types.constant.DefaultValue;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.exception.BusinessException;
import com.dasi.types.model.Result;
import com.dasi.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private IAwardQuery awardQuery;

    @Resource
    private IPointQuery tradeQuery;

    @Resource
    private IPointTrade pointTrade;

    @Resource
    private ILuckRecharge luckRecharge;

    @DCCValue("degradeRaffle:off")
    private String degradeRaffle;

    @DCCValue("degradeTrade:off")
    private String degradeTrade;

    /**
     * 查询活动列表
     * @return activityId, activityName, activityDesc
     */
    @PostMapping("/query/activities")
    @Override
    public Result<List<QueryActivityResponse>> queryActivityList() {
        List<QueryActivityListResult> queryActivityListResultList = activityQuery.queryActivityList();
        List<QueryActivityResponse> queryActivityResponseList = queryActivityListResultList.stream()
                .map(result -> QueryActivityResponse.builder()
                        .activityId(result.getActivityId())
                        .activityName(result.getActivityName())
                        .activityDesc(result.getActivityDesc())
                        .build())
                .collect(Collectors.toList());
        return Result.success(queryActivityResponseList);
    }

    /**
     * 查询当前活动的充值列表
     * @param queryActivityRechargeRequest activityId
     * @return tradeId, tradeMoney, tradeValue, tradeName
     */
    @PostMapping("/query/recharge")
    @Override
    public Result<List<QueryActivityRechargeResponse>> queryActivityRecharge(@RequestBody QueryActivityRechargeRequest queryActivityRechargeRequest) {

        Long activityId = queryActivityRechargeRequest.getActivityId();

        QueryActivityRechargeContext queryActivityRechargeContext = QueryActivityRechargeContext.builder().activityId(activityId).build();
        List<QueryActivityRechargeResult> queryActivityRechargeResultList = tradeQuery.queryActivityRechargeList(queryActivityRechargeContext);
        List<QueryActivityRechargeResponse> queryActivityRechargeResponseList = queryActivityRechargeResultList.stream()
                .map(result -> QueryActivityRechargeResponse.builder()
                        .tradeId(result.getTradeId())
                        .tradeMoney(result.getTradeMoney())
                        .tradeValue(result.getTradeValue())
                        .tradeName(result.getTradeName())
                        .build())
                .collect(Collectors.toList());

        return Result.success(queryActivityRechargeResponseList);
    }


    /**
     * 查询用户在当前活动的基本信息
     *
     * @param queryActivityAccountRequest activityId, userId
     * @return accountPoint, totalSurplus, monthSurplus, daySurplus, monthRecharge, dayRecharge
     */
    @PostMapping("/query/account")
    @Override
    public Result<QueryActivityAccountResponse> queryActivityAccount(@RequestBody QueryActivityAccountRequest queryActivityAccountRequest) {

        String userId = UserIdContext.getUserId();
        Long activityId = queryActivityAccountRequest.getActivityId();

        QueryActivityAccountContext queryActivityAccountContext = QueryActivityAccountContext.builder().userId(userId).activityId(activityId).build();
        QueryActivityAccountResult queryActivityAccountResult = activityQuery.queryActivityAccount(queryActivityAccountContext);
        QueryActivityAccountResponse queryActivityAccountResponse = QueryActivityAccountResponse.builder()
                .accountPoint(queryActivityAccountResult.getAccountPoint())
                .totalSurplus(queryActivityAccountResult.getTotalSurplus())
                .monthSurplus(queryActivityAccountResult.getMonthSurplus())
                .daySurplus(queryActivityAccountResult.getDaySurplus())
                .monthPending(queryActivityAccountResult.getMonthPending())
                .dayPending(queryActivityAccountResult.getDayPending())
                .build();

        return Result.success(queryActivityAccountResponse);
    }

    /**
     * 查询当前活动的积分兑换信息
     *
     * @param queryActivityConvertRequest activityId
     * @return tradeId, tradePoint, tradeName
     */
    @PostMapping("/query/convert")
    @Override
    public Result<List<QueryActivityConvertResponse>> queryActivityConvert(@RequestBody QueryActivityConvertRequest queryActivityConvertRequest) {

        Long activityId = queryActivityConvertRequest.getActivityId();

        QueryActivityConvertContext queryActivityConvertContext = QueryActivityConvertContext.builder().activityId(activityId).build();
        List<QueryActivityConvertResult> queryActivityConvertResultList = tradeQuery.queryActivityConvertList(queryActivityConvertContext);
        List<QueryActivityConvertResponse> queryActivityConvertResponseList = queryActivityConvertResultList.stream()
                .map(queryActivityConvertResult -> QueryActivityConvertResponse.builder()
                        .tradeId(queryActivityConvertResult.getTradeId())
                        .tradePoint(queryActivityConvertResult.getTradePoint())
                        .tradeName(queryActivityConvertResult.getTradeName())
                        .build())
                .collect(Collectors.toList());

        return Result.success(queryActivityConvertResponseList);
    }

    /**
     * 查询用户在当前活动的抽奖奖品列表
     * @param queryActivityAwardRequest activityId, userId
     * @return 每个奖品的元信息，以及在策略下的抽奖信息
     */
    @PostMapping("/query/award")
    @Override
    public Result<List<QueryActivityAwardResponse>> queryActivityAward(@RequestBody QueryActivityAwardRequest queryActivityAwardRequest) {

        String userId = UserIdContext.getUserId();
        Long activityId = queryActivityAwardRequest.getActivityId();

        QueryActivityAwardContext queryActivityAwardContext = QueryActivityAwardContext.builder().userId(userId).activityId(activityId).build();
        List<QueryActivityAwardResult> queryActivityAwardResultList = strategyQuery.queryActivityAward(queryActivityAwardContext);
        List<QueryActivityAwardResponse> queryActivityAwardResponseList = queryActivityAwardResultList.stream()
                .map(queryActivityAwardResult -> QueryActivityAwardResponse.builder()
                        .awardId(queryActivityAwardResult.getAwardId())
                        .awardName(queryActivityAwardResult.getAwardName())
                        .awardRate(queryActivityAwardResult.getAwardRate())
                        .awardIndex(queryActivityAwardResult.getAwardIndex())
                        .needLotteryCount(queryActivityAwardResult.getNeedLotteryCount())
                        .isLock(queryActivityAwardResult.getIsLock())
                        .build())
                .collect(Collectors.toList());

        return Result.success(queryActivityAwardResponseList);
    }

    /**
     * 查询用户在当前活动的获奖信息
     * @param queryUserAwardRequest activityId, userId
     * @return awardId, awardName, awardTime
     */
    @PostMapping("/query/user-award/raffle")
    @Override
    public Result<List<QueryUserAwardResponse>> queryUserAwardRaffle(@RequestBody QueryUserAwardRequest queryUserAwardRequest) {

        Long activityId = queryUserAwardRequest.getActivityId();
        String userId = UserIdContext.getUserId();

        QueryUserAwardContext queryAccountContext = QueryUserAwardContext.builder().userId(userId).activityId(activityId).build();
        List<QueryUserAwardResult> queryUserAwardResultList = awardQuery.queryUserAwardRaffleList(queryAccountContext);
        List<QueryUserAwardResponse> queryUserAwardResponseList = queryUserAwardResultList.stream()
                .map(queryUserAwardResult -> QueryUserAwardResponse.builder()
                        .awardId(queryUserAwardResult.getAwardId())
                        .awardName(queryUserAwardResult.getAwardName())
                        .awardTime(queryUserAwardResult.getAwardTime())
                        .build())
                .collect(Collectors.toList());

        return Result.success(queryUserAwardResponseList);
    }

    /**
     * 查询用户在当前活动的互动任务
     * @param queryActivityBehaviorRequest activityId, userId
     * @return behaviorName, behaviorType, isDone
     */
    @PostMapping("/query/behavior")
    @Override
    public Result<List<QueryActivityBehaviorResponse>> queryActivityBehavior(@RequestBody QueryActivityBehaviorRequest queryActivityBehaviorRequest) {

        String userId = UserIdContext.getUserId();
        Long activityId = queryActivityBehaviorRequest.getActivityId();

        QueryActivityBehaviorContext queryAccountContext = QueryActivityBehaviorContext.builder().userId(userId).activityId(activityId).build();
        List<QueryActivityBehaviorResult> queryActivityBehaviorResultList = behaviorQuery.queryBehavior(queryAccountContext);
        List<QueryActivityBehaviorResponse> queryUserAwardResponseList = queryActivityBehaviorResultList.stream()
                .map(queryActivityBehaviorResult -> QueryActivityBehaviorResponse.builder()
                        .behaviorName(queryActivityBehaviorResult.getBehaviorName())
                        .behaviorType(queryActivityBehaviorResult.getBehaviorType())
                        .rewardDesc(queryActivityBehaviorResult.getRewardDesc())
                        .isDone(queryActivityBehaviorResult.getIsDone())
                        .build())
                .collect(Collectors.toList());

        return Result.success(queryUserAwardResponseList);
    }

    /**
     * 查询用户在当前活动的幸运值情况
     * @param queryActivityLuckRequest activityId, userId
     * @return accountLuck, luckThreshold
     */
    @PostMapping("/query/luck")
    @Override
    public Result<QueryActivityLuckResponse> queryActivityLuck(@RequestBody QueryActivityLuckRequest queryActivityLuckRequest) {

        String userId = UserIdContext.getUserId();
        Long activityId = queryActivityLuckRequest.getActivityId();

        QueryActivityLuckContext queryActivityLuckContext = QueryActivityLuckContext.builder().userId(userId).activityId(activityId).build();
        QueryActivityLuckResult queryActivityLuckResult = strategyQuery.queryActivityLuck(queryActivityLuckContext);
        QueryActivityLuckResponse queryActivityLuckResponse = QueryActivityLuckResponse.builder()
                .accountLuck(queryActivityLuckResult.getAccountLuck())
                .luckThreshold(queryActivityLuckResult.getLuckThreshold())
                .build();

        return Result.success(queryActivityLuckResponse);
    }

    /**
     * 查询活动信息
     * @param activityInfoRequest activityId
     * @return 活动的基本信息和参与情况
     */
    @PostMapping("/query/info")
    @Override
    public Result<QueryActivityInfoResponse> queryActivityInfo(@RequestBody QueryActivityInfoRequest activityInfoRequest) {

        Long activityId = activityInfoRequest.getActivityId();

        QueryActivityInfoContext queryActivityInfoContext = QueryActivityInfoContext.builder().activityId(activityId).build();
        QueryActivityInfoResult activityInfoResult = activityQuery.queryActivityInfo(queryActivityInfoContext);
        QueryActivityInfoResponse queryActivityInfoResponse = QueryActivityInfoResponse.builder()
                .activityName(activityInfoResult.getActivityName())
                .activityDesc(activityInfoResult.getActivityDesc())
                .activityBeginTime(activityInfoResult.getActivityBeginTime())
                .activityEndTime(activityInfoResult.getActivityEndTime())
                .activityAccountCount(activityInfoResult.getActivityAccountCount())
                .activityAwardCount(activityInfoResult.getActivityAwardCount())
                .activityRaffleCount(activityInfoResult.getActivityRaffleCount())
                .build();

        return Result.success(queryActivityInfoResponse);
    }


    /**
     * 执行用户在当前活动的互动行为
     * @param behaviorRequest userId, activityId, behaviorType
     * @return rewardDescList
     */
    @PostMapping("/behavior")
    @Override
    public Result<BehaviorResponse> behavior(@RequestBody BehaviorRequest behaviorRequest) {

        String userId = UserIdContext.getUserId();
        Long activityId = behaviorRequest.getActivityId();
        String behaviorType = behaviorRequest.getBehaviorType();
        String businessNo = TimeUtil.thisDay(false);

        BehaviorContext behaviorContext = BehaviorContext.builder().userId(userId).activityId(activityId).behaviorType(BehaviorType.valueOf(behaviorType)).businessNo(businessNo).build();
        BehaviorResult behaviorResult = behaviorReward.doBehaviorReward(behaviorContext);
        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();

        return Result.success(behaviorResponse);
    }

    /**
     * 执行用户在当前活动的积分兑换
     * @param tradeRequest userId, activityId, tradeId
     * @return tradeDesc
     */
    @PostMapping("/trade")
    @Override
    public Result<TradeResponse> trade(@RequestBody TradeRequest tradeRequest) {

        if (degradeTrade.equals(DefaultValue.TOGGLE_ON)) {
            throw new BusinessException(ExceptionMessage.DEGRADE_ERROR);
        }

        String userId = UserIdContext.getUserId();
        Long tradeId = tradeRequest.getTradeId();
        Long activityId = tradeRequest.getActivityId();
        String businessNo = TimeUtil.thisDay(false);

        TradeContext tradeContext = TradeContext.builder().userId(userId).tradeId(tradeId).activityId(activityId).businessNo(businessNo).build();
        TradeResult tradeResult = pointTrade.doPointTrade(tradeContext);
        TradeResponse tradeResponse = TradeResponse.builder().tradeDesc(tradeResult.getTradeDesc()).build();

        return Result.success(tradeResponse);
    }

    /**
     * 增加用户在当前活动的幸运值
     * @param fortuneRequest userId, activityId, luck
     * @return accountLuck
     */
    @PostMapping("/fortune")
    @Override
    public Result<FortuneResponse> fortune(@RequestBody FortuneRequest fortuneRequest) {

        String userId = UserIdContext.getUserId();
        Long activityId = fortuneRequest.getActivityId();
        Integer luck = fortuneRequest.getLuck();

        FortuneContext fortuneContext = FortuneContext.builder().userId(userId).activityId(activityId).luck(luck).build();
        FortuneResult fortuneResult = luckRecharge.addFortune(fortuneContext);
        FortuneResponse fortuneResponse = FortuneResponse.builder().accountLuck(fortuneResult.getAccountLuck()).build();

        return Result.success(fortuneResponse);
    }

    /**
     * 用户在当前活动执行抽奖
     * @param raffleRequest activityId, userId
     * @return awardId, isLock, isEmpty
     */
    @PostMapping("/raffle")
    @RateLimit(fallbackMethod = "raffleRateLimitFallBack")
    @CircuitBreaker(fallbackMethod = "raffleCircuitBreakerFallBack")
    @Override
    public Result<RaffleResponse> raffle(@RequestBody RaffleRequest raffleRequest) {

        int i = 1 / 0;

        if (degradeRaffle.equals(DefaultValue.TOGGLE_ON)) {
            throw new BusinessException(ExceptionMessage.DEGRADE_ERROR);
        }

        String userId = UserIdContext.getUserId();
        Long activityId = raffleRequest.getActivityId();

        // 1. 参与活动
        RaffleContext raffleContext = RaffleContext.builder().userId(userId).activityId(activityId).build();
        RaffleResult raffleResult = activityRaffle.doActivityRaffle(raffleContext);

        // 2. 执行抽奖
        LotteryContext lotteryContext = LotteryContext.builder().userId(userId).strategyId(raffleResult.getStrategyId()).build();
        LotteryResult lotteryResult = strategyLottery.doStrategyLottery(lotteryContext);

        // 3. 记录中奖
        DistributeContext distributeContext = DistributeContext.builder().userId(userId).activityId(activityId).awardId(lotteryResult.getFinalAwardId()).orderId(raffleResult.getOrderId()).build();
        DistributeResult distributeResult = awardDistribute.doAwardDistribute(distributeContext);

        RaffleResponse raffleResponse = RaffleResponse.builder()
                .awardId(lotteryResult.getOriginalAwardId())
                .awardName(distributeResult.getAwardName())
                .isLock(lotteryResult.getIsLock())
                .isEmpty(lotteryResult.getIsEmpty()).build();
        return Result.success(raffleResponse);
    }

    public Result<RaffleResponse> raffleRateLimitFallBack(RaffleRequest raffleRequest) {
        return Result.error(ExceptionMessage.RATE_LIMIT_ERROR);
    }

    public Result<RaffleResponse> raffleCircuitBreakerFallBack(RaffleRequest raffleRequest) {
        return Result.error(ExceptionMessage.CIRCUIT_BREAKER_ERROR);
    }

}
