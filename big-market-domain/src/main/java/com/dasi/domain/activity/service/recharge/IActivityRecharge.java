package com.dasi.domain.activity.service.recharge;

import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;

public interface IActivityRecharge {

    RechargeResult skuRecharge(RechargeContext rechargeContext);

}
