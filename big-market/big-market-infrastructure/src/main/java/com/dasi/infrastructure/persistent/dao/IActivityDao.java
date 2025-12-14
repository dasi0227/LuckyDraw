package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Activity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IActivityDao {

    Activity queryActivityByActivityId(Long activityId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);

    List<Activity> queryActivityList();

}
