package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.StrategyAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.RaffleState;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.util.TimeUtil;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
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
        raffleAward.setAwardState(raffleAwardEntity.getAwardState().name());

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
                    raffleAwardDao.saveRaffleAward(raffleAward);
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
    public List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId) {
        // 解析为策略 id
        Long strategyId = redisService.getValue(RedisKey.STRATEGY_ID_KEY + activityId);
        if (strategyId == null) {
            strategyId = activityDao.queryStrategyIdByActivityId(activityId);
        }

        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_AWARD_KEY + activityId;
        List<StrategyAwardEntity> strategyAwardEntityList = redisService.getValue(cacheKey);
        if (null != strategyAwardEntityList && !strategyAwardEntityList.isEmpty()) {
            return strategyAwardEntityList;
        }

        // 再查数据库
        List<StrategyAward> strategyAwardList = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        if (strategyAwardList == null || strategyAwardList.isEmpty()) throw new AppException("（查询）StrategyAwardList 不存在：activityId=" + activityId);
        strategyAwardEntityList = strategyAwardList.stream()
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
        redisService.setValue(cacheKey, strategyAwardEntityList);
        return strategyAwardEntityList;
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
            if (award == null) throw new AppException("（查询）Award 不存在：awardId=" + awardId);
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
        activityAccountDay.setDay(TimeUtil.thisDay(true));
        activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDay);
        if (activityAccountDay == null) return 0;
        return activityAccountDay.getDayAllocate() - activityAccountDay.getDaySurplus();
    }

    @Override
    public int updateRaffleAwardState(RaffleAwardEntity raffleAwardEntity) {
        RaffleAward raffleAward = new RaffleAward();
        raffleAward.setUserId(raffleAwardEntity.getUserId());
        raffleAward.setOrderId(raffleAwardEntity.getOrderId());
        raffleAward.setAwardId(raffleAwardEntity.getAwardId());
        raffleAward.setAwardState(raffleAwardEntity.getAwardState().name());
        return raffleAwardDao.updateRaffleAwardState(raffleAward);
    }

}