package com.dasi.domain.strategy.repository;

import com.dasi.domain.strategy.model.entity.*;
import com.dasi.domain.strategy.model.io.StrategyAwardStock;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);

    int queryUserLotteryCountByActivityId(String userId, Long activityId);

    int queryUserLotteryCountByStrategyId(String userId, Long strategyId);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByStrategyIDAndRuleModel(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    String queryStrategyAwardTreeIdByStrategyIdAndAwardId(Long strategyId, Long awardId);

    void cacheStrategyAwardStock(String cacheKey, Integer stock);

    void cacheStrategyAwardRate(String cacheKey, Integer rateRange, Map<String, String> strategyAwardMap);

    int getRateRange(String cacheKey);

    Long getRandomStrategyAward(String cacheKey, int randomNum);

    long subtractStrategyAwardStock(String cacheKey, LocalDateTime activityEndTime);

    void sendStrategyAwardStockConsumeToMQ(StrategyAwardStock strategyAwardStock);

    StrategyAwardStock getQueueValue();

    void updateStrategyAwardStock(Long strategyId, Long awardId);

    LocalDateTime queryActivityEndTimeByStrategyId(Long strategyId);

    List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId);

    Map<String, AwardEntity> queryAwardMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId);

    Map<String, Integer> queryRuleLockLimitMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId);

    int queryUserScoreByStrategyId(String userId, Long strategyId);

}
