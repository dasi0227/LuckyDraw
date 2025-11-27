package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountDao {
    int updateAccountQuota(ActivityAccount activityAccount);

    void insertActivityAccount(ActivityAccount activityAccount);
}
