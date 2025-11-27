package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.entity.ActivityQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;

public interface IActionChain {

    void action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityQuotaEntity activityQuotaEntity);

    IActionChain next();

    IActionChain appendNext(IActionChain next);

}
