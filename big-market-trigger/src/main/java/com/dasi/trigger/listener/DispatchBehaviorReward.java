package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.activity.model.io.RechargeContext;
import com.dasi.domain.activity.model.io.RechargeResult;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.domain.behavior.event.DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage;
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

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.dispatch_behavior_reward}"))
    public void dispatchBehaviorReward(String message) {

        // 1. 解析消息
        BaseEvent.EventMessage<DispatchBehaviorRewardMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<DispatchBehaviorRewardMessage>>() {}.getType());
        DispatchBehaviorRewardMessage dispatchBehaviorRewardMessage = eventMessage.getData();
        String userId = dispatchBehaviorRewardMessage.getUserId();
        String bizId = dispatchBehaviorRewardMessage.getBizId();
        String orderId = dispatchBehaviorRewardMessage.getOrderId();
        RewardType rewardType = dispatchBehaviorRewardMessage.getRewardType();
        String rewardValue = dispatchBehaviorRewardMessage.getRewardValue();

        RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                .userId(userId)
                .bizId(bizId)
                .orderId(orderId)
                .build();

        try {
            // 2. 处理 SKU
            if (rewardType.equals(RewardType.SKU)) {
                Long skuId = Long.valueOf(rewardValue);
                log.info("=========================== 账户充值：userId={},skuId={} ===========================", userId, skuId);
                RechargeContext rechargeContext = RechargeContext.builder().userId(userId).bizId(bizId).skuId(skuId).build();
                RechargeResult rechargeResult = skuRecharge.doSkuRecharge(rechargeContext);
            }

            // 3. TODO：处理积分
            if (rewardType.equals(RewardType.POINT)) {
                Integer point = Integer.parseInt(rewardValue);
                log.info("=========================== 账户积分：userId={},point={} ===========================", userId, point);
            }

            // 4. 改变状态
            rewardOrderEntity.setRewardState(RewardState.USED);
            behaviorReward.updateRewardOrderState(rewardOrderEntity);
        } catch (Exception e) {
            rewardOrderEntity.setRewardState(RewardState.CANCELLED);
            behaviorReward.updateRewardOrderState(rewardOrderEntity);
            log.error("【发放】发放活动奖励时失败：error={}", e.getMessage());
        }
    }

}
