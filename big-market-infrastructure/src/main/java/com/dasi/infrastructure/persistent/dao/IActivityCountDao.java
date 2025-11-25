package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityCount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityCountDao {

    ActivityCount queryActivityCountByActivityCountId(Long activityCountId);

}
