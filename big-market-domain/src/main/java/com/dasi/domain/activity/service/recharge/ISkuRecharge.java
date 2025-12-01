package com.dasi.domain.activity.service.recharge;

import com.dasi.domain.activity.model.io.RechargeContext;
import com.dasi.domain.activity.model.io.RechargeResult;

public interface ISkuRecharge {

    RechargeResult doRecharge(RechargeContext rechargeContext);

}
