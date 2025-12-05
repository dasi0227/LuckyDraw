package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.io.QueryAccountContext;
import com.dasi.domain.activity.model.io.QueryAccountResult;

public interface IActivityQuery {

    QueryAccountResult queryActivityAccount(QueryAccountContext queryAccountContext);

}
