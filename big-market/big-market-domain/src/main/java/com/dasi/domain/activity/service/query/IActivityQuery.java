package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.io.QueryActivityAccountContext;
import com.dasi.domain.activity.model.io.QueryActivityAccountResult;
import com.dasi.domain.activity.model.io.QueryActivityInfoContext;
import com.dasi.domain.activity.model.io.QueryActivityInfoResult;

public interface IActivityQuery {

    QueryActivityAccountResult queryActivityAccount(QueryActivityAccountContext queryActivityAccountContext);

    QueryActivityInfoResult queryActivityInfo(QueryActivityInfoContext queryActivityInfoContext);
}
