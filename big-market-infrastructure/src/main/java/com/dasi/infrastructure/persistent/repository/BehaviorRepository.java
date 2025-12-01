package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.behavior.model.aggregate.BehaviorOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.BehaviorOrderEntity;
import com.dasi.domain.behavior.model.entity.TaskEntity;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.IBehaviorDao;
import com.dasi.infrastructure.persistent.dao.IBehaviorOrderDao;
import com.dasi.infrastructure.persistent.dao.ITaskDao;
import com.dasi.infrastructure.persistent.po.Behavior;
import com.dasi.infrastructure.persistent.po.BehaviorOrder;
import com.dasi.infrastructure.persistent.po.Task;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.RedisKey;
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
    private IBehaviorOrderDao behaviorOrderDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private TransactionTemplate transactionTemplate;

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
                        .behaviorType(behavior.getBehaviorType())
                        .behaviorReward(behavior.getBehaviorReward())
                        .behaviorConfig(behavior.getBehaviorConfig())
                        .behaviorState(behavior.getBehaviorState())
                        .build())
                .collect(Collectors.toList());


        // 3. 缓存并返回
        redisService.setValue(cacheKey, behaviorEntityList);
        return behaviorEntityList;
    }

    @Override
    public void saveBehaviorOrder(String userId, List<BehaviorOrderAggregate> behaviorOrderAggregateList) {
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                for (BehaviorOrderAggregate behaviorOrderAggregate : behaviorOrderAggregateList) {
                    try {
                        // 写入数据库
                        BehaviorOrderEntity behaviorOrderEntity = behaviorOrderAggregate.getBehaviorOrderEntity();
                        BehaviorOrder behaviorOrder = new BehaviorOrder();
                        behaviorOrder.setOrderId(behaviorOrderEntity.getOrderId());
                        behaviorOrder.setBizId(behaviorOrderEntity.getBizId());
                        behaviorOrder.setUserId(behaviorOrderEntity.getUserId());
                        behaviorOrder.setBehaviorId(behaviorOrderEntity.getBehaviorId());
                        behaviorOrder.setBehaviorType(behaviorOrderEntity.getBehaviorType());
                        behaviorOrder.setBehaviorReward(behaviorOrderEntity.getBehaviorReward());
                        behaviorOrder.setBehaviorConfig(behaviorOrderEntity.getBehaviorConfig());
                        behaviorOrderDao.saveBehaviorOrder(behaviorOrder);

                        // 写入数据库
                        TaskEntity taskEntity = behaviorOrderAggregate.getTaskEntity();
                        Task task = new Task();
                        task.setUserId(taskEntity.getUserId());
                        task.setMessageId(taskEntity.getMessageId());
                        task.setTopic(taskEntity.getTopic());
                        task.setMessage(taskEntity.getMessage());
                        task.setTaskState(taskEntity.getTaskState());
                        taskDao.saveTask(task);

                    } catch (DuplicateKeyException e) {
                        status.setRollbackOnly();
                        log.error("【中奖】保存行为记录失败（唯一约束冲突）：userId={}, orderId={}, error={}", userId, behaviorOrderAggregate.getBehaviorOrderEntity().getOrderId(), e.getMessage());
                        return 0;
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        log.error("【中奖】保存行为记录失败（未知错误）：userId={}, orderId={}, error={}", userId, behaviorOrderAggregate.getBehaviorOrderEntity().getOrderId(), e.getMessage());
                        return 0;
                    }
                }
                return 1;
            });
        } finally {
            dbRouter.clear();
        }
    }


}
