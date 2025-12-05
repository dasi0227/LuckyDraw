package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountDao {

    ActivityAccount queryActivityAccount(ActivityAccount activityAccount);

    void createActivityAccount(ActivityAccount activityAccount);

    void rechargeActivityAccount(ActivityAccount activityAccount);

    int subtractActivityAccount(ActivityAccount activityAccount);

}
