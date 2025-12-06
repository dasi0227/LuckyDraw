package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.type.RaffleState;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.IActivityAwardDao;
import com.dasi.infrastructure.persistent.dao.IAwardDao;
import com.dasi.infrastructure.persistent.dao.IRaffleOrderDao;
import com.dasi.infrastructure.persistent.dao.ITaskDao;
import com.dasi.infrastructure.persistent.po.ActivityAward;
import com.dasi.infrastructure.persistent.po.Award;
import com.dasi.infrastructure.persistent.po.RaffleOrder;
import com.dasi.infrastructure.persistent.po.Task;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Repository
public class AwardRepository implements IAwardRepository {

    @Resource
    private IAwardDao awardDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IActivityAwardDao activityAwardDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity) {

        String userId = activityAwardEntity.getUserId();
        String orderId = activityAwardEntity.getOrderId();

        // 1. 构建数据库对象
        ActivityAward activityAward = new ActivityAward();
        activityAward.setUserId(activityAwardEntity.getUserId());
        activityAward.setActivityId(activityAwardEntity.getActivityId());
        activityAward.setOrderId(activityAwardEntity.getOrderId());
        activityAward.setAwardId(activityAwardEntity.getAwardId());
        activityAward.setAwardName(activityAwardEntity.getAwardName());
        activityAward.setAwardTime(activityAwardEntity.getAwardTime());
        activityAward.setAwardState(activityAwardEntity.getAwardState().name());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState().name());

        RaffleOrder raffleOrder = new RaffleOrder();
        raffleOrder.setUserId(activityAwardEntity.getUserId());
        raffleOrder.setOrderId(activityAwardEntity.getOrderId());
        raffleOrder.setRaffleState(RaffleState.CREATED.name());

        try {
            dbRouterStrategy.doRouter(userId);

            // 2. 入库
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 写入记录
                    activityAwardDao.saveActivityAward(activityAward);
                    taskDao.saveTask(task);

                    // 更新订单状态
                    raffleOrderDao.updateRaffleOrderState(raffleOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【中奖】保存中奖记录时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                // 3. 发送到消息队列
                try {
                    raffleOrder.setRaffleState(RaffleState.USED.name());
                    raffleOrderDao.updateRaffleOrderState(raffleOrder);
                    log.info("【中奖】使用抽奖订单成功：orderId={}", orderId);

                    eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                    task.setTaskState(TaskState.DISTRIBUTED.name());
                    taskDao.updateTaskState(task);
                    log.info("【中奖】发送中奖消息成功：messageId={}", taskEntity.getMessageId());
                } catch (Exception e) {
                    task.setTaskState(TaskState.FAILED.name());
                    taskDao.updateTaskState(task);
                    raffleOrder.setRaffleState(RaffleState.CANCELLED.name());
                    raffleOrderDao.updateRaffleOrderState(raffleOrder);
                    throw new AppException("（中奖）发送中奖消息失败：messageId=" + taskEntity.getMessageId());
                }
            } else {
                raffleOrder.setRaffleState(RaffleState.CANCELLED.name());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                throw new AppException("（中奖）使用抽奖订单失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void updateActivityAwardState(ActivityAwardEntity activityAwardEntity) {
        try {
            dbRouterStrategy.doRouter(activityAwardEntity.getUserId());

            ActivityAward activityAward = new ActivityAward();
            activityAward.setUserId(activityAwardEntity.getUserId());
            activityAward.setOrderId(activityAwardEntity.getOrderId());
            activityAward.setAwardId(activityAwardEntity.getAwardId());
            activityAward.setAwardState(activityAwardEntity.getAwardState().name());
            activityAwardDao.updateActivityAwardState(activityAward);
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public AwardEntity queryAwardByAwardId(Long awardId) {
        // 先查缓存
        String cacheKey = RedisKey.AWARD_KEY + awardId;
        AwardEntity awardEntity = redisService.getValue(cacheKey);
        if (awardEntity != null) {
            return awardEntity;
        }

        // 再查数据库
        Award award = awardDao.queryAwardByAwardId(awardId);
        if (award == null) throw new AppException("（数据库）Award 不存在：awardId=" + awardId);
        awardEntity = AwardEntity.builder()
                .awardId(award.getAwardId())
                .awardType(AwardType.valueOf(award.getAwardType()))
                .awardName(award.getAwardName())
                .awardConfig(award.getAwardConfig())
                .awardDesc(award.getAwardDesc())
                .build();

        // 缓存后返回
        redisService.setValue(cacheKey, awardEntity);
        return awardEntity;
    }
}
