package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.RechargeOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRechargeOrderDao {

    void saveRechargeOrder(RechargeOrder rechargeOrder);

    void updateRechargeState(RechargeOrder rechargeOrder);

}
