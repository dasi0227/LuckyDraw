package com.dasi.domain.trade.service.trade;

import com.dasi.domain.trade.model.entity.TradeOrderEntity;
import com.dasi.domain.trade.model.io.ConvertContext;
import com.dasi.domain.trade.model.io.ConvertResult;

public interface IPointTrade {

    ConvertResult doPointTrade(ConvertContext convertContext);

    void updateTradeOrderState(TradeOrderEntity tradeOrderEntity);

}