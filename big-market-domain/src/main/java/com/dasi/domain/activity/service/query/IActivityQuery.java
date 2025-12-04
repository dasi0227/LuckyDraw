package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.entity.ActivityAccountEntity;

public interface IActivityQuery {

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

}
