package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Component;

@Component(ActionModel.ACTION_DEFAULT)
public class ActionDefaultChain extends AbstractActionChain {

    @Override
    public Boolean action(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity) {
        return true;
    }

}
