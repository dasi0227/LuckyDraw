package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
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
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

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
        Long activityId = activityAwardEntity.getActivityId();
        Long awardId = activityAwardEntity.getAwardId();

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

            AtomicInteger atomicRows = new AtomicInteger(0);
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    int rows = activityAwardDao.saveActivityAward(activityAward);
                    if (rows == 1) {
                        atomicRows.set(1);
                        taskDao.saveTask(task);
                    }
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.info("【中奖】保存中奖记录失败：userId={}, activityId={}, awardId={}", userId, activityId, awardId);
                    throw e;
                }
            });

            if (atomicRows.get() == 0) {
                return;
            }

            try {
                task.setTaskState(TaskState.DISTRIBUTED.name());
                int rows = taskDao.updateTaskState(task);
                if (rows == 1) {
                    eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                }
                log.info("【中奖】保存中奖记录成功：userId={}, activityId={}, awardId={}", userId, activityId, awardId);
                log.info("【中奖】发送中奖消息成功：messageId={}", taskEntity.getMessageId());
            } catch (Exception e) {
                task.setTaskState(TaskState.FAILED.name());
                taskDao.updateTaskState(task);
                log.info("【中奖】发送中奖消息失败：messageId={}", taskEntity.getMessageId());
                throw e;
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void saveUserAward(ActivityAccountEntity activityAccountEntity, ActivityAwardEntity activityAwardEntity, UserAwardEntity userAwardEntity) {

        String userId = userAwardEntity.getUserId();
        String orderId = userAwardEntity.getOrderId();
        Long awardId = userAwardEntity.getAwardId();

        ActivityAward activityAward = new ActivityAward();
        activityAward.setUserId(activityAwardEntity.getUserId());
        activityAward.setActivityId(activityAwardEntity.getActivityId());
        activityAward.setOrderId(activityAwardEntity.getOrderId());
        activityAward.setAwardId(activityAwardEntity.getAwardId());
        activityAward.setAwardName(activityAwardEntity.getAwardName());
        activityAward.setAwardTime(activityAwardEntity.getAwardTime());
        activityAward.setAwardState(activityAwardEntity.getAwardState().name());

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

        ActivityAccount activityAccount;
        if (activityAccountEntity != null) {
            activityAccount = new ActivityAccount();
            activityAccount.setUserId(activityAccountEntity.getUserId());
            activityAccount.setActivityId(activityAccountEntity.getActivityId());
            activityAccount.setAccountPoint(activityAccountEntity.getAccountPoint());
        } else {
            activityAccount = null;
        }

        try {
            dbRouterStrategy.doRouter(userId);

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    activityAward.setAwardState(AwardState.COMPLETED.name());
                    int rows = activityAwardDao.updateActivityAwardState(activityAward);

                    if (rows == 1) {
                        // 充值
                        if (activityAccount != null) {
                            activityAccountDao.increaseActivityAccountPoint(activityAccount);
                        }
                        // 记录
                        userAwardDao.saveUserAward(userAward);
                    }
                    log.info("【获奖】账户获奖到个人仓库成功：userId={}, awardId={}", userId, awardId);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【获奖】账户获奖到个人仓库时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
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
