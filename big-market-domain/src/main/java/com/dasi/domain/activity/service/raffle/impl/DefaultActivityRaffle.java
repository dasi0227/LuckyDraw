package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.chain.ActivityChainFactory;
import com.dasi.domain.activity.service.chain.IActivityChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DefaultActivityRaffle extends AbstractActivityRaffle {

    @Resource
    private ActivityChainFactory activityChainFactory;

    public DefaultActivityRaffle(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected void saveRaffleOrder(RaffleOrderAggregate raffleOrderAggregate) {
        activityRepository.saveRaffleOrder(raffleOrderAggregate);
    }

    @Override
    protected RaffleOrderAggregate checkRaffleAvailable(String userId, ActivityEntity activityEntity) {

        ActionChainCheckAggregate actionChainCheckAggregate = new ActionChainCheckAggregate();
        actionChainCheckAggregate.setUserId(userId);
        actionChainCheckAggregate.setActivityId(activityEntity.getActivityId());
        actionChainCheckAggregate.setActivityEntity(activityEntity);

        IActivityChain actionChain = activityChainFactory.getRaffleActionChain();
        return actionChain.action(actionChainCheckAggregate) ? actionChainCheckAggregate.getRaffleOrderAggregate() : null;

    }



}
