package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.RuleEdge;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IRuleEdgeDao {

    List<RuleEdge> queryRuleEdgeListByTreeId(String treeId);

}
