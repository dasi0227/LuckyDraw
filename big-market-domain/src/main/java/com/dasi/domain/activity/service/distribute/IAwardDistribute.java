package com.dasi.domain.activity.service.distribute;

import com.dasi.domain.activity.model.io.DistributeContext;
import com.dasi.domain.activity.model.io.DistributeResult;
import com.dasi.domain.activity.model.entity.ActivityAwardEntity;

public interface IAwardDistribute {

    DistributeResult doAwardDistribute(DistributeContext distributeContext);

    void updateActivityAwardState(ActivityAwardEntity activityAwardEntity);

}
