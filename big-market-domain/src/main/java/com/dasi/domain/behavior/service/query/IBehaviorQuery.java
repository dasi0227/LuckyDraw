package com.dasi.domain.behavior.service.query;

import com.dasi.domain.behavior.model.io.QueryActivityBehaviorContext;
import com.dasi.domain.behavior.model.io.QueryActivityBehaviorResult;

import java.util.List;

public interface IBehaviorQuery {

    List<QueryActivityBehaviorResult> queryBehavior(QueryActivityBehaviorContext queryAccountContext);

}
