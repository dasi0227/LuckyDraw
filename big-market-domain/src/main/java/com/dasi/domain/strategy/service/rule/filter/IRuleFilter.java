package com.dasi.domain.strategy.service.rule.filter;


import com.dasi.domain.strategy.model.dto.FilterResponse;
import com.dasi.domain.strategy.model.dto.FilterRequest;

public interface IRuleFilter<T extends FilterResponse.FilterDataEntity> {

    FilterResponse<T> filter(FilterRequest filterRequest);

}
