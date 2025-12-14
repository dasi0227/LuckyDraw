package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.io.*;

import java.util.List;

public interface IActivityQuery {

    QueryActivityAccountResult queryActivityAccount(QueryActivityAccountContext queryActivityAccountContext);

    QueryActivityInfoResult queryActivityInfo(QueryActivityInfoContext queryActivityInfoContext);

    List<QueryActivityListResult> queryActivityList();
}
