package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.strategy.model.entity.*;
import com.dasi.domain.strategy.model.io.StrategyAwardStock;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleCheckType;
import com.dasi.domain.strategy.model.vo.RuleEdgeVO;
import com.dasi.domain.strategy.model.vo.RuleNodeVO;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IStrategyDao strategyDao;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Resource
    private IAwardDao awardDao;

    @Resource
    private IRuleTreeDao ruleTreeDao;

    @Resource
    private IRuleEdgeDao ruleEdgeDao;

    @Resource
    private IRuleNodeDao ruleNodeDao;

    @Resource
    private IActivityDao activityDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

    @Resource
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IActivityAccountMonthDao activityAccountMonthDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Override
    public Long queryStrategyIdByActivityId(Long activityId) {
        String cacheKey = RedisKey.STRATEGY_ID_KEY + activityId;
        Long strategyId = redisService.getValue(cacheKey);
        if (strategyId != null) {
            return strategyId;
        }

        strategyId = activityDao.queryStrategyIdByActivityId(activityId);
        if (strategyId == null) throw new AppException("（数据库）StrategyId 不存在：activityId=" + activityId);
        redisService.setValue(cacheKey, strategyId);
        return strategyId;
    }

    @Override
    public Long queryActivityIdByStrategyId(Long strategyId) {
        String cacheKey = RedisKey.ACTIVITY_ID_KEY + strategyId;
        Long activityId = redisService.getValue(cacheKey);
        if (activityId != null) {
            return activityId;
        }

        activityId = activityDao.queryActivityIdByStrategyId(strategyId);
        if (activityId == null) throw new AppException("（数据库）ActivityId 不存在：strategyId=" + strategyId);
        redisService.setValue(cacheKey, activityId);
        return activityId;
    }

    @Override
    public int queryUserLotteryCountByActivityId(String userId, Long activityId) {
        Long strategyId = queryStrategyIdByActivityId(activityId);
        return queryUserLotteryCountByStrategyId(userId, strategyId);
    }


    // TODO：这里用总账户的分配 - 剩余来计算 LOCK 次数，因此这里必须确保账户已经存在
    @Override
    public int queryUserLotteryCountByStrategyId(String userId, Long strategyId) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccount activityAccount = new ActivityAccount();
            activityAccount.setUserId(userId);
            activityAccount.setActivityId(strategyId);
            activityAccount = activityAccountDao.queryActivityAccount(activityAccount);
            if (activityAccount == null) return 0;
            return activityAccount.getTotalAllocate() - activityAccount.getTotalSurplus();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId) {
        Long strategyId = redisService.getValue(RedisKey.STRATEGY_ID_KEY + activityId);
        if (strategyId == null) {
            strategyId = activityDao.queryStrategyIdByActivityId(activityId);
        }
        if (strategyId == null) throw new AppException("（数据库）StrategyId 不存在：activityId=" + activityId);

        return queryStrategyAwardListByStrategyId(strategyId);
    }

    // TODO：补充分数算法逻辑
    @Override
    public int queryUserScoreByStrategyId(String userId, Long strategyId) {
        Long activityId = queryActivityIdByStrategyId(strategyId);

        try {
            dbRouterStrategy.doRouter(userId);

            int userScore = 0;

            ActivityAccountDay activityAccountDayReq = new ActivityAccountDay();
            activityAccountDayReq.setUserId(userId);
            activityAccountDayReq.setActivityId(activityId);
            activityAccountDayReq.setDayKey(TimeUtil.thisDay(true));
            ActivityAccountDay activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDayReq);
            userScore += (activityAccountDay.getDayAllocate() - activityAccountDay.getDaySurplus()) * 1000;

            ActivityAccountMonth activityAccountMonthReq = new ActivityAccountMonth();
            activityAccountMonthReq.setUserId(userId);
            activityAccountMonthReq.setActivityId(activityId);
            activityAccountMonthReq.setMonthKey(TimeUtil.thisMonth(true));
            ActivityAccountMonth activityAccountMonth = activityAccountMonthDao.queryActivityAccountMonth(activityAccountMonthReq);
            userScore += (activityAccountMonth.getMonthAllocate() - activityAccountMonth.getMonthSurplus()) * 500;

            return userScore;
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId) {

        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntityList = redisService.getValue(cacheKey);
        if (null != strategyAwardEntityList && !strategyAwardEntityList.isEmpty()) {
            return strategyAwardEntityList;
        }

        // 再查数据库
        List<StrategyAward> strategyAwardList = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        if (strategyAwardList == null || strategyAwardList.isEmpty()) throw new AppException("（数据库）StrategyAwardList 不存在：strategyId=" + strategyId);
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
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (null != strategyEntity) {
            return strategyEntity;
        }

        // 再查数据库
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        if (strategy == null) throw new AppException("（数据库）Strategy 不存在：strategyId=" + strategyId);
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();

        // 缓存后返回
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRuleByStrategyIDAndRuleModel(Long strategyId, String ruleModel) {
        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_RULE_KEY + strategyId + Delimiter.UNDERSCORE + ruleModel;
        StrategyRuleEntity strategyRuleEntity = redisService.getValue(cacheKey);
        if (strategyRuleEntity != null) {
            return strategyRuleEntity;
        }

        // 再查数据库
        StrategyRule strategyRuleRequest = new StrategyRule();
        strategyRuleRequest.setStrategyId(strategyId);
        strategyRuleRequest.setRuleModel(ruleModel);
        StrategyRule strategyRuleResponse = strategyRuleDao.queryStrategyRuleByRuleModel(strategyRuleRequest);
        if (strategyRuleResponse == null) throw new AppException("（数据库）StrategyRule 不存在：strategyId=" + strategyId + ", ruleModel=" + ruleModel);
        strategyRuleEntity = StrategyRuleEntity.builder()
                .strategyId(strategyRuleResponse.getStrategyId())
                .ruleModel(strategyRuleResponse.getRuleModel())
                .ruleValue(strategyRuleResponse.getRuleValue())
                .ruleDesc(strategyRuleResponse.getRuleDesc())
                .build();

        // 缓存后返回
        redisService.setValue(cacheKey, strategyRuleEntity);
        return strategyRuleEntity;
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_RULE_VALUE_KEY + strategyId + Delimiter.UNDERSCORE + ruleModel;
        String ruleValue = redisService.getValue(cacheKey);
        if (ruleValue != null) {
            return ruleValue;
        }

        // 再查数据库
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setRuleModel(ruleModel);
        ruleValue = strategyRuleDao.queryStrategyRuleValue(strategyRule);

        // 缓存后返回
        redisService.setValue(cacheKey, ruleValue);
        return ruleValue;
    }

    @Override
    public String queryStrategyAwardTreeIdByStrategyIdAndAwardId(Long strategyId, Long awardId) {
        // 先查缓存
        String cacheKey = RedisKey.TREE_ID_KEY + strategyId + Delimiter.UNDERSCORE + awardId;
        String treeId = redisService.getValue(cacheKey);
        if (treeId != null) {
            return treeId;
        }

        // 再查数据库
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        treeId = strategyAwardDao.queryStrategyAwardTreeIdByStrategyIdAndAwardId(strategyAward);
        if (treeId == null) throw new AppException("（数据库）TreeId 不存在：strategyId=" + strategyId + ", awardId=" + awardId);


        // 缓存后返回
        redisService.setValue(cacheKey, treeId);
        return treeId;
    }

    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        // 1. 先查缓存
        String cacheKey = RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVOCache = redisService.getValue(cacheKey);
        if (null != ruleTreeVOCache) return ruleTreeVOCache;

        // 2. 建立 RuleNodeVO 到 RuleEdgeVO 列表的映射
        List<RuleEdge> ruleEdgeList = ruleEdgeDao.queryRuleEdgeListByTreeId(treeId);
        if (ruleEdgeList == null || ruleEdgeList.isEmpty()) throw new AppException("（数据库）规则树边不存在：treeId=" + treeId);
        Map<String, List<RuleEdgeVO>> ruleNode2EdgeMap = new HashMap<>();
        for (RuleEdge ruleEdge : ruleEdgeList) {
            RuleEdgeVO ruleEdgeVO = RuleEdgeVO.builder()
                    .treeId(ruleEdge.getTreeId())
                    .ruleNodeFrom(ruleEdge.getRuleNodeFrom())
                    .ruleNodeTo(ruleEdge.getRuleNodeTo())
                    .ruleCheckType(RuleCheckType.valueOf(ruleEdge.getRuleCheckType()))
                    .ruleCheckOutcome(RuleCheckOutcome.valueOf(ruleEdge.getRuleCheckResult()))
                    .build();

            List<RuleEdgeVO> ruleEdgeVOList = ruleNode2EdgeMap.computeIfAbsent(ruleEdgeVO.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleEdgeVOList.add(ruleEdgeVO);
        }

        // 3. 建立 RuleTreeVO 到 RuleNodeVO 列表的映射
        List<RuleNode> ruleNodeList = ruleNodeDao.queryRuleNodeListByTreeId(treeId);
        if (ruleNodeList == null || ruleNodeList.isEmpty()) throw new AppException("（数据库）规则树节点不存在：treeId=" + treeId);
        Map<String, RuleNodeVO> ruleTree2NodeMap = new HashMap<>();
        for (RuleNode ruleNode : ruleNodeList) {
            RuleNodeVO ruleNodeVO = RuleNodeVO.builder()
                    .treeId(ruleNode.getTreeId())
                    .ruleModel(ruleNode.getRuleModel())
                    .ruleValue(ruleNode.getRuleValue())
                    .ruleEdgeList(ruleNode2EdgeMap.get(ruleNode.getRuleModel()))
                    .build();
            ruleTree2NodeMap.put(ruleNode.getRuleModel(), ruleNodeVO);
        }

        // 4. 构造 RuleTreeVO
        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        if (ruleTree == null) throw new AppException("（数据库）规则树不存在：treeId=" + treeId);
        RuleTreeVO ruleTreeVO = RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeRoot(ruleTree.getTreeRoot())
                .treeNodeMap(ruleTree2NodeMap)
                .build();

        // 5. 缓存后返回
        redisService.setValue(cacheKey, ruleTreeVO);
        return ruleTreeVO;
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
            if (award == null) throw new AppException("（数据库）Award 不存在：awardId=" + awardId);
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
    public Map<String, Integer> queryRuleLockLimitMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.RULE_LOCK_LIMIT_MAP_KEY + activityId;
        Map<String, Integer> ruleLockLimitMap = redisService.getValue(cacheKey);
        if (ruleLockLimitMap != null && !ruleLockLimitMap.isEmpty()) {
            return ruleLockLimitMap;
        }

        // 再查数据库
        ruleLockLimitMap = new HashMap<>();
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntityList) {
            Long awardId = strategyAwardEntity.getAwardId();
            String treeId = strategyAwardEntity.getTreeId();
            if (StringUtils.isBlank(treeId)) {
                ruleLockLimitMap.put(String.valueOf(awardId), 0);
            } else {
                Integer lockCount = ruleNodeDao.queryRuleLockLimitByTreeId(treeId);
                ruleLockLimitMap.put(String.valueOf(awardId), lockCount == null ? 0 : lockCount);
            }
        }

        // 缓存后返回
        redisService.setValue(cacheKey, ruleLockLimitMap);
        return ruleLockLimitMap;
    }

    @Override
    public void cacheStrategyAwardStock(String cacheKey, Integer stock) {
        redisService.setAtomicLong(cacheKey, Long.valueOf(stock));
    }

    @Override
    public void cacheStrategyAwardRate(String cacheKey, Integer rateRange, Map<String, String> strategyAwardMap) {
        // 1. 存储当前策略对应的概率长度
        redisService.setValue(RedisKey.STRATEGY_RATE_RANGE_KEY + cacheKey, rateRange);

        // 2. 存储当前策略对应的概率奖品表
        RMap<String, String> cacheMap = redisService.getMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey);
        cacheMap.putAll(strategyAwardMap);
    }

    @Override
    public int getRateRange(String cacheKey) {
        return redisService.getValue(RedisKey.STRATEGY_RATE_RANGE_KEY + cacheKey);
    }

    @Override
    public Long getRandomStrategyAward(String cacheKey, int randomNum) {
        return Long.valueOf(redisService.getFromMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey, String.valueOf(randomNum)));
    }

    @Override
    public long subtractStrategyAwardStock(String cacheKey, LocalDateTime activityEndTime) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0L) {
            redisService.setAtomicLong(cacheKey, 0L);
            return -1L;
        }
        String lockKey = cacheKey + Delimiter.UNDERSCORE + (surplus + 1);
        Duration expire = Duration.ofMillis(
                activityEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        - System.currentTimeMillis()
                        + TimeUnit.DAYS.toMillis(1));
        if (!redisService.setNx(lockKey, expire)) {
            return -1L;
        }
        return surplus;
    }

    @Override
    public void sendStrategyAwardStockConsumeToMQ(StrategyAwardStock strategyAwardStock) {
        String queueKey = RedisKey.STRATEGY_AWARD_STOCK_QUEUE_KEY;
        RBlockingQueue<StrategyAwardStock> blockingQueue = redisService.getBlockingQueue(queueKey);
        RDelayedQueue<StrategyAwardStock> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // 构造延迟队列，三秒后才放入
        delayedQueue.offer(strategyAwardStock, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStock getQueueValue() {
        String queueKey = RedisKey.STRATEGY_AWARD_STOCK_QUEUE_KEY;
        RBlockingQueue<StrategyAwardStock> blockingQueue = redisService.getBlockingQueue(queueKey);
        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Long awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public LocalDateTime queryActivityEndTimeByStrategyId(Long strategyId) {
        String cacheKey = RedisKey.ACTIVITY_END_TIME_KEY + strategyId;
        String activityEndTime = redisService.getValue(cacheKey);
        if (activityEndTime != null) {
            return LocalDateTime.parse(activityEndTime);
        }

        Long activityId = queryActivityIdByStrategyId(strategyId);
        Activity activity = activityDao.queryActivityByActivityId(activityId);
        activityEndTime = activity.getActivityEndTime().toString();
        redisService.setValue(cacheKey, activityEndTime);

        return LocalDateTime.parse(activityEndTime);
    }

}
