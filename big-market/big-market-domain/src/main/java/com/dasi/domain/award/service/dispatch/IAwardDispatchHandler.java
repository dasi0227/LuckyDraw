package com.dasi.domain.award.service.dispatch;

import com.dasi.domain.award.model.aggregate.AwardDispatchAggregate;

public interface IAwardDispatchHandler {

    void dispatchHandle(AwardDispatchAggregate awardDispatchAggregate);

}
