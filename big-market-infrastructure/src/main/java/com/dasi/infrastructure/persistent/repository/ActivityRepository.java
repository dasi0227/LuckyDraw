package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.RechargeSkuStockEmptyEvent;
import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.queue.ActivitySkuStock;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.model.type.RechargeState;
import com.dasi.domain.activity.model.type.TaskState;
import com.dasi.domain.activity.model.vo.AccountSurplusSnapshot;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IRedisService redisService;

    @Resource
    private IActivityDao activityDao;

    @Resource
    private IActivitySkuDao rechargeSkuDao;

    @Resource
    private IRechargeOrderDao rechargeOrderDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IActivityAwardDao activityAwardDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

    @Resource
    private IActivityAccountMonthDao activityAccountMonthDao;

    @Resource
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private RechargeSkuStockEmptyEvent rechargeSkuStockEmptyEvent;

    @Override
    public ActivitySkuEntity queryRechargeSkuBySkuId(Long skuId) {
        ActivitySku activitySku = rechargeSkuDao.queryRechargeSkuBySkuId(skuId);
        if (activitySku == null) throw new AppException("（查询）ActivitySku 不存在：skuId=" + skuId);
        return ActivitySkuEntity.builder()
                .skuId(activitySku.getSkuId())
                .activityId(activitySku.getActivityId())
                .count(activitySku.getCount())
                .stockAllocate(activitySku.getStockAllocate())
                .stockSurplus(activitySku.getStockSurplus())
                .build();
    }

    @Override
    public List<ActivitySkuEntity> queryRechargeSkuByActivityId(Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_RECHARGE_SKU_KEY + activityId;
        List<ActivitySkuEntity> activitySkuEntityList = redisService.getValue(cacheKey);
        if (activitySkuEntityList != null && !activitySkuEntityList.isEmpty()) {
            return activitySkuEntityList;
        }

        // 再查数据库
        List<ActivitySku> activitySkuList = rechargeSkuDao.queryRechargeSkuByActivityId(activityId);
        if (activitySkuList == null || activitySkuList.isEmpty()) throw new AppException("（查询）RechargeSkuList 不存在：activityId=" + activityId);
        activitySkuEntityList = activitySkuList.stream()
                .map(activitySku -> ActivitySkuEntity.builder()
                        .skuId(activitySku.getSkuId())
                        .activityId(activitySku.getActivityId())
                        .count(activitySku.getCount())
                        .stockAllocate(activitySku.getStockAllocate())
                        .stockSurplus(activitySku.getStockSurplus())
                        .build())
                .collect(Collectors.toList());

        // 缓存并返回
        redisService.setValue(cacheKey, activitySkuEntityList);
        return activitySkuEntityList;

    }

    @Override
    public ActivityEntity queryActivityByActivityId(Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_KEY + activityId;
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if (activityEntity != null) {
            return activityEntity;
        }

        // 再查数据库
        Activity activity = activityDao.queryActivityByActivityId(activityId);
        if (activity == null) throw new AppException("（查询）Activity 不存在：activity=Id" + activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .activityDesc(activity.getActivityDesc())
                .activityBeginTime(activity.getActivityBeginTime())
                .activityEndTime(activity.getActivityEndTime())
                .strategyId(activity.getStrategyId())
                .activityState(ActivityState.valueOf(activity.getActivityState()))
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccount activityAccount = new ActivityAccount();
            activityAccount.setUserId(userId);
            activityAccount.setActivityId(activityId);
            activityAccount = activityAccountDao.queryActivityAccount(activityAccount);
            if (activityAccount == null) return null; // 交给上层处理 null
            return ActivityAccountEntity.builder()
                    .userId(activityAccount.getUserId())
                    .activityId(activityAccount.getActivityId())
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
    public ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
            activityAccountMonth.setUserId(userId);
            activityAccountMonth.setActivityId(activityId);
            activityAccountMonth.setMonthKey(month);
            activityAccountMonth = activityAccountMonthDao.queryActivityAccountMonth(activityAccountMonth);
            if (activityAccountMonth == null) return null; // 交给上层处理 null
            return ActivityAccountMonthEntity.builder()
                    .activityId(activityAccountMonth.getActivityId())
                    .userId(activityAccountMonth.getUserId())
                    .monthKey(activityAccountMonth.getMonthKey())
                    .monthAllocate(activityAccountMonth.getMonthAllocate())
                    .monthSurplus(activityAccountMonth.getMonthSurplus())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccountDay activityAccountDay = new ActivityAccountDay();
            activityAccountDay.setUserId(userId);
            activityAccountDay.setActivityId(activityId);
            activityAccountDay.setDayKey(day);
            activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDay);
            if (activityAccountDay == null) return null; // 交给上层处理 null
            return ActivityAccountDayEntity.builder()
                    .activityId(activityAccountDay.getActivityId())
                    .userId(activityAccountDay.getUserId())
                    .dayKey(activityAccountDay.getDayKey())
                    .dayAllocate(activityAccountDay.getDayAllocate())
                    .daySurplus(activityAccountDay.getDaySurplus())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public RaffleOrderEntity queryUnusedRaffleOrder(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            RaffleOrder raffleOrderReq = new RaffleOrder();
            raffleOrderReq.setUserId(userId);
            raffleOrderReq.setActivityId(activityId);
            RaffleOrder raffleOrder = raffleOrderDao.queryUnusedRaffleOrder(raffleOrderReq);
            if (raffleOrder == null) return null;
            return RaffleOrderEntity.builder()
                    .orderId(raffleOrder.getOrderId())
                    .userId(raffleOrder.getUserId())
                    .activityId(raffleOrder.getActivityId())
                    .strategyId(raffleOrder.getStrategyId())
                    .raffleState(RaffleState.valueOf(raffleOrder.getRaffleState()))
                    .raffleTime(raffleOrder.getRaffleTime())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void cacheRechargeSkuStockSurplus(Long skuId, Integer stockSurplus) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_SURPLUS_KEY + skuId;
        redisService.setAtomicLong(cacheKey, Long.valueOf(stockSurplus));
    }

    // -1 表示为空，-2 表示错误
    @Override
    public Long subtractRechargeSkuStockSurplus(Long skuId, LocalDateTime activityEndTime) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_SURPLUS_KEY + skuId;
        if (!redisService.isExists(cacheKey)) return -2L;

        long surplus = redisService.decr(cacheKey);
        if (surplus < -1L) {
            redisService.setAtomicLong(cacheKey, 0L);
            return -2L;
        }
        if (surplus == -1L) {
            redisService.setAtomicLong(cacheKey, 0L);
            return -1L;
        }
        if (surplus == 0L) {
            eventPublisher.publish(rechargeSkuStockEmptyEvent.getTopic(), rechargeSkuStockEmptyEvent.buildEventMessage(skuId));
            return surplus;
        }

        String lockKey = cacheKey + Delimiter.UNDERSCORE + surplus;
        Duration expire = Duration.ofMillis(
                activityEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        - System.currentTimeMillis()
                        + TimeUnit.DAYS.toMillis(1));
        if (!redisService.setNx(lockKey, expire)) {
            return -2L;
        }
        return surplus;
    }

    @Override
    public void sendActivitySkuStockConsumeToMQ(ActivitySkuStock activitySkuStock) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<ActivitySkuStock> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(activitySkuStock, 3, TimeUnit.SECONDS);
    }

    @Override
    public ActivitySkuStock getQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        blockingQueue.clear();
    }

    @Override
    public void updateRechargeSkuStock(Long skuId) {
        rechargeSkuDao.updateRechargeSkuStock(skuId);
    }

    @Override
    public void clearRechargeSkuStock(Long skuId) {
        rechargeSkuDao.clearRechargeSkuStock(skuId);
    }


    // TODO：月限制和日限制的逻辑还没实现
    @Override
    public void createActivityAccountIfAbsent(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            transactionTemplate.execute(status -> {
                try {
                    String dayKey = TimeUtil.thisDay(true);
                    String monthKey = TimeUtil.thisMonth(true);

                    /* ========== 1. 查询/创建 总账户 ========== */
                    ActivityAccount accountReq = new ActivityAccount();
                    accountReq.setUserId(userId);
                    accountReq.setActivityId(activityId);
                    ActivityAccount account = activityAccountDao.queryActivityAccount(accountReq);

                    if (account == null) {
                        account = new ActivityAccount();
                        account.setUserId(userId);
                        account.setActivityId(activityId);
                        account.setTotalAllocate(0);
                        account.setTotalSurplus(0);
                        account.setMonthLimit(-1);
                        account.setDayLimit(-1);
                        activityAccountDao.createActivityAccount(account);
                        log.info("【创建】总账户：userId={}, activityId={}, totalAllocate={}, totalSurplus={}, monthLimit={}, dayLimit={}",
                                userId, activityId, 0, 0, -1, -1);
                    }

                    // TODO：当前可用次数，用来初始化日/月账户（这里先用 totalSurplus，后面再按 limit 规则改）
                    Integer count = account.getTotalSurplus();

                    /* ========== 2. 查询/创建 日账户 ========== */
                    ActivityAccountDay dayReq = new ActivityAccountDay();
                    dayReq.setUserId(userId);
                    dayReq.setActivityId(activityId);
                    dayReq.setDayKey(dayKey);
                    ActivityAccountDay dayAccount = activityAccountDayDao.queryActivityAccountDay(dayReq);

                    if (dayAccount == null) {
                        dayAccount = new ActivityAccountDay();
                        dayAccount.setActivityId(activityId);
                        dayAccount.setUserId(userId);
                        dayAccount.setDayKey(dayKey);
                        dayAccount.setDayAllocate(count);
                        dayAccount.setDaySurplus(count);
                        activityAccountDayDao.createActivityAccountDay(dayAccount);
                        log.info("【创建】日账户：userId={}, activityId={}, day={}, dayAllocate=daySurplus={}",
                                userId, activityId, dayKey, count);
                    }

                    /* ========== 3. 查询/创建 月账户 ========== */
                    ActivityAccountMonth monthReq = new ActivityAccountMonth();
                    monthReq.setUserId(userId);
                    monthReq.setActivityId(activityId);
                    monthReq.setMonthKey(monthKey);
                    ActivityAccountMonth monthAccount = activityAccountMonthDao.queryActivityAccountMonth(monthReq);

                    if (monthAccount == null) {
                        monthAccount = new ActivityAccountMonth();
                        monthAccount.setActivityId(activityId);
                        monthAccount.setUserId(userId);
                        monthAccount.setMonthKey(monthKey);
                        monthAccount.setMonthAllocate(count);
                        monthAccount.setMonthSurplus(count);
                        activityAccountMonthDao.createActivityAccountMonth(monthAccount);
                        log.info("【创建】月账户：userId={}, activityId={}, month={}, monthAllocate=monthSurplus={}",
                                userId, activityId, monthKey, count);
                    }

                    return null;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【创建】创建账户时发生错误：error={}", e.getMessage());
                    throw new AppException("（创建）创建账户时发生错误：userId=" + userId + ", activityId=" + activityId);
                }
            });
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveRechargeOrder(RechargeOrderEntity rechargeOrderEntity) {

        // 订单对象
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setOrderId(rechargeOrderEntity.getOrderId());
        rechargeOrder.setBizId(rechargeOrderEntity.getBizId());
        rechargeOrder.setActivityId(rechargeOrderEntity.getActivityId());
        rechargeOrder.setUserId(rechargeOrderEntity.getUserId());
        rechargeOrder.setSkuId(rechargeOrderEntity.getSkuId());
        rechargeOrder.setCount(rechargeOrderEntity.getCount());
        rechargeOrder.setRechargeState(rechargeOrderEntity.getRechargeState().name());
        rechargeOrder.setRechargeTime(rechargeOrderEntity.getRechargeTime());

        // 基础信息
        String orderId = rechargeOrderEntity.getOrderId();
        String userId = rechargeOrderEntity.getUserId();
        Long activityId = rechargeOrderEntity.getActivityId();
        Integer count = rechargeOrderEntity.getCount();
        String monthKey = TimeUtil.thisMonth(true);
        String dayKey = TimeUtil.thisDay(true);

        // 创建用于充值的账户
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setActivityId(activityId);
        activityAccount.setUserId(userId);
        activityAccount.setTotalAllocate(count);
        activityAccount.setTotalSurplus(count);

        ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
        activityAccountMonth.setActivityId(activityId);
        activityAccountMonth.setUserId(userId);
        activityAccountMonth.setMonthKey(monthKey);
        activityAccountMonth.setMonthAllocate(count);
        activityAccountMonth.setMonthSurplus(count);

        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setUserId(userId);
        activityAccountDay.setDayKey(dayKey);
        activityAccountDay.setDayAllocate(count);
        activityAccountDay.setDaySurplus(count);

        try {
            dbRouterStrategy.doRouter(userId);

            AccountSurplusSnapshot before = getAccountSurplusSnapshot(userId, activityId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 执行充值
                    activityAccountDao.rechargeActivityAccount(activityAccount);
                    activityAccountMonthDao.rechargeActivityAccountMonth(activityAccountMonth);
                    activityAccountDayDao.rechargeActivityAccountDay(activityAccountDay);
                    // 写入订单
                    rechargeOrderDao.saveRechargeOrder(rechargeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                AccountSurplusSnapshot after = getAccountSurplusSnapshot(userId, activityId);

                rechargeOrder.setRechargeState(RechargeState.USED.name());
                rechargeOrderDao.updateRechargeState(rechargeOrder);

                log.info("【充值】userId={}, activityId={}, total:{}->{}, month({}):{}->{}, day({}):{}->{}",
                        userId, activityId,
                        before.getTotalSurplus(),  after.getTotalSurplus(),
                        monthKey,   before.getMonthSurplus(),  after.getMonthSurplus(),
                        dayKey,     before.getDaySurplus(),    after.getDaySurplus()
                );
                log.info("【充值】保存充值订单成功：orderId={}", orderId);
            } else {
                rechargeOrder.setRechargeState(RechargeState.CANCELLED.name());
                rechargeOrderDao.updateRechargeState(rechargeOrder);
                throw new AppException("（充值）保存充值订单失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveRaffleOrder(RaffleOrderEntity raffleOrderEntity) {

        String orderId = raffleOrderEntity.getOrderId();
        String userId = raffleOrderEntity.getUserId();
        Long activityId = raffleOrderEntity.getActivityId();
        String monthKey = TimeUtil.thisMonth(true);
        String dayKey = TimeUtil.thisDay(true);

        try {
            dbRouterStrategy.doRouter(userId);

            AccountSurplusSnapshot before = getAccountSurplusSnapshot(userId, activityId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    int count;

                    /* ===================
                    // 1. 更新账户总余额
                    ====================*/
                    ActivityAccount activityAccount = new ActivityAccount();
                    activityAccount.setUserId(userId);
                    activityAccount.setActivityId(activityId);
                    count = activityAccountDao.subtractActivityAccount(activityAccount);
                    if (count == 0) {
                        status.setRollbackOnly();
                        log.info("【抽奖】账户不存在：userId={}, activityId={}", userId, activityId);
                        return false;
                    }

                    /* ===================
                    // 2. 保存/更新账户月余额
                    ====================*/
                    ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
                    activityAccountMonth.setUserId(userId);
                    activityAccountMonth.setActivityId(activityId);
                    activityAccountMonth.setMonthKey(monthKey);
                    count = activityAccountMonthDao.subtractActivityAccountMonth(activityAccountMonth);
                    if (count == 0) {
                        status.setRollbackOnly();
                        log.info("【抽奖】月账户不存在：userId={}, activityId={}, month={}", userId, activityId, monthKey);
                        return false;
                    }

                    /* ===================
                    // 3. 保存/更新账户日余额
                    ====================*/
                    ActivityAccountDay activityAccountDay = new ActivityAccountDay();
                    activityAccountDay.setUserId(userId);
                    activityAccountDay.setActivityId(activityId);
                    activityAccountDay.setDayKey(dayKey);
                    count = activityAccountDayDao.subtractActivityAccountDay(activityAccountDay);
                    if (count == 0) {
                        status.setRollbackOnly();
                        log.info("【抽奖】日账户不存在：userId={}, activityId={}, day={}", userId, activityId, dayKey);
                        return false;
                    }

                    /* ===================
                    // 4. 保存抽奖订单
                    ====================*/
                    RaffleOrder raffleOrder = new RaffleOrder();
                    raffleOrder.setOrderId(raffleOrderEntity.getOrderId());
                    raffleOrder.setUserId(raffleOrderEntity.getUserId());
                    raffleOrder.setActivityId(raffleOrderEntity.getActivityId());
                    raffleOrder.setStrategyId(raffleOrderEntity.getStrategyId());
                    raffleOrder.setRaffleState(raffleOrderEntity.getRaffleState().name());
                    raffleOrder.setRaffleTime(raffleOrderEntity.getRaffleTime());
                    raffleOrderDao.saveRaffleOrder(raffleOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【抽奖】保存抽奖订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });


            // TODO：是否需要更新抽奖订单状态
            if (Boolean.TRUE.equals(success)) {
                AccountSurplusSnapshot after = getAccountSurplusSnapshot(userId, activityId);
                log.info("【抽奖】userId={}, activityId={}, total:{}->{}, month({}):{}->{}, day:({}){}->{}",
                        userId, activityId,
                        before.getTotalSurplus(),  after.getTotalSurplus(),
                        monthKey,   before.getMonthSurplus(),  after.getMonthSurplus(),
                        dayKey,     before.getDaySurplus(),    after.getDaySurplus()
                );
                log.info("【抽奖】保存抽奖订单成功：orderId={}", orderId);
            } else {
                throw new AppException("（抽奖）保存抽奖订单失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity) {

        String userId = activityAwardEntity.getUserId();

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
                log.info("【中奖】保存中奖记录成功：userId={}, activityId={}, awardId={}", activityAwardEntity.getUserId(), activityAwardEntity.getActivityId(), activityAwardEntity.getAwardId());

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

            } else {
                raffleOrder.setRaffleState(RaffleState.CANCELLED.name());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                throw new AppException("保存中奖记录失败：orderId=" + raffleOrder.getOrderId());
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

    private AccountSurplusSnapshot getAccountSurplusSnapshot(String userId, Long activityId) {
        // ===== 1. 总账户 =====
        ActivityAccount accountReq = new ActivityAccount();
        accountReq.setUserId(userId);
        accountReq.setActivityId(activityId);
        ActivityAccount account = activityAccountDao.queryActivityAccount(accountReq);

        // ===== 2. 月账户 =====
        String monthKey = TimeUtil.thisMonth(true);
        ActivityAccountMonth monthReq = new ActivityAccountMonth();
        monthReq.setUserId(userId);
        monthReq.setActivityId(activityId);
        monthReq.setMonthKey(monthKey);
        ActivityAccountMonth monthAccount = activityAccountMonthDao.queryActivityAccountMonth(monthReq);

        // ===== 3. 日账户 =====
        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountDay dayReq = new ActivityAccountDay();
        dayReq.setUserId(userId);
        dayReq.setActivityId(activityId);
        dayReq.setDayKey(dayKey);
        ActivityAccountDay dayAccount = activityAccountDayDao.queryActivityAccountDay(dayReq);

        // ===== 4. 组装快照 =====
        return AccountSurplusSnapshot.builder()
                .totalSurplus(account == null ? 0 : account.getTotalSurplus())
                .monthSurplus(monthAccount == null ? 0 : monthAccount.getMonthSurplus())
                .daySurplus(dayAccount == null ? 0 : dayAccount.getDaySurplus())
                .build();
    }

}
