package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.ActivityOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IActivityOrderDao {

    @DBRouter(key = "userId")
    void insert(ActivityOrder activityOrder);

    @DBRouter
    List<ActivityOrder> queryActivityOrderListByUserId(String userId);

}
