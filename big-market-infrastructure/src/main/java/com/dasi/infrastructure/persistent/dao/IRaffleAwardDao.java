package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.RaffleAward;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRaffleAwardDao {

    void saveRaffleAward(RaffleAward raffleAward);

    @DBRouter
    int updateRaffleAwardState(RaffleAward raffleAward);
}
