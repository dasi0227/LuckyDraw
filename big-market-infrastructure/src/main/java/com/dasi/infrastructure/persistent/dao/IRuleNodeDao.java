package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.RuleNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IRuleNodeDao {

    List<RuleNode> queryRuleNodeListByTreeId(String treeId);

    Integer queryRuleNodeLockCountByTreeId(String treeId);
}
