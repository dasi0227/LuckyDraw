package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityQuota;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityQuotaDao {

    ActivityQuota queryActivityQuotaByActivityQuotaId(Long activityQuotaId);

}
