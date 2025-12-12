package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.activity.model.io.SkuRechargeContext;
import com.dasi.domain.activity.model.io.SkuRechargeResult;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.domain.behavior.event.DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.type.RewardState;
import com.dasi.domain.behavior.model.type.RewardType;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.domain.point.model.io.TradeContext;
import com.dasi.domain.point.model.io.TradeResult;
import com.dasi.domain.point.service.trade.IPointTrade;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchBehaviorReward {

    @Resource
    private ISkuRecharge skuRecharge;

    @Resource
    private IPointTrade tradePoint;

    @Resource
    private IBehaviorReward behaviorReward;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.dispatch_behavior_reward}"))
    public void dispatchBehaviorReward(String message) {

        // 1. 解析消息
        BaseEvent.EventMessage<DispatchBehaviorRewardMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<DispatchBehaviorRewardMessage>>() {}.getType());
        DispatchBehaviorRewardMessage dispatchBehaviorRewardMessage = eventMessage.getData();
        String userId = dispatchBehaviorRewardMessage.getUserId();
        String bizId = dispatchBehaviorRewardMessage.getBizId();
        String orderId = dispatchBehaviorRewardMessage.getOrderId();

        RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder().userId(userId).bizId(bizId).orderId(orderId).build();
        RewardType rewardType = dispatchBehaviorRewardMessage.getRewardType();
        Long rewardValue = Long.parseLong(dispatchBehaviorRewardMessage.getRewardValue());

        try {

            // 2. 处理返利
            switch (rewardType) {
                case SKU:
                    SkuRechargeContext skuRechargeContext = SkuRechargeContext.builder().userId(userId).bizId(bizId).skuId(rewardValue).build();
                    SkuRechargeResult skuRechargeResult = skuRecharge.doSkuRecharge(skuRechargeContext);
                    log.debug("{}", skuRechargeResult);
                    break;
                case POINT:
                    TradeContext tradeContext = TradeContext.builder().userId(userId).bizId(bizId).tradeId(rewardValue).build();
                    TradeResult tradeResult = tradePoint.doPointTrade(tradeContext);
                    log.debug("{}", tradeResult);
                    break;
                default:
                    throw new AppException("奖励类型不存在：rewardType=" + rewardType);
            }

            // 3. 改变状态
            rewardOrderEntity.setRewardState(RewardState.USED);
            behaviorReward.updateRewardOrderState(rewardOrderEntity);

        } catch (Exception e) {
            rewardOrderEntity.setRewardState(RewardState.CANCELLED);
            behaviorReward.updateRewardOrderState(rewardOrderEntity);
            log.error("分发行为返利失败", e);
        }
    }

}
