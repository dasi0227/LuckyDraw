package com.dasi.domain.activity.repository;

import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;

public interface IActivityRepository {

    ActivitySkuEntity queryActivitySkuBySku(Long sku);

    ActivityEntity queryActivityByActivityId(Long activityId);

    ActivityCountEntity queryActivityCountByActivityCountId(Long activityCountId);
}
