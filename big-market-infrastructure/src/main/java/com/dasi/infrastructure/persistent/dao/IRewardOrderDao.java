package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.RewardOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRewardOrderDao {

    void saveRewardOrder(RewardOrder rewardOrder);

    void updateRewardOrderState(RewardOrder rewardOrder);

    RewardOrder querySign(RewardOrder rewardOrderReq);

}
