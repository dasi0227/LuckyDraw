package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountMonthDao {

    ActivityAccountMonth queryActivityAccountMonth(ActivityAccountMonth activityAccountMonth);

    void createActivityAccountMonth(ActivityAccountMonth activityAccountMonth);

    void decreaseActivityAccountMonthRaffle(ActivityAccountMonth activityAccountMonth);

    void increaseActivityAccountMonthRaffle(ActivityAccountMonth activityAccountMonth);

}
