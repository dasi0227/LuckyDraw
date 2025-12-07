package com.dasi.domain.award.service.dispatch;

import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.io.DispatchContext;

public interface IAwardDispatchHandler {

    void dispatchHandle(DispatchContext dispatchContext, AwardEntity awardEntity);

}
