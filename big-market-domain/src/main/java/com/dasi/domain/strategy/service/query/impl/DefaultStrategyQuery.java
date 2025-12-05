package com.dasi.domain.strategy.service.query.impl;

import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.io.ActivityAwardDetail;
import com.dasi.domain.strategy.model.io.StrategyRuleWeightDetail;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.query.IStrategyQuery;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultStrategyQuery implements IStrategyQuery {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public List<ActivityAwardDetail> queryActivityAward(String userId, Long activityId) {

        if (StringUtils.isBlank(userId)) throw new AppException("（抽奖）缺少参数 userId");
        if (activityId == null) throw new AppException("（抽奖）缺少参数 activityId");

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
                    return ActivityAwardDetail.builder()
                            .awardId(awardId)
                            .awardTitle(strategyAwardEntity.getAwardTitle())
                            .awardRate(strategyAwardEntity.getAwardRate())
                            .awardIndex(strategyAwardEntity.getAwardIndex())
                            .awardName(awardEntityMap.get(key).getAwardName())
                            .awardConfig(awardEntityMap.get(key).getAwardConfig())
                            .awardDesc(awardEntityMap.get(key).getAwardDesc())
                            .limitLotteryCount(limitLotteryCountMap.get(key))
                            .needLotteryCount(needLotteryCountMap.get(key))
                            .isLock(needLotteryCountMap.get(key) > 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public StrategyRuleWeightDetail queryStrategyRuleWeight(String userId, Long activityId) {

        if (StringUtils.isBlank(userId)) throw new AppException("（抽奖）缺少参数 userId");
        if (activityId == null) throw new AppException("（抽奖）缺少参数 activityId");

        // 1. 基础信息
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        int userScore = strategyRepository.queryUserScoreByStrategyId(userId, strategyId);
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleModel.RULE_WEIGHT.getCode());
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardListByActivityId(activityId);
        Map<String, AwardEntity> awardEntityMap = strategyRepository.queryAwardMapByActivityId(strategyAwardEntityList, activityId);

        List<String> awardNameList = new ArrayList<>();

        // 2. 没配置权重规则：直接返回所有奖品
        if (StringUtils.isBlank(ruleValue)) {
            for (AwardEntity awardEntity : awardEntityMap.values()) {
                awardNameList.add(awardEntity.getAwardName());
            }
            return StrategyRuleWeightDetail.builder()
                    .userScore(userScore)
                    .prevWeight(-1)
                    .nextWeight(-1)
                    .awardNameList(awardNameList)
                    .build();
        }

        // 3. 解析得到积分阈值和对应的奖品列表
        Map<Integer, String> weightMap = new TreeMap<>();
        for (String group : ruleValue.split(Delimiter.SPACE)) {
            if (StringUtils.isBlank(group)) continue;
            String[] parts = group.split(Delimiter.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("权重规则格式非法：" + group);
            weightMap.put(Integer.parseInt(parts[0]), parts[1]);
        }

        // 4. 根据用户积分找到 prevWeight（当前档）和 nextWeight（下一档）
        int prevWeight = -1;
        int nextWeight = -1;

        for (Map.Entry<Integer, String> entry : weightMap.entrySet()) {
            Integer threshold = entry.getKey();
            if (userScore >= threshold) {
                prevWeight = threshold;
            } else {
                nextWeight = threshold;
                break;
            }
        }

        // 5. 选择本次应该展示的奖品列表
        if (prevWeight == -1) {
            for (AwardEntity awardEntity : awardEntityMap.values()) {
                awardNameList.add(awardEntity.getAwardName());
            }
        } else {
            for (String awardIdStr : weightMap.get(prevWeight).split(Delimiter.COMMA)) {
                awardNameList.add(awardEntityMap.get(awardIdStr).getAwardName());
            }
        }

        return StrategyRuleWeightDetail.builder()
                .userScore(userScore)
                .prevWeight(prevWeight)
                .nextWeight(nextWeight)
                .awardNameList(awardNameList)
                .build();
    }

}
