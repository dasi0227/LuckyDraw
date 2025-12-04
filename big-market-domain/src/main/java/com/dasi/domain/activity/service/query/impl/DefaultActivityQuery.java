package com.dasi.domain.activity.service.query.impl;

import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.query.IActivityQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultActivityQuery implements IActivityQuery {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        activityRepository.createActivityAccountIfAbsent(userId, activityId);
        return activityRepository.queryActivityAccountByActivityId(userId, activityId);
    }

}
