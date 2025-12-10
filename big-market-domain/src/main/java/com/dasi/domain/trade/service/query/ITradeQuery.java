package com.dasi.domain.trade.service.query;

import com.dasi.domain.trade.model.io.QueryConvertContext;
import com.dasi.domain.trade.model.io.QueryConvertResult;

import java.util.List;

public interface ITradeQuery {


    List<QueryConvertResult> queryConvertListByActivityId(QueryConvertContext queryConvertContext);

}
