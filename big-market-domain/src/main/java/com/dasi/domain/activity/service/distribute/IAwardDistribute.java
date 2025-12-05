package com.dasi.domain.activity.service.distribute;

import com.dasi.domain.activity.model.io.DistributeContext;
import com.dasi.domain.activity.model.io.DistributeResult;

public interface IAwardDistribute {

    DistributeResult doAwardDistribute(DistributeContext distributeContext);

}
