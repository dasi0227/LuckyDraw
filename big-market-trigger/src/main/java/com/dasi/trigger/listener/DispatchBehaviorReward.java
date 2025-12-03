package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.activity.model.io.RechargeContext;
import com.dasi.domain.activity.model.io.RechargeResult;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.domain.behavior.event.DistributeBehaviorRewardEvent.DistributeBehaviorRewardMessage;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.type.RewardState;
import com.dasi.domain.behavior.model.type.RewardType;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.types.event.BaseEvent;
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
    private IBehaviorReward behaviorReward;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.distribute_behavior_reward}"))
    public void dispatchBehaviorReward(String message) {
        // 1. 解析消息
        BaseEvent.EventMessage<DistributeBehaviorRewardMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<DistributeBehaviorRewardMessage>>() {}.getType());
        DistributeBehaviorRewardMessage distributeBehaviorRewardMessage = eventMessage.getData();
        String userId = distributeBehaviorRewardMessage.getUserId();
        String bizId = distributeBehaviorRewardMessage.getBizId();
        String orderId = distributeBehaviorRewardMessage.getOrderId();
        RewardType rewardType = distributeBehaviorRewardMessage.getRewardType();
        String rewardValue = distributeBehaviorRewardMessage.getRewardValue();

        RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                .userId(userId)
                .bizId(bizId)
                .orderId(orderId)
                .build();

        try {
            // 2. 处理 SKU
            if (rewardType.equals(RewardType.SKU)) {
                Long skuId = Long.valueOf(rewardValue);
                RechargeContext rechargeContext = RechargeContext.builder()
                        .userId(userId)
                        .bizId(bizId)
                        .skuId(skuId)
                        .build();
                log.info("【发放】增加抽奖次数：userId={}, rewardType={}, rewardValue={}", userId, rewardType, rewardValue);
                RechargeResult rechargeResult = skuRecharge.doSkuRecharge(rechargeContext);
                log.info("【发放】用户充值成功：userId={}, orderId={}", userId, rechargeResult.getOrderId());
            }

            // 3. 处理积分
            if (rewardType.equals(RewardType.POINT)) {
                log.info("【发放】增加积分：userId={}, rewardType={}, rewardValue={}", userId, rewardType, rewardValue);
            }

            // 4. 改变状态
            rewardOrderEntity.setRewardState(RewardState.USED);
            int count = behaviorReward.updateRewardOrderState(rewardOrderEntity);
            if (count == 1) {
                log.info("【发放】更新返利订单成功：orderId={}", orderId);
            } else {
                rewardOrderEntity.setRewardState(RewardState.CANCELLED);
                behaviorReward.updateRewardOrderState(rewardOrderEntity);
                log.info("【发放】更新返利订单失败：orderId={}", orderId);
            }
        } catch (Exception e) {
            rewardOrderEntity.setRewardState(RewardState.CANCELLED);
            behaviorReward.updateRewardOrderState(rewardOrderEntity);
            log.error("【发放】获取返利失败：error={}", e.getMessage());
        }
    }

}
