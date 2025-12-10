package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.entity.*;
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
    public UserAccountEntity queryUserAccountByUserId(String userId) {
        try {
            dbRouterStrategy.doRouter(userId);

            UserAccount userAccount = userAccountDao.queryUserAccountByUserId(userId);
            if (userAccount == null) throw new AppException("UserAccount 不存在：userId=" + userId);
            return UserAccountEntity.builder()
                    .userId(userAccount.getUserId())
                    .userState(UserState.valueOf(userAccount.getUserState()))
                    .userPoint(userAccount.getUserPoint())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public ActivityAwardEntity queryActivityAwardByOrderId(String userId, String orderId) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAward activityAward = activityAwardDao.queryActivityAwardByOrderId(orderId);
            if (activityAward == null) throw new AppException("ActivityAward 不存在：orderId=" + orderId);
            return ActivityAwardEntity.builder()
                    .userId(activityAward.getUserId())
                    .activityId(activityAward.getActivityId())
                    .orderId(activityAward.getOrderId())
                    .awardId(activityAward.getAwardId())
                    .awardName(activityAward.getAwardName())
                    .awardTime(activityAward.getAwardTime())
                    .awardState(AwardState.valueOf(activityAward.getAwardState()))
                    .build();
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
                .awardValue(award.getAwardValue())
                .awardDesc(award.getAwardDesc())
                .build();

        // 缓存后返回
        redisService.setValue(cacheKey, awardEntity);
        return awardEntity;
    }

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

        try {
            dbRouterStrategy.doRouter(userId);

            // 2. 入库
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 写入记录
                    activityAwardDao.saveActivityAward(activityAward);
                    taskDao.saveTask(task);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【中奖】保存中奖记录时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【中奖】保存中奖记录成功：userId={}, activityId={}, awardId={}", userId, activityId, awardId);
                // 3. 发送到消息队列
                try {
                    eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                    task.setTaskState(TaskState.DISTRIBUTED.name());
                    taskDao.updateTaskState(task);
                    log.info("【中奖】发送中奖消息成功：messageId={}", taskEntity.getMessageId());
                } catch (Exception e) {
                    task.setTaskState(TaskState.FAILED.name());
                    taskDao.updateTaskState(task);
                    throw new AppException("发送中奖消息失败：messageId=" + taskEntity.getMessageId());
                }
            } else {
                throw new AppException("保存中奖记录失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void saveUserAward(DispatchHandleAggregate dispatchHandleAggregate) {

        String orderId = dispatchHandleAggregate.getOrderId();
        String userId = dispatchHandleAggregate.getUserId();
        Long awardId = dispatchHandleAggregate.getAwardId();

        ActivityAwardEntity activityAwardEntity = dispatchHandleAggregate.getActivityAwardEntity();
        ActivityAward activityAward = new ActivityAward();
        activityAward.setUserId(activityAwardEntity.getUserId());
        activityAward.setActivityId(activityAwardEntity.getActivityId());
        activityAward.setOrderId(activityAwardEntity.getOrderId());
        activityAward.setAwardId(activityAwardEntity.getAwardId());
        activityAward.setAwardName(activityAwardEntity.getAwardName());
        activityAward.setAwardTime(activityAwardEntity.getAwardTime());
        activityAward.setAwardState(activityAwardEntity.getAwardState().name());

        UserAwardEntity userAwardEntity = dispatchHandleAggregate.getUserAwardEntity();
        UserAward userAward = new UserAward();
        userAward.setOrderId(userAwardEntity.getOrderId());
        userAward.setUserId(userAwardEntity.getUserId());
        userAward.setAwardId(userAwardEntity.getAwardId());
        userAward.setAwardType(userAwardEntity.getAwardType().name());
        userAward.setAwardName(userAwardEntity.getAwardName());
        userAward.setAwardDesc(userAwardEntity.getAwardDesc());
        userAward.setAwardDeadline(userAwardEntity.getAwardDeadline());
        userAward.setAwardTime(userAwardEntity.getAwardTime());

        try {
            dbRouterStrategy.doRouter(userId);

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    userAwardDao.saveUserAward(userAward);
                    log.info("【获奖】账户获奖到个人仓库成功：userId={}, awardId={}", userId, awardId);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【获奖】账户获奖到个人仓库时发生错误：error={}", e.getMessage());
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
    public void increaseUserAccountPoint(DispatchHandleAggregate dispatchHandleAggregate) {

        String orderId = dispatchHandleAggregate.getOrderId();
        String userId = dispatchHandleAggregate.getUserId();
        Integer userPoint = dispatchHandleAggregate.getUserPoint();

        UserAccountEntity userAccountEntity = dispatchHandleAggregate.getUserAccountEntity();
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userAccountEntity.getUserId());
        userAccount.setUserState(userAccountEntity.getUserState().name());
        userAccount.setUserPoint(userPoint);

        ActivityAwardEntity activityAwardEntity = dispatchHandleAggregate.getActivityAwardEntity();
        ActivityAward activityAward = new ActivityAward();
        activityAward.setUserId(activityAwardEntity.getUserId());
        activityAward.setActivityId(activityAwardEntity.getActivityId());
        activityAward.setOrderId(activityAwardEntity.getOrderId());
        activityAward.setAwardId(activityAwardEntity.getAwardId());
        activityAward.setAwardName(activityAwardEntity.getAwardName());
        activityAward.setAwardTime(activityAwardEntity.getAwardTime());
        activityAward.setAwardState(activityAwardEntity.getAwardState().name());

        try {
            dbRouterStrategy.doRouter(userId);

            Integer before = userAccountDao.queryUserPointByUserId(userId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    userAccountDao.increaseUserAccountPoint(userAccount);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【获奖】增加账户积分时发生错误：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = userAccountDao.queryUserPointByUserId(userId);


            if (Boolean.TRUE.equals(success)) {
                activityAward.setAwardState(AwardState.COMPLETED.name());
                activityAwardDao.updateActivityAwardState(activityAward);
                log.info("【获奖】增加账户积分成功：userId={}, point={}->{}", userId, before, after);
                log.info("【获奖】账户获奖成功：orderId={}", orderId);
            } else {
                activityAward.setAwardState(AwardState.FAILED.name());
                activityAwardDao.updateActivityAwardState(activityAward);
                throw new AppException("账户获奖失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

}
