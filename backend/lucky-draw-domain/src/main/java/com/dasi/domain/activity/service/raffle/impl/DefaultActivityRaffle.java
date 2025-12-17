package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RaffleOrderEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.chain.ActivityChainFactory;
import com.dasi.domain.activity.service.chain.IActivityChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DefaultActivityRaffle extends AbstractActivityRaffle {

    public DefaultActivityRaffle(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Resource
    private ActivityChainFactory activityChainFactory;

    @Override
    protected void saveRaffleOrder(RaffleOrderEntity raffleOrderEntity) {
        activityRepository.saveRaffleOrder(raffleOrderEntity);
    }

    @Override
    protected Boolean checkRaffleAvailable(String userId, ActivityEntity activityEntity) {
        ActionChainCheckAggregate actionChainCheckAggregate = ActionChainCheckAggregate.builder()
                .userId(userId)
                .activityId(activityEntity.getActivityId())
                .activityEntity(activityEntity)
                .build();
        IActivityChain actionChain = activityChainFactory.getRaffleActionChain();
        return actionChain.action(actionChainCheckAggregate);
    }

}
