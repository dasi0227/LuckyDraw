package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.dasi.infrastructure.persistent.po.ActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountMonthDao {

    @DBRouter
    ActivityAccountMonth queryActivityAccountMonth(ActivityAccountMonth activityAccountMonth);


}
