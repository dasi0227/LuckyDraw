package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.RaffleState;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.ActivityAward;
import com.dasi.infrastructure.persistent.po.RaffleOrder;
import com.dasi.infrastructure.persistent.po.Task;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Repository
public class AwardRepository implements IAwardRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IActivityAwardDao raffleAwardDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public void saveRaffleAward(RaffleAwardEntity raffleAwardEntity, TaskEntity taskEntity) {

        String userId = raffleAwardEntity.getUserId();

        // 1. 构建数据库对象
        ActivityAward activityAward = new ActivityAward();
        activityAward.setUserId(raffleAwardEntity.getUserId());
        activityAward.setActivityId(raffleAwardEntity.getActivityId());
        activityAward.setStrategyId(raffleAwardEntity.getStrategyId());
        activityAward.setOrderId(raffleAwardEntity.getOrderId());
        activityAward.setAwardId(raffleAwardEntity.getAwardId());
        activityAward.setAwardName(raffleAwardEntity.getAwardName());
        activityAward.setAwardTime(raffleAwardEntity.getAwardTime());
        activityAward.setAwardState(raffleAwardEntity.getAwardState().name());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState().name());

        RaffleOrder raffleOrder = new RaffleOrder();
        raffleOrder.setUserId(raffleAwardEntity.getUserId());
        raffleOrder.setOrderId(raffleAwardEntity.getOrderId());
        raffleOrder.setRaffleState(RaffleState.CREATED.name());

        // 2. 入库
        try {
            dbRouter.doRouter(userId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 写入记录
                    raffleAwardDao.saveRaffleAward(activityAward);
                    taskDao.saveTask(task);

                    // 更新订单状态
                    if (raffleOrderDao.updateRaffleOrderState(raffleOrder) != 1) {
                        status.setRollbackOnly();
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【中奖】保存中奖记录时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                raffleOrder.setRaffleState(RaffleState.USED.name());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                log.info("【中奖】保存中奖记录成功：userId={}, activityId={}, awardId={}", raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId());
            } else {
                raffleOrder.setRaffleState(RaffleState.CANCELLED.name());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                throw new AppException("保存中奖记录失败：orderId=" + raffleOrder.getOrderId());
            }

        } finally {
            dbRouter.clear();
        }

        // 3. 发送到消息队列
        try {
            eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
            task.setTaskState(TaskState.DISTRIBUTED.name());
            taskDao.updateTaskState(task);
            log.info("【中奖】发送中奖记录消息：messageId={}", taskEntity.getMessageId());
        } catch (Exception e) {
            task.setTaskState(TaskState.FAILED.name());
            taskDao.updateTaskState(task);
            throw new AppException("（中奖）发送中奖记录消息失败：messageId=" + taskEntity.getMessageId());
        }

    }

    @Override
    public int updateRaffleAwardState(RaffleAwardEntity raffleAwardEntity) {
        ActivityAward activityAward = new ActivityAward();
        activityAward.setUserId(raffleAwardEntity.getUserId());
        activityAward.setOrderId(raffleAwardEntity.getOrderId());
        activityAward.setAwardId(raffleAwardEntity.getAwardId());
        activityAward.setAwardState(raffleAwardEntity.getAwardState().name());
        return raffleAwardDao.updateRaffleAwardState(activityAward);
    }

}