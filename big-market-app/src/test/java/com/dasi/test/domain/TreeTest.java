package com.dasi.test.domain;

import com.dasi.domain.strategy.model.enumeration.RuleCheckResult;
import com.dasi.domain.strategy.model.enumeration.RuleCheckType;
import com.dasi.domain.strategy.model.enumeration.RuleModel;
import com.dasi.domain.strategy.model.tree.TreeEdge;
import com.dasi.domain.strategy.model.tree.TreeNode;
import com.dasi.domain.strategy.model.tree.TreeResult;
import com.dasi.domain.strategy.model.tree.TreeRoot;
import com.dasi.domain.strategy.service.rule.tree.IRuleTreeEngine;
import com.dasi.domain.strategy.service.rule.tree.RuleTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TreeTest {

    @Resource
    private RuleTreeFactory ruleTreeFactory;

    @Test
    public void test_ruleTree() {

        TreeNode rule_lock = TreeNode.builder()
                .treeId(10000001)
                .ruleModel(RuleModel.RULE_LOCK.getName())
                .ruleDesc("限定用户完成n次后抽奖解锁")
                .ruleValue("1")
                .treeLineList(new ArrayList<TreeEdge>() {{
                    add(TreeEdge.builder()
                            .treeId(10000001)
                            .ruleNodeFrom(RuleModel.RULE_LOCK.getName())
                            .ruleNodeTo(RuleModel.RULE_LUCK.getName())
                            .ruleCheckType(RuleCheckType.EQUAL)
                            .ruleCheckResult(RuleCheckResult.CAPTURE)
                            .build());

                    add(TreeEdge.builder()
                            .treeId(10000001)
                            .ruleNodeFrom(RuleModel.RULE_LOCK.getName())
                            .ruleNodeTo(RuleModel.RULE_STOCK.getName())
                            .ruleCheckType(RuleCheckType.EQUAL)
                            .ruleCheckResult(RuleCheckResult.PERMIT)
                            .build());
                }})
                .build();

        TreeNode rule_luck = TreeNode.builder()
                .treeId(10000001)
                .ruleModel(RuleModel.RULE_LUCK.getName())
                .ruleDesc("随机积分")
                .ruleValue("1")
                .treeLineList(null)
                .build();

        TreeNode rule_stock = TreeNode.builder()
                .treeId(10000001)
                .ruleModel(RuleModel.RULE_STOCK.getName())
                .ruleDesc("库存处理")
                .ruleValue(null)
                .treeLineList(new ArrayList<TreeEdge>() {{
                    add(TreeEdge.builder()
                            .treeId(10000001)
                            .ruleNodeFrom(RuleModel.RULE_LOCK.getName())
                            .ruleNodeTo(RuleModel.RULE_LUCK.getName())
                            .ruleCheckType(RuleCheckType.EQUAL)
                            .ruleCheckResult(RuleCheckResult.CAPTURE)
                            .build());
                }})
                .build();

        TreeRoot treeRoot = TreeRoot.builder()
                .id(10000001)
                .treeName("决策树规则：name")
                .treeDesc("决策树规则：desc")
                .treeRoot(RuleModel.RULE_LOCK.getName())
                .build();

        treeRoot.setTreeNodeMap(new HashMap<String, TreeNode>() {{
            put(RuleModel.RULE_LOCK.getName(), rule_lock);
            put(RuleModel.RULE_STOCK.getName(), rule_stock);
            put(RuleModel.RULE_LUCK.getName(), rule_luck);
        }});

        IRuleTreeEngine treeEngine = ruleTreeFactory.getTreeEngine(treeRoot);
        TreeResult result = treeEngine.process("wyw", 100001L, 1000);

        log.info("测试结果：{}", result);
    }
}
