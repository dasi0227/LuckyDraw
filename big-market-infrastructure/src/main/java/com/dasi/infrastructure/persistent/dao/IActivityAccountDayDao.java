package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.dasi.infrastructure.persistent.po.ActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountDayDao {

    @DBRouter
    ActivityAccountDay queryActivityAccountDay(ActivityAccountDay activityAccountDay);

}
