package com.dasi.trigger.http;

import com.dasi.api.IActivityService;
import com.dasi.api.dto.RaffleRequestDTO;
import com.dasi.api.dto.RaffleResponseDTO;
import com.dasi.api.dto.RechargeRequestDTO;
import com.dasi.api.dto.RechargeResponseDTO;
import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.dto.RaffleResult;
import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.domain.award.model.dto.DistributeContext;
import com.dasi.domain.award.model.dto.DistributeResult;
import com.dasi.domain.award.service.send.IAwardDistribute;
import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.types.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    private IActivityAssemble activityAssemble;

    @Resource
    private IStrategyAssemble strategyAssemble;

    @GetMapping("/assemble")
    @Override
    public Result<Boolean> assemble(@RequestParam Long activityId) {
        boolean flag1 = activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        boolean flag2 = strategyAssemble.assembleStrategyByActivityId(activityId);
        return Result.success(flag1 && flag2);
    }

    @PostMapping("/recharge")
    @Override
    public Result<RechargeResponseDTO> recharge(@RequestBody RechargeRequestDTO rechargeRequestDTO) {
        RechargeContext rechargeContext = RechargeContext.builder().userId(rechargeRequestDTO.getUserId()).skuId(rechargeRequestDTO.getSkuId()).bizId(rechargeRequestDTO.getBizId()).build();
        RechargeResult rechargeResult = skuRecharge.doRecharge(rechargeContext);
        RechargeResponseDTO rechargeResponseDTO = RechargeResponseDTO.builder().orderId(rechargeResult.getOrderId()).totalCount(rechargeResult.getTotalCount()).monthCount(rechargeResult.getMonthCount()).dayCount(rechargeResult.getDayCount()).build();
        return Result.success(rechargeResponseDTO);
    }

    @PostMapping("/raffle")
    @Override
    public Result<RaffleResponseDTO> raffle(@RequestBody RaffleRequestDTO raffleRequestDTO) {
        // 1. 参数校验
        String userId = raffleRequestDTO.getUserId();
        Long activityId = raffleRequestDTO.getActivityId();
        if (StringUtils.isBlank(userId) || activityId == null) {
            return Result.error("参数为空");
        }

        // 2. 参与活动
        log.info("=========================== 参与活动：userId={},activityId={} ===========================", userId, activityId);
        RaffleContext raffleContext = RaffleContext.builder().userId(userId).activityId(activityId).build();
        RaffleResult raffleResult = activityRaffle.doActivityRaffle(raffleContext);
        log.info("=========================== 参与活动：orderId={},strategyId={} ===========================", raffleResult.getOrderId(), raffleResult.getStrategyId());

        // 3. 执行抽奖
        log.info("=========================== 执行抽奖：userId={},strategyId={} ===========================", userId, raffleResult.getStrategyId());
        LotteryContext lotteryContext = LotteryContext.builder().userId(userId).strategyId(raffleResult.getStrategyId()).build();
        LotteryResult lotteryResult = strategyLottery.doStrategyLottery(lotteryContext);
        log.info("=========================== 执行抽奖：awardId={},awardName={} ===========================", lotteryResult.getAwardId(), lotteryResult.getAwardName());

        // 4. 记录中奖
        log.info("=========================== 记录中奖 ===========================");
        DistributeContext distributeContext = DistributeContext.builder().userId(userId).activityId(activityId).awardId(lotteryResult.getAwardId()).awardName(lotteryResult.getAwardName()).strategyId(raffleResult.getStrategyId()).build();
        DistributeResult distributeResult = awardDistribute.doAwardDistribute(distributeContext);
        log.info("=========================== 记录中奖：messageId={} ===========================", distributeResult.getMessageId());

        // 5. 返回结果
        RaffleResponseDTO raffleResponseDTO = RaffleResponseDTO.builder().awardId(distributeResult.getAwardId()).awardName(distributeResult.getAwardName()).build();
        return Result.success(raffleResponseDTO);
    }



}
