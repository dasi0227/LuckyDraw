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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchBehaviorReward {

    @Value("${spring.rabbitmq.topic.distribute_behavior_reward}")
    private String topic;

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
        RewardType rewardType = distributeBehaviorRewardMessage.getRewardType();
        String rewardValue = distributeBehaviorRewardMessage.getRewardValue();

        RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                .userId(distributeBehaviorRewardMessage.getUserId())
                .bizId(distributeBehaviorRewardMessage.getBizId())
                .orderId(distributeBehaviorRewardMessage.getOrderId())
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
                RechargeResult rechargeResult = skuRecharge.doSkuRecharge(rechargeContext);
                log.info("【发放奖励】增加用户抽奖次数：orderId={}, total+={}, month+={}, day+={}", rechargeResult.getOrderId(), rechargeResult.getTotalCount(), rechargeResult.getMonthCount(), rechargeResult.getDayCount());
            }

            // 3. 处理积分
            if (rewardType.equals(RewardType.POINT)) {
                Integer point = Integer.valueOf(rewardValue);
                log.info("【发放奖励】增加用户积分：point={}", point);
            }

            // 4. 改变状态
            rewardOrderEntity.setRewardState(RewardState.USED);
            int count = behaviorReward.updateRewardOrderState(rewardOrderEntity);
            if (count == 1) {
                log.info("【发放奖励】成功：userId={}, bizId={}", userId, bizId);
            } else {
                rewardOrderEntity.setRewardState(RewardState.CANCELLED);
                behaviorReward.updateRewardOrderState(rewardOrderEntity);
                log.error("【发放奖励】失败：userId={}, bizId={}", userId, bizId);
            }
        } catch (Exception e) {
            rewardOrderEntity.setRewardState(RewardState.CANCELLED);
            behaviorReward.updateRewardOrderState(rewardOrderEntity);
            log.error("【发放奖励】失败：error={}", e.getMessage());
        }
    }

}
