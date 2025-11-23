package com.dasi.test.infrastructure;


import com.dasi.domain.strategy.model.tree.RuleTreeVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TreeDaoTest {

    @Resource
    private IStrategyRepository strategyRepository;

    @Test
    public void testStrategyRepository() {
        String treeId = "tree_lock";
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(treeId);
        log.info("ruleTreeVO = {}", ruleTreeVO);

    }

}

