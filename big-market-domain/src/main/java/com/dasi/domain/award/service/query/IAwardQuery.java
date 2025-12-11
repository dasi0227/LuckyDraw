package com.dasi.domain.award.service.query;

import com.dasi.domain.award.model.io.QueryUserAwardContext;
import com.dasi.domain.award.model.io.QueryUserAwardResult;

import java.util.List;

public interface IAwardQuery {

    List<QueryUserAwardResult> queryUserAwardRaffleList(QueryUserAwardContext queryAccountContext);

}
