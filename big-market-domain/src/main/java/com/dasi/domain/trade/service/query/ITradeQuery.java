package com.dasi.domain.trade.service.query;

import com.dasi.domain.trade.model.io.QueryActivityConvertContext;
import com.dasi.domain.trade.model.io.QueryActivityConvertResult;

import java.util.List;

public interface ITradeQuery {


    List<QueryActivityConvertResult> queryActivityConvertList(QueryActivityConvertContext queryActivityConvertContext);

}
