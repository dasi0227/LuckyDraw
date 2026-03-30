package com.dasi.domain.behavior.service.reward;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.behavior.event.DispatchBehaviorRewardEvent;
import com.dasi.domain.behavior.model.aggregate.RewardOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.model.type.RewardState;
import com.dasi.domain.behavior.model.type.TaskState;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.event.BaseEvent;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BehaviorReward implements IBehaviorReward {

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Resource
    private DispatchBehaviorRewardEvent dispatchBehaviorRewardEvent;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public BehaviorResult doBehaviorReward(BehaviorContext behaviorContext) {

        // 1. 参数校验
        String userId = behaviorContext.getUserId();
        Long activityId = behaviorContext.getActivityId();
        BehaviorType behaviorType = behaviorContext.getBehaviorType();
        String businessNo = behaviorContext.getBusinessNo();
        if (StringUtils.isBlank(businessNo)) throw new AppException("缺少参数 businessNo");
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");
        if (behaviorType == null) throw new AppException("缺少参数 behaviorType");

        // 2. 查询行为奖励
        List<BehaviorEntity> behaviorEntityList = queryBehaviorList(activityId, behaviorType);

        // 3. 保存订单
        List<RewardOrderEntity> rewardOrderEntityList = saveRewardOrder(activityId, userId, businessNo, behaviorEntityList);

        // 4. 返回订单信息
        List<String> rewardDescList = rewardOrderEntityList.stream()
                .map(RewardOrderEntity::getRewardDesc)
                .collect(Collectors.toList());

        return BehaviorResult.builder()
                .rewardDescList(rewardDescList)
                .build();
    }

    private List<BehaviorEntity> queryBehaviorList(Long activityId, BehaviorType behaviorType) {
        return behaviorRepository.queryBehaviorListByBehaviorType(activityId, behaviorType);
    }

    private List<RewardOrderEntity> saveRewardOrder(Long activityId, String userId, String businessNo, List<BehaviorEntity> behaviorEntityList) {
        List<RewardOrderEntity> rewardOrderEntityList = new ArrayList<>();
        List<RewardOrderAggregate> rewardOrderAggregateList = new ArrayList<>();

        for (BehaviorEntity behaviorEntity : behaviorEntityList) {
            // 1. 构造业务ID
            String bizId = businessNo + Delimiter.UNDERSCORE + userId + Delimiter.UNDERSCORE + behaviorEntity.getBehaviorType() + Delimiter.UNDERSCORE + behaviorEntity.getRewardType() + Delimiter.UNDERSCORE + behaviorEntity.getRewardValue();

            // 2. 构造订单实体
            RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                    .orderId(uniqueIdGenerator.nextRewardOrderId())
                    .bizId(bizId)
                    .userId(userId)
                    .activityId(behaviorEntity.getActivityId())
                    .behaviorType(behaviorEntity.getBehaviorType())
                    .rewardType(behaviorEntity.getRewardType())
                    .rewardValue(behaviorEntity.getRewardValue())
                    .rewardState(RewardState.CREATED)
                    .rewardDesc(behaviorEntity.getRewardDesc())
                    .rewardTime(LocalDateTime.now())
                    .build();
            rewardOrderEntityList.add(rewardOrderEntity);

            // 3. 构造消息实体
            DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage dispatchBehaviorRewardMessage = DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage.builder()
                    .userId(rewardOrderEntity.getUserId())
                    .bizId(rewardOrderEntity.getBizId())
                    .orderId(rewardOrderEntity.getOrderId())
                    .activityId(activityId)
                    .rewardType(behaviorEntity.getRewardType())
                    .rewardValue(behaviorEntity.getRewardValue())
                    .build();
            BaseEvent.EventMessage<DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage> eventMessage = dispatchBehaviorRewardEvent.buildEventMessage(dispatchBehaviorRewardMessage);

            // 4. 构造任务实体
            TaskEntity taskEntity = TaskEntity.builder()
                    .userId(userId)
                    .messageId(eventMessage.getMessageId())
                    .topic(dispatchBehaviorRewardEvent.getTopic())
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

        behaviorRepository.saveRewardOrder(userId, rewardOrderAggregateList);

        return rewardOrderEntityList;
    }

}
