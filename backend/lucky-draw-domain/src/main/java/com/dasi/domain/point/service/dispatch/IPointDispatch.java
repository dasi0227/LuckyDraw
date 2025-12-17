package com.dasi.domain.point.service.dispatch;

import com.dasi.domain.point.model.io.DispatchContext;
import com.dasi.domain.point.model.io.DispatchResult;

public interface IPointDispatch {

    DispatchResult doPointDispatch(DispatchContext dispatchContext);

}
