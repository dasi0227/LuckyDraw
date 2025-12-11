package com.dasi.domain.behavior.service.query;

import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.io.QueryActivityBehaviorContext;
import com.dasi.domain.behavior.model.io.QueryActivityBehaviorResult;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BehaviorQuery implements IBehaviorQuery {

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Override
    public List<QueryActivityBehaviorResult> queryDistinctBehavior(QueryActivityBehaviorContext queryActivityBehaviorContext) {

        String userId = queryActivityBehaviorContext.getUserId();
        Long activityId = queryActivityBehaviorContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        List<QueryActivityBehaviorResult> queryActivityBehaviorResultList = new ArrayList<>();

        List<BehaviorEntity> behaviorEntityList = behaviorRepository.queryDistinctBehaviorListByActivityId(activityId);
        behaviorEntityList.forEach(behaviorEntity -> {
            QueryActivityBehaviorResult queryActivityBehaviorResult = new QueryActivityBehaviorResult();
            queryActivityBehaviorResult.setBehaviorName(behaviorEntity.getBehaviorName());
            queryActivityBehaviorResult.setBehaviorType(behaviorEntity.getBehaviorType().name());

            RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .behaviorType(behaviorEntity.getBehaviorType())
                    .build();
            Boolean isDone = behaviorRepository.queryExistBehaviorToday(rewardOrderEntity);
            queryActivityBehaviorResult.setIsDone(isDone);

            queryActivityBehaviorResultList.add(queryActivityBehaviorResult);
        });


        return queryActivityBehaviorResultList;
    }

}
