package com.dasi.domain.point.service.recharge;

import com.dasi.domain.point.model.io.PointRechargeContext;
import com.dasi.domain.point.model.io.PointRechargeResult;

public interface IPointRecharge {

    PointRechargeResult doPointRecharge(PointRechargeContext pointRechargeContext);

}
