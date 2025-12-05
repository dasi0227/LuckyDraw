package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.RaffleOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRaffleOrderDao {

    RaffleOrder queryUnusedRaffleOrder(RaffleOrder raffleOrder);

    void saveRaffleOrder(RaffleOrder raffleOrder);

    int updateRaffleOrderState(RaffleOrder raffleOrder);

}
