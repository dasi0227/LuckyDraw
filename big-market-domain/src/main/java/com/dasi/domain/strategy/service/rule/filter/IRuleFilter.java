package com.dasi.domain.strategy.service.rule.filter;


import com.dasi.domain.strategy.model.io.FilterResponse;
import com.dasi.domain.strategy.model.io.FilterRequest;

public interface IRuleFilter<T extends FilterResponse.FilterDataEntity> {

    FilterResponse<T> filter(FilterRequest filterRequest);

}
