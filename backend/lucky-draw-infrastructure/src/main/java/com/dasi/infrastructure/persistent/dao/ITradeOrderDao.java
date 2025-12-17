package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.TradeOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface ITradeOrderDao {

    TradeOrder queryTradeOrderByOrderId(String orderId);

    int saveTradeOrder(TradeOrder tradeOrder);

    int updateTradeState(TradeOrder tradeOrder);

}
