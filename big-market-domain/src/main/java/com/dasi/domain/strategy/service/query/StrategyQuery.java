package com.dasi.domain.strategy.service.query;

import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.io.QueryActivityAwardContext;
import com.dasi.domain.strategy.model.io.QueryActivityAwardResult;
import com.dasi.domain.strategy.model.io.QueryActivityLuckContext;
import com.dasi.domain.strategy.model.io.QueryActivityLuckResult;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StrategyQuery implements IStrategyQuery {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public List<QueryActivityAwardResult> queryActivityAward(QueryActivityAwardContext queryActivityAwardContext) {

        String userId = queryActivityAwardContext.getUserId();
        Long activityId = queryActivityAwardContext.getActivityId();

        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        // 1. 先拿到当前活动对应的策略的所有奖品
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardListByActivityId(activityId);

        // 2. 查询奖品的详细信息
        Map<String, AwardEntity> awardEntityMap = strategyRepository.queryAwardMapByActivityId(strategyAwardEntityList, activityId);

        // 3. 查询策略奖品的详细信息
        Map<String, Integer> limitLotteryCountMap = strategyRepository.queryRuleLockLimitMapByActivityId(strategyAwardEntityList, activityId);

        // 4. 查询用户的详细信息
        Integer userLotteryCount = strategyRepository.queryUserLotteryCountByActivityId(userId, activityId);
        Map<String, Integer> needLotteryCountMap = limitLotteryCountMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Math.max(entry.getValue() - userLotteryCount, 0)));

        return strategyAwardEntityList.stream()
                .sorted(Comparator.comparingInt(StrategyAwardEntity::getAwardIndex))
                .map(strategyAwardEntity -> {
                    Long awardId = strategyAwardEntity.getAwardId();
                    String key = String.valueOf(awardId);
                    return QueryActivityAwardResult.builder()
                            .awardId(awardId)
                            .awardRate(strategyAwardEntity.getAwardRate())
                            .awardIndex(strategyAwardEntity.getAwardIndex())
                            .awardName(awardEntityMap.get(key).getAwardName())
                            .needLotteryCount(needLotteryCountMap.get(key))
                            .isLock(needLotteryCountMap.get(key) > 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public QueryActivityLuckResult queryActivityLuck(QueryActivityLuckContext queryActivityLuckContext) {

        String userId = queryActivityLuckContext.getUserId();
        Long activityId = queryActivityLuckContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        // 1. 基础信息
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        int accountLuck = strategyRepository.queryActivityAccountLuck(userId, activityId);
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleModel.RULE_LUCK.name());
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardListByActivityId(activityId);
        Map<String, AwardEntity> awardEntityMap = strategyRepository.queryAwardMapByActivityId(strategyAwardEntityList, activityId);

        List<String> awardNameList = new ArrayList<>();

        // 2. 没配置幸运值规则：直接返回所有奖品
        if (StringUtils.isBlank(ruleValue)) {
            for (AwardEntity awardEntity : awardEntityMap.values()) {
                awardNameList.add(awardEntity.getAwardName());
            }
            return QueryActivityLuckResult.builder()
                    .accountLuck(accountLuck)
                    .prevLuck(-1)
                    .nextLuck(-1)
                    .awardNameList(awardNameList)
                    .build();
        }

        // 3. 解析得到积分阈值和对应的奖品列表
        Map<Integer, String> LuckMap = new TreeMap<>();
        for (String group : ruleValue.split(Delimiter.SPACE)) {
            if (StringUtils.isBlank(group)) continue;
            String[] parts = group.split(Delimiter.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("幸运值规则格式非法：" + group);
            LuckMap.put(Integer.parseInt(parts[0]), parts[1]);
        }

        // 4. 根据用户积分找到 prevLuck（当前档）和 nextLuck（下一档）
        int prevLuck = -1;
        int nextLuck = -1;

        for (Map.Entry<Integer, String> entry : LuckMap.entrySet()) {
            Integer threshold = entry.getKey();
            if (accountLuck >= threshold) {
                prevLuck = threshold;
            } else {
                nextLuck = threshold;
                break;
            }
        }

        // 5. 选择本次应该展示的奖品列表
        if (prevLuck == -1) {
            for (AwardEntity awardEntity : awardEntityMap.values()) {
                awardNameList.add(awardEntity.getAwardName());
            }
        } else {
            for (String awardIdStr : LuckMap.get(prevLuck).split(Delimiter.COMMA)) {
                awardNameList.add(awardEntityMap.get(awardIdStr).getAwardName());
            }
        }

        return QueryActivityLuckResult.builder()
                .accountLuck(accountLuck)
                .prevLuck(prevLuck)
                .nextLuck(nextLuck)
                .awardNameList(awardNameList)
                .build();
    }

}
