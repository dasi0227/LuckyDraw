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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BehaviorQuery implements IBehaviorQuery {

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Override
    public List<QueryActivityBehaviorResult> queryBehavior(QueryActivityBehaviorContext ctx) {

        String userId = ctx.getUserId();
        Long activityId = ctx.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        List<BehaviorEntity> behaviorEntityList = behaviorRepository.queryBehaviorListByActivityId(activityId);

        // 1. 按 behavior_name 分组
        Map<String, List<BehaviorEntity>> groupByName = behaviorEntityList.stream()
                .collect(Collectors.groupingBy(BehaviorEntity::getBehaviorName));

        List<QueryActivityBehaviorResult> queryActivityBehaviorResultList = new ArrayList<>();

        groupByName.forEach((name, list) -> {

            // 2. 合并 rewardDesc ---
            String rewardDesc = list.stream()
                    .map(BehaviorEntity::getRewardDesc)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("+"));

            // 3. 判断是否做过
            boolean isDone = list.stream().anyMatch(entity -> {
                RewardOrderEntity rewardOrderEntity = RewardOrderEntity.builder()
                        .userId(userId)
                        .activityId(activityId)
                        .behaviorType(entity.getBehaviorType())
                        .build();
                return behaviorRepository.queryExistBehaviorToday(rewardOrderEntity);
            });

            // 4. 取第一个作为代表，把合并信息写入返回对象
            BehaviorEntity first = list.get(0);

            QueryActivityBehaviorResult queryActivityBehaviorResult = new QueryActivityBehaviorResult();
            queryActivityBehaviorResult.setBehaviorName(first.getBehaviorName());
            queryActivityBehaviorResult.setBehaviorType(first.getBehaviorType().name());
            queryActivityBehaviorResult.setRewardDesc(rewardDesc);
            queryActivityBehaviorResult.setIsDone(isDone);

            queryActivityBehaviorResultList.add(queryActivityBehaviorResult);
        });

        return queryActivityBehaviorResultList;
    }

}
