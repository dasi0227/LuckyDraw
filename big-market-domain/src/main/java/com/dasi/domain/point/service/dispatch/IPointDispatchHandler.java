package com.dasi.domain.point.service.dispatch;

import com.dasi.domain.point.model.aggregate.PointDispatchAggregate;

public interface IPointDispatchHandler {

    void dispatchHandle(PointDispatchAggregate pointDispatchAggregate);

}
