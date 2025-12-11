package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.entity.*;
import com.dasi.domain.award.model.type.AwardSource;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.DefaultValue;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
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
    private IActivityAwardDao activityAwardDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

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
    public List<UserAwardEntity> queryUserAwardRaffleList(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            UserAward userAwardReq = new UserAward();
            userAwardReq.setUserId(userId);
            userAwardReq.setActivityId(activityId);
            userAwardReq.setAwardSource(AwardSource.RAFFLE.name());

            List<UserAward> userAwardList = userAwardDao.queryUserAwardList(userAwardReq);

            return userAwardList.stream()
                    .map(userAward -> UserAwardEntity.builder()
                            .orderId(userAward.getOrderId())
                            .userId(userAward.getUserId())
                            .awardId(userAward.getAwardId())
                            .activityId(userAward.getActivityId())
                            .awardSource(AwardSource.valueOf(userAward.getAwardSource()))
                            .awardName(userAward.getAwardName())
                            .awardDesc(userAward.getAwardDesc())
                            .awardDeadline(userAward.getAwardDeadline())
                            .awardTime(userAward.getAwardTime())
                            .build())
                    .collect(Collectors.toList());
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccount activityAccount = new ActivityAccount();
            activityAccount.setUserId(userId);
            activityAccount.setActivityId(activityId);
            activityAccount = activityAccountDao.queryActivityAccount(activityAccount);
            if (activityAccount == null) return null;
            return ActivityAccountEntity.builder()
                    .userId(activityAccount.getUserId())
                    .activityId(activityAccount.getActivityId())
                    .accountPoint(activityAccount.getAccountPoint())
                    .totalAllocate(activityAccount.getTotalAllocate())
                    .totalSurplus(activityAccount.getTotalSurplus())
                    .dayLimit(activityAccount.getDayLimit())
                    .monthLimit(activityAccount.getMonthLimit())
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
        userAward.setActivityId(userAwardEntity.getActivityId());
        userAward.setAwardSource(userAwardEntity.getAwardSource().name());
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
    public void createActivityAccountIfAbsent(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);
            ActivityAccount activityAccountReq = new ActivityAccount();
            activityAccountReq.setUserId(userId);
            activityAccountReq.setActivityId(activityId);
            ActivityAccount activityAccount = activityAccountDao.queryActivityAccount(activityAccountReq);
            if (activityAccount == null) {
                activityAccount = new ActivityAccount();
                activityAccount.setUserId(userId);
                activityAccount.setActivityId(activityId);
                activityAccount.setAccountPoint(0);
                activityAccount.setAccountLuck(0);
                activityAccount.setTotalAllocate(0);
                activityAccount.setTotalSurplus(0);
                activityAccount.setMonthLimit(DefaultValue.MONTH_LIMIT);
                activityAccount.setDayLimit(DefaultValue.DAY_LIMIT);
                activityAccountDao.createActivityAccount(activityAccount);
            }
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void increaseActivityAccountPoint(DispatchHandleAggregate dispatchHandleAggregate) {

        String orderId = dispatchHandleAggregate.getOrderId();
        String userId = dispatchHandleAggregate.getUserId();
        Integer accountPoint = dispatchHandleAggregate.getAccountPoint();

        ActivityAccountEntity activityAccountEntity = dispatchHandleAggregate.getActivityAccountEntity();
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityAccountEntity.getUserId());
        activityAccount.setActivityId(activityAccountEntity.getActivityId());
        activityAccount.setAccountPoint(accountPoint);

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

            Integer before = activityAccountDao.queryActivityAccountLuck(activityAccount);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    activityAccountDao.increaseActivityAccountPoint(activityAccount);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【获奖】增加账户积分时发生错误：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = activityAccountDao.queryActivityAccountLuck(activityAccount);


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
