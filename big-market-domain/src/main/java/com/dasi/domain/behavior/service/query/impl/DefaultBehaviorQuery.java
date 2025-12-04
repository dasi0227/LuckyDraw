package com.dasi.domain.behavior.service.query.impl;

import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.domain.behavior.service.query.IBehaviorQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultBehaviorQuery implements IBehaviorQuery {

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Override
    public Boolean querySign(String userId, Long activityId) {
        return behaviorRepository.querySign(userId, activityId);
    }

}
