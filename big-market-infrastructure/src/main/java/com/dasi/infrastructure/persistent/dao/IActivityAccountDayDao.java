package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountDayDao {

    ActivityAccountDay queryActivityAccountDay(ActivityAccountDay activityAccountDay);

    void createActivityAccountDay(ActivityAccountDay activityAccountDay);

    int subtractActivityAccountDay(ActivityAccountDay activityAccountDay);

    void rechargeActivityAccountDay(ActivityAccountDay activityAccountDay);

}
