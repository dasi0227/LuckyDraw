package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.dasi.infrastructure.persistent.po.RaffleOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleOrderDao {

    @DBRouter
    RaffleOrder queryUnusedRaffleOrder(RaffleOrder raffleOrder);


}
