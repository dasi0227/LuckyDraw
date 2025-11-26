package com.dasi.domain.strategy.service.rule.tree.impl;


import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.vo.RuleEdgeVO;
import com.dasi.domain.strategy.model.vo.RuleNodeVO;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import com.dasi.domain.strategy.service.rule.tree.IRuleTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RuleTreeEngine implements IRuleTreeEngine {

    private final Map<String, IRuleTree> ruleTreeMap;

    private final RuleTreeVO ruleTreeVO;

    public RuleTreeEngine(Map<String, IRuleTree> ruleTreeMap, RuleTreeVO ruleTreeVO) {
        this.ruleTreeMap = ruleTreeMap;
        this.ruleTreeVO = ruleTreeVO;
    }

    @Override
    public RuleCheckResult process(String userId, Long strategyId, Integer awardId) {

        // 获取基础信息
        String treeRoot = this.ruleTreeVO.getTreeRoot();
        Map<String, RuleNodeVO> treeNodeMap = this.ruleTreeVO.getTreeNodeMap();

        RuleCheckResult ruleCheckResult = null;
        RuleNodeVO curTreeNode = treeNodeMap.get(treeRoot);
        while (curTreeNode != null) {
            IRuleTree ruleTree = ruleTreeMap.get(curTreeNode.getRuleModel());
            String ruleValue = curTreeNode.getRuleValue();
            ruleCheckResult = ruleTree.logic(userId, strategyId, awardId, ruleValue);
            String nextTreeNode = next(ruleCheckResult.getRuleCheckOutcome(), curTreeNode.getRuleEdgeList());
            curTreeNode = treeNodeMap.get(nextTreeNode);
        }

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
