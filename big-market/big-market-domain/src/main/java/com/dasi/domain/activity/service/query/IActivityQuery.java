package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.io.QueryActivityAccountContext;
import com.dasi.domain.activity.model.io.QueryActivityAccountResult;

public interface IActivityQuery {

    QueryActivityAccountResult queryActivityAccount(QueryActivityAccountContext queryActivityAccountContext);

}
