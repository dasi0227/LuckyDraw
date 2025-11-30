package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.RechargeOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRechargeOrderDao {

    void saveRechargeOrder(RechargeOrder rechargeOrder);

    @DBRouter
    List<RechargeOrder> queryRechargeOrderListByUserId(String userId);

    @DBRouter
    void updateRechargeState(RechargeOrder rechargeOrder);
}
