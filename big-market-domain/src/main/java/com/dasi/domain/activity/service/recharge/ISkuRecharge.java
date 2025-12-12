package com.dasi.domain.activity.service.recharge;

import com.dasi.domain.activity.model.io.SkuRechargeContext;
import com.dasi.domain.activity.model.io.SkuRechargeResult;

public interface ISkuRecharge {

    @SuppressWarnings("all")
    SkuRechargeResult doSkuRecharge(SkuRechargeContext skuRechargeContext);

}
