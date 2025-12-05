package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.dasi.infrastructure.persistent.po.ActivityAward;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IActivityAwardDao {

    void saveActivityAward(ActivityAward activityAward);

    void updateActivityAwardState(ActivityAward activityAward);

}
