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
    private IDBRouterStrategy dbRouter;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<BehaviorEntity> queryBehaviorListByBehaviorIds(List<Long> behaviorIds) {
        // 1. 先查缓存
        String cacheKey = RedisKey.BEHAVIOR_LIST + behaviorIds.toString();
        List<BehaviorEntity> behaviorEntityList = redisService.getValue(cacheKey);
        if (behaviorEntityList != null && !behaviorEntityList.isEmpty()) {
            return behaviorEntityList;
        }

        // 2. 再查数据库
        List<Behavior> behaviorList = behaviorDao.queryBehaviorListByBehaviorIds(behaviorIds);
        behaviorEntityList = behaviorList.stream()
                .map(behavior -> BehaviorEntity.builder()
                        .behaviorId(behavior.getBehaviorId())
                        .behaviorDesc(behavior.getBehaviorDesc())
                        .behaviorType(BehaviorType.valueOf(behavior.getBehaviorType()))
                        .rewardType(RewardType.valueOf(behavior.getRewardType()))
                        .rewardValue(behavior.getRewardValue())
                        .behaviorState(BehaviorState.valueOf(behavior.getBehaviorState()))
                        .build())
                .collect(Collectors.toList());


        // 3. 缓存并返回
        redisService.setValue(cacheKey, behaviorEntityList);
        return behaviorEntityList;
    }

    @Override
    public void saveRewardOrder(String userId, List<RewardOrderAggregate> rewardOrderAggregateList) {
        try {
            dbRouter.doRouter(userId);
            Boolean success = transactionTemplate.execute(status -> {
                for (RewardOrderAggregate rewardOrderAggregate : rewardOrderAggregateList) {
                    try {
                        // 写入数据库
                        RewardOrderEntity rewardOrderEntity = rewardOrderAggregate.getRewardOrderEntity();
                        RewardOrder rewardOrder = new RewardOrder();
                        rewardOrder.setOrderId(rewardOrderEntity.getOrderId());
                        rewardOrder.setBizId(rewardOrderEntity.getBizId());
                        rewardOrder.setUserId(rewardOrderEntity.getUserId());
                        rewardOrder.setBehaviorId(rewardOrderEntity.getBehaviorId());
                        rewardOrder.setRewardType(rewardOrderEntity.getRewardType().name());
                        rewardOrder.setRewardValue(rewardOrderEntity.getRewardValue());
                        rewardOrder.setRewardState(rewardOrderEntity.getRewardState().name());
                        rewardOrderDao.saveRewardOrder(rewardOrder);

                        // 写入数据库
                        TaskEntity taskEntity = rewardOrderAggregate.getTaskEntity();
                        Task task = new Task();
                        task.setUserId(taskEntity.getUserId());
                        task.setMessageId(taskEntity.getMessageId());
                        task.setTopic(taskEntity.getTopic());
                        task.setMessage(taskEntity.getMessage());
                        task.setTaskState(taskEntity.getTaskState().name());
                        taskDao.saveTask(task);

                    } catch (DuplicateKeyException e) {
                        status.setRollbackOnly();
                        log.error("【奖励】保存行为记录失败（唯一约束冲突）：userId={}, orderId={}, error={}", userId, rewardOrderAggregate.getRewardOrderEntity().getOrderId(), e.getMessage());
                        return false;
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        log.error("【奖励】保存行为记录失败（未知错误）：userId={}, orderId={}, error={}", userId, rewardOrderAggregate.getRewardOrderEntity().getOrderId(), e.getMessage());
                        return false;
                    }
                }
                return true;
            });


            boolean flag = false;
            if (Boolean.TRUE.equals(success)) {
                for (RewardOrderAggregate rewardOrderAggregate : rewardOrderAggregateList) {
                    TaskEntity taskEntity = rewardOrderAggregate.getTaskEntity();
                    Task task = new Task();
                    task.setUserId(taskEntity.getUserId());
                    task.setMessageId(taskEntity.getMessageId());
                    try {
                        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                        task.setTaskState(TaskState.DISTRIBUTED.name());
                        taskDao.updateTaskState(task);
                        log.error("【奖励】发送行为记录消息成功：messageId={}", taskEntity.getMessageId());
                    } catch (Exception e) {
                        flag = true;
                        task.setTaskState(TaskState.FAILED.name());
                        taskDao.updateTaskState(task);
                        log.error("【奖励】发送行为记录消息失败：messageId={}", taskEntity.getMessageId());
                    }
                }
            } else {
                throw new AppException("【奖励】行为触发奖励失败");
            }

            if (flag) {
                throw new AppException("【奖励】行为触发奖励失败");
            }

        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public int updateRewardOrderState(RewardOrderEntity rewardOrderEntity) {
        RewardOrder rewardOrder = new RewardOrder();
        rewardOrder.setOrderId(rewardOrderEntity.getOrderId());
        rewardOrder.setBizId(rewardOrderEntity.getBizId());
        rewardOrder.setUserId(rewardOrderEntity.getUserId());
        rewardOrder.setRewardState(rewardOrderEntity.getRewardState().name());
        return rewardOrderDao.updateRewardOrderState(rewardOrder);
    }


}
