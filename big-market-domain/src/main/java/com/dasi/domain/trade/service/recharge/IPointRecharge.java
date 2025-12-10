package com.dasi.domain.trade.service.recharge;

import com.dasi.domain.trade.model.io.PointRechargeContext;
import com.dasi.domain.trade.model.io.PointRechargeResult;

public interface IPointRecharge {

    PointRechargeResult doPointRecharge(PointRechargeContext pointRechargeContext);

}
