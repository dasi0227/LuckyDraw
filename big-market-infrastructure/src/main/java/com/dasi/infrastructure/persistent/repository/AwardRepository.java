package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.IAwardDao;
import com.dasi.infrastructure.persistent.dao.IRaffleAwardDao;
import com.dasi.infrastructure.persistent.dao.IRaffleOrderDao;
import com.dasi.infrastructure.persistent.dao.ITaskDao;
import com.dasi.infrastructure.persistent.po.RaffleAward;
import com.dasi.infrastructure.persistent.po.RaffleOrder;
import com.dasi.infrastructure.persistent.po.Task;
import com.dasi.infrastructure.persistent.redis.IRedisService;
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
public class AwardRepository implements IAwardRepository {

    @Resource
    private IAwardDao awardDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IRaffleAwardDao raffleAwardDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private IRedisService redisService;

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
        RaffleAward raffleAward = new RaffleAward();
        raffleAward.setUserId(raffleAwardEntity.getUserId());
        raffleAward.setActivityId(raffleAwardEntity.getActivityId());
        raffleAward.setStrategyId(raffleAwardEntity.getStrategyId());
        raffleAward.setOrderId(raffleAwardEntity.getOrderId());
        raffleAward.setAwardId(raffleAwardEntity.getAwardId());
        raffleAward.setAwardName(raffleAwardEntity.getAwardName());
        raffleAward.setAwardTime(raffleAwardEntity.getAwardTime());
        raffleAward.setAwardState(raffleAwardEntity.getAwardState());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState());

        // 2. 入库
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    // 写入记录
                    raffleAwardDao.saveRaffleAward(raffleAward);
                    taskDao.saveTask(task);

                    // 更新订单状态
                    RaffleOrder raffleOrder = new RaffleOrder();
                    raffleOrder.setUserId(raffleAwardEntity.getUserId());
                    raffleOrder.setOrderId(raffleAwardEntity.getOrderId());
                    raffleOrder.setRaffleState(RaffleState.USED.getCode());
                    if (raffleOrderDao.updateRaffleOrderState(raffleOrder) != 1) {
                        status.setRollbackOnly();
                        log.warn("【中奖】保存中奖记录失败（订单已经使用）：orderId = {}", raffleOrder.getOrderId());
                        return null;
                    }

                    log.warn("【中奖】保存中奖记录：userId = {}, activityId = {}, awardId = {}",
                            raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId());
                    return null;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.warn("【中奖】保存中奖记录失败（唯一约束冲突）：userId = {}, activityId = {}, awardId = {}, error={}",
                            raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(),
                            e.getMessage());
                    throw new AppException("保存中奖记录失败");
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.warn("【中奖】保存中奖记录失败（未知错误）：userId = {}, activityId = {}, awardId = {}, error={}",
                            raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(),
                            e.getMessage());
                    throw new AppException("保存中奖记录失败");
                }
            });
        } finally {
            dbRouter.clear();
        }

        // 3. 发送到消息队列
        try {
            eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
            task.setTaskState(TaskState.DISTRIBUTED.getCode());
            taskDao.updateTaskState(task);
            log.warn("【中奖】发送中奖记录到消息队列：userId = {}, activityId = {}, awardId = {}, topic={}",
                    raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(),
                    task.getTopic());
        } catch (Exception e) {
            task.setTaskState(TaskState.FAILED.getCode());
            taskDao.updateTaskState(task);
            log.warn("【中奖】发送中奖记录到消息队列失败：userId = {}, activityId = {}, awardId = {}, topic={}, error={}",
                    raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(),
                    task.getTopic(), e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<TaskEntity> queryUnsolvedTask() {
        List<Task> tasks = taskDao.queryUnsolvedTask();
        return tasks.stream()
                .map(task -> TaskEntity.builder()
                    .userId(task.getUserId())
                    .messageId(task.getMessageId())
                    .topic(task.getTopic())
                    .message(task.getMessage())
                    .taskState(task.getTaskState())
                    .build())
                .collect(Collectors.toList());
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState());

        try {
            eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
            task.setTaskState(TaskState.DISTRIBUTED.getCode());
            taskDao.updateTaskState(task);
        } catch (Exception e) {
            task.setTaskState(TaskState.FAILED.getCode());
            taskDao.updateTaskState(task);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTaskState(TaskEntity taskEntity) {
        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState());

        taskDao.updateTaskState(task);
    }

}