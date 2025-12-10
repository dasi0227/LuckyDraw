package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Trade;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ITradeDao {

    Trade queryTradeByTradeId(Long tradeId);

}
