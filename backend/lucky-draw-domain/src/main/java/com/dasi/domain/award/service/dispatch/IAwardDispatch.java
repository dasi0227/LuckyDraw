package com.dasi.domain.award.service.dispatch;

import com.dasi.domain.award.model.io.DispatchContext;
import com.dasi.domain.award.model.io.DispatchResult;

public interface IAwardDispatch {

    DispatchResult doAwardDispatch(DispatchContext dispatchContext);

}
