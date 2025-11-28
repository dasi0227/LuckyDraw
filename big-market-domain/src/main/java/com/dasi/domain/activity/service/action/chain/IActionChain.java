package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;

public interface IActionChain {

    Boolean action(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity);

    IActionChain next();

    IActionChain appendNext(IActionChain next);

    IActionChain clone();

}
