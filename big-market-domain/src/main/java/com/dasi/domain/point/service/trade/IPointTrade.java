package com.dasi.domain.point.service.trade;

import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.io.TradeContext;
import com.dasi.domain.point.model.io.TradeResult;

public interface IPointTrade {

    TradeResult doPointTrade(TradeContext tradeContext);

    void updateTradeOrderState(TradeOrderEntity tradeOrderEntity);

}