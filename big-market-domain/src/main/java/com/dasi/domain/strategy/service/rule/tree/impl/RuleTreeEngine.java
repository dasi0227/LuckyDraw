package com.dasi.domain.strategy.service.rule.tree.impl;


import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.model.tree.RuleEdgeVO;
import com.dasi.domain.strategy.model.tree.RuleNodeVO;
import com.dasi.domain.strategy.model.tree.RuleTreeVO;
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
    public RuleCheckResponse process(String userId, Long strategyId, Integer awardId) {

        // 获取基础信息
        String treeRoot = this.ruleTreeVO.getTreeRoot();
        Map<String, RuleNodeVO> treeNodeMap = this.ruleTreeVO.getTreeNodeMap();

        RuleCheckResponse ruleCheckResponse = null;
        RuleNodeVO curTreeNode = treeNodeMap.get(treeRoot);
        while (curTreeNode != null) {
            IRuleTree ruleTree = ruleTreeMap.get(curTreeNode.getRuleModel());
            String ruleValue = curTreeNode.getRuleValue();
            ruleCheckResponse = ruleTree.logic(userId, strategyId, awardId, ruleValue);
            String nextTreeNode = next(ruleCheckResponse.getRuleCheckResult(), curTreeNode.getRuleEdgeList());
            curTreeNode = treeNodeMap.get(nextTreeNode);
        }

        return ruleCheckResponse;
    }

    private String next(RuleCheckResult value, List<RuleEdgeVO> ruleEdgeVOList) {
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

    public boolean decide(RuleCheckResult value, RuleEdgeVO ruleEdgeVO) {
        switch (ruleEdgeVO.getRuleCheckType()) {
            case EQUAL:
                return value.equals(ruleEdgeVO.getRuleCheckResult());
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}
