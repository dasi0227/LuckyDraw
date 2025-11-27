package com.dasi.domain.activity.service.recharge;

import com.dasi.domain.activity.model.dto.SkuRechargeContext;
import com.dasi.domain.activity.model.dto.SkuRechargeResult;

public interface IActivityRecharge {

    SkuRechargeResult skuRecharge(SkuRechargeContext skuRechargeContext);

}
