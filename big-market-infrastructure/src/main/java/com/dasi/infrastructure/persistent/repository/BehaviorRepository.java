package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.behavior.model.aggregate.RewardOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import com.dasi.domain.behavior.model.type.BehaviorState;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.model.type.RewardType;
import com.dasi.domain.behavior.model.type.TaskState;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.IBehaviorDao;
import com.dasi.infrastructure.persistent.dao.IRewardOrderDao;
import com.dasi.infrastructure.persistent.dao.ITaskDao;
import com.dasi.infrastructure.persistent.po.Behavior;
import com.dasi.infrastructure.persistent.po.RewardOrder;
import com.dasi.infrastructure.persistent.po.Task;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BehaviorRepository implements IBehaviorRepository {

    @Resource
    private IBehaviorDao behaviorDao;

    @Resource
    private IRewardOrderDao rewardOrderDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<BehaviorEntity> queryBehaviorList(Long activityId, BehaviorType behaviorType) {
        // 1. 先查缓存
        String cacheKey = RedisKey.BEHAVIOR_LIST + activityId + Delimiter.UNDERSCORE + behaviorType;
        List<BehaviorEntity> behaviorEntityList = redisService.getValue(cacheKey);
        if (behaviorEntityList != null && !behaviorEntityList.isEmpty()) {
            return behaviorEntityList;
        }

        // 2. 再查数据库
        Behavior behaviorReq = new Behavior();
        behaviorReq.setActivityId(activityId);
        behaviorReq.setBehaviorType(behaviorType.name());
        List<Behavior> behaviorList = behaviorDao.queryBehaviorList(behaviorReq);
        if (behaviorList == null || behaviorList.isEmpty()) throw new AppException("（数据库）BehaviorList 不存在：activityId=" + activityId);
        behaviorEntityList = behaviorList.stream()
                .map(behavior -> BehaviorEntity.builder()
                        .activityId(behavior.getActivityId())
                        .behaviorType(BehaviorType.valueOf(behavior.getBehaviorType()))
                        .behaviorState(BehaviorState.valueOf(behavior.getBehaviorState()))
                        .rewardType(RewardType.valueOf(behavior.getRewardType()))
                        .rewardValue(behavior.getRewardValue())
                        .rewardDesc(behavior.getRewardDesc())
                        .build())
                .collect(Collectors.toList());


        // 3. 缓存并返回
        redisService.setValue(cacheKey, behaviorEntityList);
        return behaviorEntityList;
    }

    @Override
    public Boolean querySign(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            RewardOrder rewardOrderReq = new RewardOrder();
            rewardOrderReq.setUserId(userId);
            rewardOrderReq.setActivityId(activityId);
            RewardOrder rewardOrder = rewardOrderDao.querySign(rewardOrderReq);
            return rewardOrder != null;
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveRewardOrder(String userId, List<RewardOrderAggregate> rewardOrderAggregateList) {
        try {
            dbRouterStrategy.doRouter(userId);
            Boolean success = transactionTemplate.execute(status -> {
                for (RewardOrderAggregate rewardOrderAggregate : rewardOrderAggregateList) {
                    RewardOrderEntity rewardOrderEntity = rewardOrderAggregate.getRewardOrderEntity();
                    RewardOrder rewardOrder = new RewardOrder();
                    rewardOrder.setOrderId(rewardOrderEntity.getOrderId());
                    rewardOrder.setBizId(rewardOrderEntity.getBizId());
                    rewardOrder.setUserId(rewardOrderEntity.getUserId());
                    rewardOrder.setActivityId(rewardOrderEntity.getActivityId());
                    rewardOrder.setBehaviorType(rewardOrderEntity.getBehaviorType());
                    rewardOrder.setRewardType(rewardOrderEntity.getRewardType().name());
                    rewardOrder.setRewardValue(rewardOrderEntity.getRewardValue());
                    rewardOrder.setRewardState(rewardOrderEntity.getRewardState().name());
                    rewardOrder.setRewardDesc(rewardOrderEntity.getRewardDesc());
                    rewardOrder.setRewardTime(rewardOrderEntity.getRewardTime());

                    TaskEntity taskEntity = rewardOrderAggregate.getTaskEntity();
                    Task task = new Task();
                    task.setUserId(taskEntity.getUserId());
                    task.setMessageId(taskEntity.getMessageId());
                    task.setTopic(taskEntity.getTopic());
                    task.setMessage(taskEntity.getMessage());
                    task.setTaskState(taskEntity.getTaskState().name());

                    try {
                        rewardOrderDao.saveRewardOrder(rewardOrder);
                        taskDao.saveTask(task);
                    } catch (DuplicateKeyException e) {
                        status.setRollbackOnly();
                        log.info("【返利】用户已通过当前行为获取奖励：userId={}, activityId={}, behaviorType={}", rewardOrderEntity.getUserId(), rewardOrderEntity.getActivityId(), rewardOrderEntity.getBehaviorType());
                        throw new AppException("（返利）用户已通过当前行为获取奖励：behaviorType=" + rewardOrderEntity.getBehaviorType());
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        log.error("【返利】保存返利订单时发生错误：error={}", e.getMessage());
                        return false;
                    }
                }
                return true;
            });

            if (Boolean.TRUE.equals(success)) {
                for (RewardOrderAggregate rewardOrderAggregate : rewardOrderAggregateList) {
                    log.info("【返利】保存返利订单成功：orderId={}", rewardOrderAggregate.getRewardOrderEntity().getOrderId());
                    TaskEntity taskEntity = rewardOrderAggregate.getTaskEntity();
                    Task task = new Task();
                    task.setUserId(taskEntity.getUserId());
                    task.setMessageId(taskEntity.getMessageId());
                    try {
                        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                        task.setTaskState(TaskState.DISTRIBUTED.name());
                        taskDao.updateTaskState(task);
                        log.info("【返利】发送返利消息成功：messageId={}", taskEntity.getMessageId());
                    } catch (Exception e) {
                        task.setTaskState(TaskState.FAILED.name());
                        taskDao.updateTaskState(task);
                        log.info("【返利】发送返利消息失败：messageId={}", taskEntity.getMessageId());
                    }
                }
            } else {
                throw new AppException("（返利）保存返利订单失败");
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void updateRewardOrderState(RewardOrderEntity rewardOrderEntity) {
        try {
            dbRouterStrategy.doRouter(rewardOrderEntity.getUserId());

            RewardOrder rewardOrder = new RewardOrder();
            rewardOrder.setOrderId(rewardOrderEntity.getOrderId());
            rewardOrder.setBizId(rewardOrderEntity.getBizId());
            rewardOrder.setUserId(rewardOrderEntity.getUserId());
            rewardOrder.setRewardState(rewardOrderEntity.getRewardState().name());
            rewardOrderDao.updateRewardOrderState(rewardOrder);
        } finally {
            dbRouterStrategy.clear();
        }
    }


}
