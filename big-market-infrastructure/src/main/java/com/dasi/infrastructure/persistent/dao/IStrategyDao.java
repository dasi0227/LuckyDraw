package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Strategy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IStrategyDao {
    Strategy queryStrategyByStrategyId(Long strategyId);
}
