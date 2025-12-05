package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;

public interface IAwardRepository {

    void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity);

    void updateActivityAwardState(ActivityAwardEntity activityAwardEntity);

}
