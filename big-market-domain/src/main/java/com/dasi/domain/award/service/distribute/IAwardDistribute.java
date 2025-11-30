package com.dasi.domain.award.service.distribute;

import com.dasi.domain.award.model.dto.DistributeContext;
import com.dasi.domain.award.model.dto.DistributeResult;

public interface IAwardDistribute {

    DistributeResult doAwardDistribute(DistributeContext distributeContext);

}
