package com.dasi.trigger.http;

import com.dasi.api.IBigMarketService;
import com.dasi.api.dto.*;
import com.dasi.domain.activity.model.io.QueryAccountContext;
import com.dasi.domain.activity.model.io.QueryAccountResult;
import com.dasi.domain.activity.model.io.RaffleContext;
import com.dasi.domain.activity.model.io.RaffleResult;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.activity.service.query.IActivityQuery;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.service.query.IBehaviorQuery;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.domain.trade.model.io.ConvertContext;
import com.dasi.domain.trade.model.io.QueryConvertContext;
import com.dasi.domain.trade.model.io.QueryConvertResult;
import com.dasi.domain.trade.model.io.ConvertResult;
import com.dasi.domain.trade.service.query.ITradeQuery;
import com.dasi.domain.trade.service.trade.IPointTrade;
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
    private ITradeQuery tradeQuery;

    @Resource
    private IPointTrade pointTrade;

    @Resource
    private IActivityAssemble activityAssemble;

    @Resource
    private IStrategyAssemble strategyAssemble;

    /**
     * 获取活动的交易信息
     *
     * @param queryConvertRequest activityId
     * @return tradeId、tradePoint、tradeName
     */
    @PostMapping("/query/convert")
    @Override
    public Result<List<QueryConvertResponse>> queryConvert(@RequestBody QueryConvertRequest queryConvertRequest) {

        Long activityId = queryConvertRequest.getActivityId();

        QueryConvertContext queryConvertContext = QueryConvertContext.builder().activityId(activityId).build();
        List<QueryConvertResult> queryConvertResultList = tradeQuery.queryConvertListByActivityId(queryConvertContext);
        List<QueryConvertResponse> queryConvertResponseList = queryConvertResultList.stream()
                .map(queryConvertResult -> QueryConvertResponse.builder()
                        .tradeId(queryConvertResult.getTradeId())
                        .tradePoint(queryConvertResult.getTradePoint())
                        .tradeName(queryConvertResult.getTradeName())
                        .build())
                .collect(Collectors.toList());

        return Result.success(queryConvertResponseList);

    }

    /**
     * 获取用户在当前活动的信息
     *
     * @param queryAccountRequest activityId、userId
     * @return tradeId、tradePoint、tradeName
     */
    @PostMapping("/query/account")
    @Override
    public Result<QueryAccountResponse> queryActivityAccount(@RequestBody QueryAccountRequest queryAccountRequest) {

        String userId = queryAccountRequest.getUserId();
        Long activityId = queryAccountRequest.getActivityId();

        QueryAccountContext queryAccountContext = QueryAccountContext.builder().userId(userId).activityId(activityId).build();
        QueryAccountResult queryAccountResult = activityQuery.queryActivityAccount(queryAccountContext);
        QueryAccountResponse queryAccountResponse = QueryAccountResponse.builder()
                .userPoint(queryAccountResult.getUserPoint())
                .totalSurplus(queryAccountResult.getTotalSurplus())
                .monthSurplus(queryAccountResult.getMonthSurplus())
                .daySurplus(queryAccountResult.getDaySurplus())
                .monthRecharge(queryAccountResult.getMonthRecharge())
                .dayRecharge(queryAccountResult.getDayRecharge())
                .build();

        return Result.success(queryAccountResponse);
    }



    @PostMapping("/assemble")
    @Override
    public Result<Void> assemble(@RequestParam Long activityId) {
        boolean flag1 = activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        boolean flag2 = strategyAssemble.assembleStrategyByActivityId(activityId);
        return flag1 && flag2 ? Result.success("装配活动成功") : Result.error("装配活动失败");
    }

    @PostMapping("/convert")
    @Override
    public Result<ConvertResponse> convert(@RequestBody ConvertRequest convertRequest) {

        String userId = convertRequest.getUserId();
        Long tradeId = convertRequest.getTradeId();
        Long activityId = convertRequest.getActivityId();

        ConvertContext convertContext = ConvertContext.builder().userId(userId).tradeId(tradeId).businessNo(TimeUtil.thisDay(false)).activityId(activityId).build();
        ConvertResult convertResult = pointTrade.doPointTrade(convertContext);
        ConvertResponse convertResponse = ConvertResponse.builder().tradeDesc(convertResult.getTradeDesc()).build();
        return Result.success(convertResponse);
    }

    @PostMapping("/raffle")
    @Override
    public Result<RaffleResponse> raffle(@RequestBody RaffleRequest raffleRequest) {

        String userId = raffleRequest.getUserId();
        Long activityId = raffleRequest.getActivityId();

        // 1. 参与活动
        RaffleContext raffleContext = RaffleContext.builder().userId(userId).activityId(activityId).build();
        RaffleResult raffleResult = activityRaffle.doActivityRaffle(raffleContext);

        // 2. 执行抽奖
        LotteryContext lotteryContext = LotteryContext.builder().userId(userId).strategyId(raffleResult.getStrategyId()).build();
        LotteryResult lotteryResult = strategyLottery.doStrategyLottery(lotteryContext);

        // 3. 记录中奖
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
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.SIGN);
        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    @PostMapping("/behavior/like")
    @Override
    public Result<BehaviorResponse> behaviorLike(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.LIKE);
        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    @PostMapping("/behavior/share")
    @Override
    public Result<BehaviorResponse> behaviorShare(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();
        BehaviorResult behaviorResult = behavior(userId, activityId, BehaviorType.SHARE);
        BehaviorResponse behaviorResponse = BehaviorResponse.builder().rewardDescList(behaviorResult.getRewardDescList()).build();
        return Result.success(behaviorResponse);
    }

    @PostMapping("/behavior/comment")
    @Override
    public Result<BehaviorResponse> behaviorComment(@RequestBody BehaviorRequest behaviorRequest) {
        String userId = behaviorRequest.getUserId();
        Long activityId = behaviorRequest.getActivityId();
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
                        .awardValue(activityAwardDetail.getAwardValue())
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
