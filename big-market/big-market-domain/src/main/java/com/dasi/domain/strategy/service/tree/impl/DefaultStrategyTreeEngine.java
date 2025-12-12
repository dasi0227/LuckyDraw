package com.dasi.domain.strategy.service.tree.impl;


import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.model.vo.RuleEdgeVO;
import com.dasi.domain.strategy.model.vo.RuleNodeVO;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;
import com.dasi.domain.strategy.service.tree.IStrategyTree;
import com.dasi.domain.strategy.service.tree.IStrategyTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultStrategyTreeEngine implements IStrategyTreeEngine {

    private final Map<String, IStrategyTree> ruleTreeMap;

    private final RuleTreeVO ruleTreeVO;

    public DefaultStrategyTreeEngine(Map<String, IStrategyTree> ruleTreeMap, RuleTreeVO ruleTreeVO) {
        this.ruleTreeMap = ruleTreeMap;
        this.ruleTreeVO = ruleTreeVO;
    }

    @Override
    public RuleCheckResult process(String userId, Long strategyId, Long awardId) {

        // 获取基础信息
        String treeRoot = this.ruleTreeVO.getTreeRoot();
        Map<String, RuleNodeVO> treeNodeMap = this.ruleTreeVO.getTreeNodeMap();

        boolean isLock = false;
        boolean isEmpty = false;

        RuleCheckResult ruleCheckResult = new RuleCheckResult();
        RuleNodeVO curTreeNode = treeNodeMap.get(treeRoot);
        while (curTreeNode != null) {

            IStrategyTree ruleTree = ruleTreeMap.get(curTreeNode.getRuleModel());
            String ruleValue = curTreeNode.getRuleValue();
            ruleCheckResult = ruleTree.logic(userId, strategyId, awardId, ruleValue);

            if (ruleCheckResult.getRuleCheckOutcome().equals(RuleCheckOutcome.CAPTURE)) {
                RuleModel ruleModel = ruleCheckResult.getRuleModel();
                isLock |= (ruleModel == RuleModel.RULE_LOCK);
                isEmpty  |= (ruleModel == RuleModel.RULE_STOCK);
            }

            String nextTreeNode = next(ruleCheckResult.getRuleCheckOutcome(), curTreeNode.getRuleEdgeList());
            curTreeNode = treeNodeMap.get(nextTreeNode);
        }

        ruleCheckResult.setIsLock(isLock);
        ruleCheckResult.setIsEmpty(isEmpty);
        return ruleCheckResult;
    }

    private String next(RuleCheckOutcome value, List<RuleEdgeVO> ruleEdgeVOList) {
        if (ruleEdgeVOList == null || ruleEdgeVOList.isEmpty()) {
            return null;
        }

        for (RuleEdgeVO ruleEdgeVO : ruleEdgeVOList) {
            if (decide(value, ruleEdgeVO)) {
                return ruleEdgeVO.getRuleNodeTo();
            }
        }

        return null;
    }

    public boolean decide(RuleCheckOutcome value, RuleEdgeVO ruleEdgeVO) {
        switch (ruleEdgeVO.getRuleCheckType()) {
            case EQUAL:
                return value.equals(ruleEdgeVO.getRuleCheckOutcome());
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}
