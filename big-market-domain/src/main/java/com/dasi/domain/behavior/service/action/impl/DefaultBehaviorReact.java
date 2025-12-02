package com.dasi.domain.behavior.service.action.impl;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.behavior.event.DistributeBehaviorRewardEvent;
import com.dasi.domain.behavior.event.DistributeBehaviorRewardEvent.DistributeBehaviorRewardMessage;
import com.dasi.domain.behavior.model.aggregate.BehaviorOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.BehaviorOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import com.dasi.domain.behavior.model.type.TaskState;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.event.BaseEvent.EventMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultBehaviorReact extends AbstractBehaviorReact {

    public DefaultBehaviorReact(IBehaviorRepository behaviorRepository) {
        super(behaviorRepository);
    }

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Resource
    private DistributeBehaviorRewardEvent distributeBehaviorRewardEvent;

    @Override
    protected List<BehaviorOrderEntity> saveBehaviorOrder(String userId, String businessNo, List<Long> behaviorIds) {
        // 1. 查询配置
        List<BehaviorEntity> behaviorEntityList = behaviorRepository.queryBehaviorListByBehaviorIds(behaviorIds);
        if (behaviorEntityList == null || behaviorEntityList.isEmpty()) {
            return null;
        }

        List<BehaviorOrderEntity> behaviorOrderEntityList = new ArrayList<>();
        List<BehaviorOrderAggregate> behaviorOrderAggregateList = new ArrayList<>();

        // 2. 构建聚合对象
        for (BehaviorEntity behaviorEntity : behaviorEntityList) {
            // 1. 构造业务ID
            String bizId = businessNo + Delimiter.UNDERSCORE + userId + Delimiter.UNDERSCORE + behaviorEntity.getBehaviorId();

            // 2. 构造订单实体
            BehaviorOrderEntity behaviorOrderEntity = BehaviorOrderEntity.builder()
                        .orderId(RandomStringUtils.randomNumeric(12))
                        .bizId(bizId)
                        .userId(userId)
                        .behaviorId(behaviorEntity.getBehaviorId())
                        .behaviorType(behaviorEntity.getBehaviorType())
                        .behaviorReward(behaviorEntity.getBehaviorReward())
                        .behaviorConfig(behaviorEntity.getBehaviorConfig())
                        .build();
            behaviorOrderEntityList.add(behaviorOrderEntity);

            // 3. 构造消息实体
            DistributeBehaviorRewardMessage distributeRaffleAwardMessage = DistributeBehaviorRewardMessage.builder()
                        .userId(behaviorOrderEntity.getUserId())
                        .bizId(behaviorOrderEntity.getBizId())
                        .behavior_reward(behaviorEntity.getBehaviorReward())
                        .behavior_config(behaviorEntity.getBehaviorConfig())
                        .build();
            EventMessage<DistributeBehaviorRewardMessage> eventMessage = distributeBehaviorRewardEvent.buildEventMessage(distributeRaffleAwardMessage);

            // 4. 构造任务实体
            TaskEntity taskEntity = TaskEntity.builder()
                        .userId(userId)
                        .messageId(eventMessage.getMessageId())
                        .topic(distributeBehaviorRewardEvent.getTopic())
                        .message(JSON.toJSONString(eventMessage)  )
                        .taskState(TaskState.CREATED.getCode())
                        .build();

            // 5. 构造聚合实体
            BehaviorOrderAggregate behaviorOrderAggregate = BehaviorOrderAggregate.builder()
                    .userId(userId)
                    .behaviorOrderEntity(behaviorOrderEntity)
                    .taskEntity(taskEntity)
                    .build();
            behaviorOrderAggregateList.add(behaviorOrderAggregate);
        }

        // 3. 保存订单
        behaviorRepository.saveBehaviorOrder(userId, behaviorOrderAggregateList);

        return behaviorOrderEntityList;
    }

}
