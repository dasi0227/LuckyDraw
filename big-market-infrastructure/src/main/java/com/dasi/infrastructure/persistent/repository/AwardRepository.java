package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class AwardRepository implements IAwardRepository {

    @Resource
    private IActivityDao activityDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IAwardDao awardDao;

    @Resource
    private IRuleNodeDao ruleNodeDao;

    @Resource
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IRaffleAwardDao raffleAwardDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private IRedisService redisService;

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

        RaffleOrder raffleOrder = new RaffleOrder();
        raffleOrder.setUserId(raffleAwardEntity.getUserId());
        raffleOrder.setOrderId(raffleAwardEntity.getOrderId());
        raffleOrder.setRaffleState(RaffleState.USED.getCode());

        // 2. 入库
        try {
            dbRouter.doRouter(userId);
            Integer success = transactionTemplate.execute(status -> {
                try {
                    // 写入记录并更新订单状态
                    raffleAwardDao.saveRaffleAward(raffleAward);
                    taskDao.saveTask(task);
                    int count = raffleOrderDao.updateRaffleOrderState(raffleOrder);
                    // 更新订单状态
                    if (count != 1) {
                        status.setRollbackOnly();
                        log.error("【中奖】保存中奖记录失败（订单已经使用）：orderId={}", raffleOrder.getOrderId());
                        return 0;
                    } else {
                        log.info("【中奖】保存中奖记录成功：userId={}, activityId={}, awardId={}", raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId());
                        return 1;
                    }
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("【中奖】保存中奖记录失败（唯一约束冲突）：userId={}, activityId={}, awardId={}, error={}", raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(), e.getMessage());
                    return 0;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【中奖】保存中奖记录失败（未知错误）：userId={}, activityId={}, awardId={}, error={}", raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(), e.getMessage());
                    return 0;
                }
            });

            if (success != null && success.equals(0)) {
                raffleOrder.setRaffleState(RaffleState.CANCELLED.getCode());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                throw new AppException("保存中奖记录失败：orderId=" + raffleOrder.getOrderId());
            }

        } finally {
            dbRouter.clear();
        }

        // 3. 发送到消息队列
        try {
            eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
            task.setTaskState(TaskState.DISTRIBUTED.getCode());
            taskDao.updateTaskState(task);
            log.error("【中奖】发送中奖记录到消息队列：userId={}, activityId={}, awardId={}, topic={}", raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(), task.getTopic());
        } catch (Exception e) {
            task.setTaskState(TaskState.FAILED.getCode());
            taskDao.updateTaskState(task);
            log.error("【中奖】发送中奖记录到消息队列失败：userId={}, activityId={}, awardId={}, topic={}, error={}", raffleAwardEntity.getUserId(), raffleAwardEntity.getActivityId(), raffleAwardEntity.getAwardId(), task.getTopic(), e.getMessage());
            throw new AppException("发送中奖记录到消息队列失败");
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

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId) {
        // 解析为策略 id
        Long strategyId = redisService.getValue(RedisKey.STRATEGY_ID_KEY + activityId);
        if (strategyId == null) {
            strategyId = activityDao.queryStrategyIdByActivityId(activityId);
        }

        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);
        if (null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) {
            return strategyAwardEntities;
        }

        // 再查数据库
        List<StrategyAward> list = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        strategyAwardEntities = list.stream()
                .map(strategyAward -> StrategyAwardEntity.builder()
                        .strategyId(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .treeId(strategyAward.getTreeId())
                        .awardTitle(strategyAward.getAwardTitle())
                        .awardAllocate(strategyAward.getAwardAllocate())
                        .awardSurplus(strategyAward.getAwardSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .awardIndex(strategyAward.getAwardIndex())
                        .build())
                .collect(Collectors.toList());


        // 缓存后返回
        redisService.setValue(cacheKey, strategyAwardEntities);
        return strategyAwardEntities;
    }

    @Override
    public Map<String, AwardEntity> queryAwardMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.AWARD_MAP_KEY + activityId;
        Map<String, AwardEntity> awardEntityMap = redisService.getValue(cacheKey);
        if (awardEntityMap != null && !awardEntityMap.isEmpty()) {
            return awardEntityMap;
        }

        // 再查数据库
        awardEntityMap = new HashMap<>();
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntityList) {
            Long awardId = strategyAwardEntity.getAwardId();
            Award award = awardDao.queryAwardByAwardId(awardId);
            AwardEntity awardEntity = AwardEntity.builder()
                    .awardId(award.getAwardId())
                    .awardName(award.getAwardName())
                    .awardConfig(award.getAwardConfig())
                    .awardDesc(award.getAwardDesc())
                    .build();
            awardEntityMap.put(String.valueOf(awardId), awardEntity);
        }

        // 缓存后返回
        redisService.setValue(cacheKey, awardEntityMap);
        return awardEntityMap;
    }

    @Override
    public Map<String, Integer> queryRuleNodeLockCountMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.RULE_NODE_MAP_KEY + activityId;
        Map<String, Integer> ruleNodeLockCountMap = redisService.getValue(cacheKey);
        if (ruleNodeLockCountMap != null && !ruleNodeLockCountMap.isEmpty()) {
            return ruleNodeLockCountMap;
        }

        // 再查数据库
        ruleNodeLockCountMap = new HashMap<>();
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntityList) {
            Long awardId = strategyAwardEntity.getAwardId();
            String treeId = strategyAwardEntity.getTreeId();
            if (StringUtils.isBlank(treeId)) {
                ruleNodeLockCountMap.put(String.valueOf(awardId), 0);
            }
            Integer lockCount = ruleNodeDao.queryRuleNodeLockCountByTreeId(treeId);
            ruleNodeLockCountMap.put(String.valueOf(awardId), lockCount == null ? 0 : lockCount);
        }

        // 缓存后返回
        redisService.setValue(cacheKey, ruleNodeLockCountMap);
        return ruleNodeLockCountMap;
    }

    @Override
    public Integer queryUserLotteryCount(String userId, Long activityId) {
        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setUserId(userId);
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setDay(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDay);
        if (activityAccountDay == null) return 0;
        return activityAccountDay.getDayAllocate() - activityAccountDay.getDaySurplus();
    }

    @Override
    public int updateRaffleAwardState(RaffleAwardEntity raffleAwardEntity) {
        RaffleAward raffleAward = new RaffleAward();
        raffleAward.setOrderId(raffleAwardEntity.getOrderId());
        raffleAward.setAwardId(raffleAwardEntity.getAwardId());
        raffleAward.setAwardState(raffleAwardEntity.getAwardState());
        return raffleAwardDao.updateRaffleAwardState(raffleAward);
    }

}