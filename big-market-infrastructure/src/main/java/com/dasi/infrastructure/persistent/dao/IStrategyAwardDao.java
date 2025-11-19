package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IStrategyAwardDao {
    List<StrategyAward> queryStrategyAwardListByStrategyId(Long strategyId);
}
