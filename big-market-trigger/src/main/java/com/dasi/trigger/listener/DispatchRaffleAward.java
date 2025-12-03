package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.award.event.DistributeRaffleAwardEvent.DistributeRaffleAwardMessage;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchRaffleAward {

    @Resource
    private IAwardDistribute awardDistribute;

    @RabbitListener(queuesToDeclare = @Queue(value = "distribute_raffle_award"))
    public void dispatchRaffleAward(String message) {
        EventMessage<DistributeRaffleAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<DistributeRaffleAwardMessage>>() {}.getType());
        DistributeRaffleAwardMessage distributeRaffleAwardMessage = eventMessage.getData();
        String userId = distributeRaffleAwardMessage.getUserId();
        Long awardId = distributeRaffleAwardMessage.getAwardId();
        String orderId = distributeRaffleAwardMessage.getOrderId();

        RaffleAwardEntity raffleAwardEntity = RaffleAwardEntity.builder()
                .userId(userId)
                .awardId(awardId)
                .orderId(orderId)
                .build();

        try {
            boolean success = userGetAward(userId, awardId);
            if (success) {
                raffleAwardEntity.setAwardState(AwardState.COMPLETED);
                awardDistribute.updateRaffleAwardState(raffleAwardEntity);
                log.info("【发放】更新抽奖记录成功：orderId={}", orderId);
            } else {
                raffleAwardEntity.setAwardState(AwardState.FAILED);
                awardDistribute.updateRaffleAwardState(raffleAwardEntity);
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
