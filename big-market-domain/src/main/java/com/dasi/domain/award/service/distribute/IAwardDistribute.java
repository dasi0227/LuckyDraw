package com.dasi.domain.award.service.distribute;

import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;

public interface IAwardDistribute {

    DistributeResult doAwardDistribute(DistributeContext distributeContext);

}
