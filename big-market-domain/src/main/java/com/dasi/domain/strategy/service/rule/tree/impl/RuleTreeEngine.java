package com.dasi.domain.strategy.service.rule.tree.impl;


import com.dasi.domain.strategy.model.enumeration.RuleCheckResult;
import com.dasi.domain.strategy.model.tree.TreeEdge;
import com.dasi.domain.strategy.model.tree.TreeNode;
import com.dasi.domain.strategy.model.tree.TreeResult;
import com.dasi.domain.strategy.model.tree.TreeRoot;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import com.dasi.domain.strategy.service.rule.tree.IRuleTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RuleTreeEngine implements IRuleTreeEngine {

    private final Map<String, IRuleTree> ruleTreeMap;

    private final TreeRoot treeRoot;

    public RuleTreeEngine(Map<String, IRuleTree> ruleTreeMap, TreeRoot treeRoot) {
        this.ruleTreeMap = ruleTreeMap;
        this.treeRoot = treeRoot;
    }

    @Override
    public TreeResult process(String userId, Long strategyId, Integer awardId) {

        // 获取基础信息
        String treeRoot = this.treeRoot.getTreeRoot();
        Map<String, TreeNode> treeNodeMap = this.treeRoot.getTreeNodeMap();

        TreeResult treeResult = null;
        TreeNode curTreeNode = treeNodeMap.get(treeRoot);
        while (curTreeNode != null) {
            IRuleTree ruleTree = ruleTreeMap.get(curTreeNode.getRuleModel());
            treeResult = ruleTree.logic(userId, strategyId, awardId);
            log.info("【决策树引擎】：RuleModel = {}，RuleResult = {}", curTreeNode.getRuleModel(), treeResult);
            String nextTreeNode = next(treeResult.getRuleCheckResult(), curTreeNode.getTreeLineList());
            curTreeNode = treeNodeMap.get(nextTreeNode);
        }


        return treeResult;
    }

    private String next(RuleCheckResult value, List<TreeEdge> treeEdgeList) {
        if (treeEdgeList == null || treeEdgeList.isEmpty()) {
            return null;
        }

        for (TreeEdge treeEdge : treeEdgeList) {
            if (decide(value, treeEdge)) {
                return treeEdge.getRuleNodeTo();
            }
        }

        throw new RuntimeException("配置错误，未找到可执行节点");
    }

    public boolean decide(RuleCheckResult value, TreeEdge treeEdge) {
        switch (treeEdge.getRuleCheckType()) {
            case EQUAL:
                return value.equals(treeEdge.getRuleCheckResult());
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}
