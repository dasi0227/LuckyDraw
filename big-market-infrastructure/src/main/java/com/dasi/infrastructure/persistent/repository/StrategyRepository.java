package com.dasi.infrastructure.persistent.repository;

import com.dasi.domain.strategy.model.rule.RuleCheckOutcome;
import com.dasi.domain.strategy.model.rule.RuleCheckType;
import com.dasi.domain.strategy.model.message.StockUpdateMessage;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.model.tree.RuleEdgeVO;
import com.dasi.domain.strategy.model.tree.RuleNodeVO;
import com.dasi.domain.strategy.model.tree.RuleTreeVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.Character;
import com.dasi.types.constant.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private IRedisService redisService;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId) {

        // 先看缓存是否有【策略奖品】列表
        String cacheKey = RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> entities = redisService.getValue(cacheKey);
        if (null != entities && !entities.isEmpty()) {
            return entities;
        }

        // 如果缓存没有，就从数据库读取【策略奖品】，然后再转换为所需要的实体
        List<StrategyAward> list = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        entities = new ArrayList<>(list.size());
        for (StrategyAward strategyAward : list) {
            StrategyAwardEntity entity = StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardTitle(strategyAward.getAwardTitle())
                    .awardSubtitle(strategyAward.getAwardSubtitle())
                    .sort(strategyAward.getSort())
                    .awardCount(strategyAward.getAwardCount())
                    .awardCountSurplus(strategyAward.getAwardCountSurplus())
                    .awardRate(strategyAward.getAwardRate())
                    .build();
            entities.add(entity);
        }

        // 从数据库查找完之后，要放入缓存之中
        redisService.setValue(cacheKey, entities);

        return entities;
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
    public Integer getStrategyAwardAssemble(String cacheKey, int randomNum) {
        return Integer.valueOf(redisService.getFromMap(RedisKey.STRATEGY_RATE_TABLE_KEY + cacheKey, String.valueOf(randomNum)));
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 先看缓存是否有【策略】列表
        String cacheKey = RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity entity = redisService.getValue(cacheKey);
        if (null != entity) {
            return entity;
        }

        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        entity = StrategyEntity.builder()
            .strategyId(strategy.getStrategyId())
            .strategyDesc(strategy.getStrategyDesc())
            .ruleModels(strategy.getRuleModels())
            .build();
        redisService.setValue(cacheKey, entity);

        return entity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRuleByStrategyIDAndRuleModel(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleRequest = new StrategyRule();
        strategyRuleRequest.setStrategyId(strategyId);
        strategyRuleRequest.setRuleModel(ruleModel);
        StrategyRule strategyRuleResponse = strategyRuleDao.queryStrategyRuleByRuleModel(strategyRuleRequest);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleResponse.getStrategyId())
                .awardId(strategyRuleResponse.getAwardId())
                .ruleType(strategyRuleResponse.getRuleType())
                .ruleModel(strategyRuleResponse.getRuleModel())
                .ruleValue(strategyRuleResponse.getRuleValue())
                .ruleDesc(strategyRuleResponse.getRuleDesc())
                .build();
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);
        return strategyRuleDao.queryStrategyRuleValue(strategyRule);
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        return queryStrategyRuleValue(strategyId, null, ruleModel);
    }

    @Override
    public AwardEntity queryAwardEntityByAwardId(Integer awardId) {
        Award award = awardDao.queryAwardByAwardId(awardId);
        return AwardEntity.builder()
                .awardId(award.getAwardId())
                .awardKey(award.getAwardKey())
                .awardConfig(award.getAwardConfig())
                .awardDesc(award.getAwardDesc())
                .build();
    }

    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        // 1. 查缓存
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

        // 5. 放入缓存
        redisService.setValue(cacheKey, ruleTreeVO);

        return ruleTreeVO;
    }

    @Override
    public String[] queryStrategyRuleModelByStrategyIdAndAwardId(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModels(strategyAward);
        return ruleModels.split(Character.COMMA);
    }

    @Override
    public void cacheStrategyAwardStock(String cacheKey, Integer awardCount) {
        if (!redisService.isExists(cacheKey)) {
            redisService.setAtomicLong(cacheKey, awardCount);
        }
    }

    @Override
    public long subStrategyAwardCount(String cacheKey) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0) {
            redisService.setAtomicLong(cacheKey, 0);
            return -1L;
        }
        String lockKey = cacheKey + Character.UNDERSCORE + (surplus + 1);
        if (redisService.setNx(lockKey)) {
            return surplus;
        } else {
            return -1L;
        }
    }

    @Override
    public void sendStockConsumeToQueue(StockUpdateMessage stockUpdateMessage) {
        String queueKey = RedisKey.STRATEGY_AWARD_STOCK_QUEUE_KEY;
        RBlockingQueue<StockUpdateMessage> blockingQueue = redisService.getBlockingQueue(queueKey);
        RDelayedQueue<StockUpdateMessage> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // 构造延迟队列，三秒后才放入
        delayedQueue.offer(stockUpdateMessage, 3, TimeUnit.SECONDS);
    }

    @Override
    public StockUpdateMessage getQueueValue() {
        String queueKey = RedisKey.STRATEGY_AWARD_STOCK_QUEUE_KEY;
        RBlockingQueue<StockUpdateMessage> blockingQueue = redisService.getBlockingQueue(queueKey);
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
