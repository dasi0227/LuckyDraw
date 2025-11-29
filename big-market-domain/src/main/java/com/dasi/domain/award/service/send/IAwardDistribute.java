package com.dasi.domain.award.service.send;

import com.dasi.domain.award.model.dto.DistributeContext;
import com.dasi.domain.award.model.dto.DistributeResult;

public interface IAwardDistribute {

    DistributeResult doAwardDistribute(DistributeContext distributeContext);

}
