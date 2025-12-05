package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.activity.event.DistributeActivityAwardEvent.DistributeActivityAwardMessage;
import com.dasi.domain.activity.model.entity.ActivityAwardEntity;
import com.dasi.domain.activity.model.type.AwardState;
import com.dasi.domain.activity.service.distribute.IAwardDistribute;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchActivityAward {

    @Resource
    private IAwardDistribute awardDistribute;

    @RabbitListener(queuesToDeclare = @Queue(value = "distribute_raffle_award"))
    public void dispatchActivityAward(String message) {
        EventMessage<DistributeActivityAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<DistributeActivityAwardMessage>>() {}.getType());
        DistributeActivityAwardMessage distributeActivityAwardMessage = eventMessage.getData();
        String userId = distributeActivityAwardMessage.getUserId();
        Long awardId = distributeActivityAwardMessage.getAwardId();
        String orderId = distributeActivityAwardMessage.getOrderId();

        ActivityAwardEntity activityAwardEntity = ActivityAwardEntity.builder()
                .userId(userId)
                .awardId(awardId)
                .orderId(orderId)
                .build();

        try {
            boolean success = userGetAward(userId, awardId);
            if (success) {
                activityAwardEntity.setAwardState(AwardState.COMPLETED);
                awardDistribute.updateActivityAwardState(activityAwardEntity);
                log.info("【发放】更新抽奖记录成功：orderId={}", orderId);
            } else {
                activityAwardEntity.setAwardState(AwardState.FAILED);
                awardDistribute.updateActivityAwardState(activityAwardEntity);
                log.info("【发放】更新抽奖记录失败：orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("【发放奖励】获取奖品失败：error={}", e.getMessage());
        }
    }

    // TODO：具体怎么发送奖品到用户还有待补充，这里只是简单消费一下并修改状态
    private boolean userGetAward(String userId, Long awardId) {
        log.info("【发放】获取奖品：userId={}, awardId={}", userId, awardId);
        return true;
    }

}
