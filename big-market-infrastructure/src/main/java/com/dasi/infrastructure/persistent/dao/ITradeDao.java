package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Trade;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITradeDao {

    Trade queryTradeByTradeId(Long tradeId);

    List<Trade> queryActivityConvertList(Long activityId);

}
