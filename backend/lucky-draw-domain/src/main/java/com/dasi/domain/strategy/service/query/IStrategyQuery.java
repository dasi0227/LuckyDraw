package com.dasi.domain.strategy.service.query;

import com.dasi.domain.strategy.model.io.QueryActivityAwardContext;
import com.dasi.domain.strategy.model.io.QueryActivityAwardResult;
import com.dasi.domain.strategy.model.io.QueryActivityLuckContext;
import com.dasi.domain.strategy.model.io.QueryActivityLuckResult;

import java.util.List;

public interface IStrategyQuery {

    List<QueryActivityAwardResult> queryActivityAward(QueryActivityAwardContext queryActivityAwardContext);

    QueryActivityLuckResult queryActivityLuck(QueryActivityLuckContext queryActivityLuckContext);

}
