package com.dasi.infrastructure.persistent.repository;

import com.dasi.domain.strategy.model.entity.*;
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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IRedisService redisService;

    @Override
    public Long queryStrategyIdByActivityId(Long activityId) {
        String cacheKey = RedisKey.STRATEGY_ID_KEY + activityId;
        Long strategyId = redisService.getValue(cacheKey);
        if (strategyId != null) {
            return strategyId;
        }

        strategyId = activityDao.queryStrategyIdByActivityId(activityId);
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
        redisService.setValue(cacheKey, activityId);
        return activityId;
    }

    @Override
    public int queryUserLotteryCount(String userId, Long strategyId) {
        Long activityId = queryActivityIdByStrategyId(strategyId);
        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setUserId(userId);
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setDay(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDay);
        if (activityAccountDay == null) return 0;
        return activityAccountDay.getDayAllocate() - activityAccountDay.getDaySurplus();
    }

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId) {

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
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 先查缓存
        String cacheKey = RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (null != strategyEntity) {
            return strategyEntity;
        }

        // 再查数据库
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
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
    public AwardEntity queryAwardByAwardId(Integer awardId) {
        // 先查缓存
        String cacheKey = RedisKey.AWARD_KEY + awardId;
        AwardEntity awardEntity = redisService.getValue(cacheKey);
        if (awardEntity != null) {
            return awardEntity;
        }

        // 再查数据库
        Award award = awardDao.queryAwardByAwardId(awardId);
        awardEntity = AwardEntity.builder()
                .awardId(award.getAwardId())
                .awardName(award.getAwardName())
                .awardConfig(award.getAwardConfig())
                .awardDesc(award.getAwardDesc())
                .build();

        // 缓存后返回
        redisService.setValue(cacheKey, awardEntity);
        return awardEntity;
    }

    @Override
    public String queryStrategyAwardTreeIdByStrategyIdAndAwardId(Long strategyId, Integer awardId) {
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
    public Integer getRandomStrategyAward(String cacheKey, int randomNum) {
        return Integer.valueOf(redisService.getFromMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey, String.valueOf(randomNum)));
    }

    @Override
    public long subStrategyAwardStock(String cacheKey) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0L) {
            redisService.setAtomicLong(cacheKey, 0L);
            return -1L;
        }
        String lockKey = cacheKey + Delimiter.UNDERSCORE + (surplus + 1);
        if (!redisService.setNx(lockKey)) {
            return -1L;
        }
        return surplus;
    }

    @Override
    public void sendStrategyAwardStockConsumeToMQ(StrategyAwardStockEntity strategyAwardStockEntity) {
        String queueKey = RedisKey.STRATEGY_AWARD_STOCK_QUEUE_KEY;
        RBlockingQueue<StrategyAwardStockEntity> blockingQueue = redisService.getBlockingQueue(queueKey);
        RDelayedQueue<StrategyAwardStockEntity> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // 构造延迟队列，三秒后才放入
        delayedQueue.offer(strategyAwardStockEntity, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockEntity getQueueValue() {
        String queueKey = RedisKey.STRATEGY_AWARD_STOCK_QUEUE_KEY;
        RBlockingQueue<StrategyAwardStockEntity> blockingQueue = redisService.getBlockingQueue(queueKey);
        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

}
