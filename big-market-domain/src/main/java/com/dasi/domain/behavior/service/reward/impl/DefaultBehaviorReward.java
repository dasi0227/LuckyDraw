package com.dasi.domain.behavior.service.reward.impl;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.behavior.event.DistributeBehaviorRewardEvent;
import com.dasi.domain.behavior.event.DistributeBehaviorRewardEvent.DistributeBehaviorRewardMessage;
import com.dasi.domain.behavior.model.aggregate.RewardOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import com.dasi.domain.behavior.model.type.BehaviorState;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.model.type.RewardState;
import com.dasi.domain.behavior.model.type.TaskState;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultBehaviorReward extends AbstractBehaviorReward {

    public DefaultBehaviorReward(IBehaviorRepository behaviorRepository) {
        super(behaviorRepository);
    }

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Resource
    private DistributeBehaviorRewardEvent distributeBehaviorRewardEvent;

    @Override
    protected List<BehaviorEntity> queryBehaviorList(Long activityId, BehaviorType behaviorType) {
        return behaviorRepository.queryBehaviorList(activityId, behaviorType);
    }

    @Override
    protected List<RewardOrderEntity> saveRewardOrder(String userId, String businessNo, List<BehaviorEntity> behaviorEntityList) {
        List<RewardOrderEntity> rewardOrderEntityList = new ArrayList<>();
        List<RewardOrderAggregate> rewardOrderAggregateList = new ArrayList<>();

        for (BehaviorEntity behaviorEntity : behaviorEntityList) {
            // 0. 检查是否可用
            if (behaviorEntity.getBehaviorState().equals(BehaviorState.UNAVAILABLE)) {
                log.info("【奖励】行为触发不可用：behaviorId={}, behavior_type={}, behavior_state={}", behaviorEntity.getBehaviorId(), behaviorEntity.getBehaviorType(), behaviorEntity.getBehaviorState());
                continue;
            }

            // 1. 构造业务ID
            String bizId = businessNo + Delimiter.UNDERSCORE + userId + Delimiter.UNDERSCORE + behaviorEntity.getBehaviorId();

            // 2. 构造订单实体
            RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                        .orderId(RandomStringUtils.randomNumeric(12))
                        .bizId(bizId)
                        .userId(userId)
                        .behaviorId(behaviorEntity.getBehaviorId())
                        .rewardType(behaviorEntity.getRewardType())
                        .rewardValue(behaviorEntity.getRewardValue())
                        .rewardState(RewardState.CREATED)
                        .rewardDesc(behaviorEntity.getRewardDesc())
                        .build();
            rewardOrderEntityList.add(rewardOrderEntity);

            // 3. 构造消息实体
            DistributeBehaviorRewardMessage distributeRaffleAwardMessage = DistributeBehaviorRewardMessage.builder()
                        .userId(rewardOrderEntity.getUserId())
                        .bizId(rewardOrderEntity.getBizId())
                        .orderId(rewardOrderEntity.getOrderId())
                        .rewardType(behaviorEntity.getRewardType())
                        .rewardValue(behaviorEntity.getRewardValue())
                        .build();
            EventMessage<DistributeBehaviorRewardMessage> eventMessage = distributeBehaviorRewardEvent.buildEventMessage(distributeRaffleAwardMessage);

            // 4. 构造任务实体
            TaskEntity taskEntity = TaskEntity.builder()
                        .userId(userId)
                        .messageId(eventMessage.getMessageId())
                        .topic(distributeBehaviorRewardEvent.getTopic())
                        .message(JSON.toJSONString(eventMessage)  )
                        .taskState(TaskState.CREATED)
                        .build();

            // 5. 构造聚合实体
            RewardOrderAggregate rewardOrderAggregate = RewardOrderAggregate.builder()
                    .userId(userId)
                    .rewardOrderEntity(rewardOrderEntity)
                    .taskEntity(taskEntity)
                    .build();
            rewardOrderAggregateList.add(rewardOrderAggregate);
        }

        if (rewardOrderAggregateList.isEmpty()) {
            log.error("【奖励】当前行为无法触发任何【奖励】");
        } else {
            behaviorRepository.saveRewardOrder(userId, rewardOrderAggregateList);
        }

        return rewardOrderEntityList;
    }

    @Override
    public int updateRewardOrderState(RewardOrderEntity rewardOrderEntity) {
        return behaviorRepository.updateRewardOrderState(rewardOrderEntity);
    }

}
