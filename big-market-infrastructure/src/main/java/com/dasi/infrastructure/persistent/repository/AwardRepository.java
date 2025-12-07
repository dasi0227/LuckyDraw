package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.aggregate.AwardDispatchHandleAggregate;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.*;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
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
    private IUserAccountDao userAccountDao;

    @Resource
    private IUserAwardDao userAwardDao;

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
        Long activityId = activityAwardEntity.getActivityId();
        Long awardId = activityAwardEntity.getAwardId();

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
                    log.error("【中奖】保存获奖记录时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【中奖】保存获奖记录成功：userId={}, activityId={}, awardId={}", userId, activityId, awardId);
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
                    throw new AppException("发送中奖消息失败：messageId=" + taskEntity.getMessageId());
                }
            } else {
                raffleOrder.setRaffleState(RaffleState.CANCELLED.name());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                throw new AppException("使用抽奖订单失败：orderId=" + orderId);
            }

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
        if (award == null) throw new AppException("Award 不存在：awardId=" + awardId);
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

    @Override
    public void createUserAccountIfAbsent(String userId) {
        try {
            dbRouterStrategy.doRouter(userId);
            UserAccount userAccount = userAccountDao.queryUserAccountByUserId(userId);
            if (userAccount == null) {
                userAccount = new UserAccount();
                userAccount.setUserId(userId);
                userAccount.setUserState(UserState.ENABLE.name());
                userAccount.setUserPoint(0);
                userAccountDao.createUserAccount(userAccount);
            }
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void increaseUserAccountPoint(AwardDispatchHandleAggregate awardDispatchHandleAggregate) {

        Long awardId = awardDispatchHandleAggregate.getAwardId();
        String orderId = awardDispatchHandleAggregate.getOrderId();
        String userId = awardDispatchHandleAggregate.getUserId();
        Integer userPoint = awardDispatchHandleAggregate.getUserPoint();

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserPoint(userPoint);

        ActivityAward activityAward = new ActivityAward();
        activityAward.setOrderId(orderId);
        activityAward.setActivityId(awardId);
        activityAward.setUserId(userId);

        try {
            dbRouterStrategy.doRouter(userId);

            Integer prevPoint = userAccountDao.queryUserPointByUserId(userId);

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    userAccountDao.increaseUserAccountPoint(userAccount);
                    log.info("【获奖】增加用户账户积分：point={}->{}", prevPoint, prevPoint + userPoint);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【获奖】增加用户账户积分时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                activityAward.setAwardState(AwardState.COMPLETED.name());
                activityAwardDao.updateActivityAwardState(activityAward);
                log.info("【获奖】使用获奖记录成功：orderId={}", orderId);
            } else {
                activityAward.setAwardState(AwardState.FAILED.name());
                activityAwardDao.updateActivityAwardState(activityAward);
                log.info("【获奖】使用获奖记录失败：orderId={}", orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

}
