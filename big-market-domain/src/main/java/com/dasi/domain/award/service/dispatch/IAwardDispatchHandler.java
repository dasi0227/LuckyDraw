package com.dasi.domain.award.service.dispatch;

import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;

public interface IAwardDispatchHandler {

    void dispatchHandle(DispatchHandleAggregate dispatchHandleAggregate);

}
